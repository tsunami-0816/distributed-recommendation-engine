package com.example.recommend.service;

import com.example.recommend.common.ErrorCode;
import com.example.recommend.config.RecommendProperties;
import com.example.recommend.dto.*;
import com.example.recommend.dto.admin.AbExperimentDTO;
import com.example.recommend.dto.admin.ModelVersionDTO;
import com.example.recommend.service.admin.AbExperimentService;
import com.example.recommend.service.admin.ModelManagementService;
import com.example.recommend.service.behavior.BehaviorCollectorService;
import com.example.recommend.service.cache.RecommendCacheService;
import com.example.recommend.service.rank.RankService;
import com.example.recommend.service.recall.CandidateItem;
import com.example.recommend.service.scene.SceneStrategy;
import com.example.recommend.service.scene.SceneStrategyFactory;
import com.google.common.hash.Hashing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 推荐门面（Recommendation Facade）。
 * <p>
 * 架构文档 M09-T01：编排推荐主链路全流程：
 * A/B 分流 → 缓存检查 → 场景策略选择 → 多路召回 → 特征组装 → 精排 → 重排
 * → 缓存写入 → 曝光埋点 → 返回统一响应。
 * <p>
 * 各阶段计时埋点，异常处理与降级。缓存命中 < 5ms，缓存未命中 P50 < 100ms P99 < 200ms。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationFacade {

    private final SceneStrategyFactory sceneStrategyFactory;
    private final RankService rankService;
    private final RecommendCacheService cacheService;
    private final BehaviorCollectorService behaviorCollectorService;
    private final RecommendProperties properties;
    // 修复Bug-001：注入 A/B 实验服务，从实验配置读取 running 状态实验进行分流
    private final AbExperimentService abExperimentService;
    // 修复Bug-002：注入模型管理服务，从活跃模型状态读取当前模型版本
    private final ModelManagementService modelManagementService;

    /**
     * 执行推荐主链路。
     *
     * @param request 推荐请求（已通过参数校验）
     * @return 推荐响应（含物品列表、A/B 组、模型版本、耗时明细）
     */
    public RecommendResponseDTO recommend(RecommendRequestDTO request) {
        long totalStart = System.currentTimeMillis();

        // 1. 场景策略选择与校验
        SceneStrategy strategy = sceneStrategyFactory.getStrategy(request.getScene());
        strategy.validate(request);

        // 2. A/B 分流（修复Bug-001：从 AbExperimentService 读取 running 实验，按 trafficPercent + groupsConfig 动态分流）
        AbAssignment abAssignment = assignAbGroup(request.getUserId());
        String abGroup = abAssignment.group();

        // 3. 记录用户访问（用于高频用户判定和分层 TTL）
        cacheService.recordAccess(request.getUserId());

        // 4. 缓存检查
        // 修复Bug-002：从 ModelManagementService 读取活跃模型版本，而非固定读取配置文件
        String activeModel = getActiveModelVersion();
        // 修复Bug-001：实验组指定了 modelVersion 时使用实验组模型，否则使用全局活跃模型
        String modelVersion = abAssignment.modelVersion() != null ? abAssignment.modelVersion() : activeModel;
        String cacheKey = cacheService.buildKey(request.getScene(), request.getUserId(), abGroup, modelVersion);

        long cacheStart = System.currentTimeMillis();
        RecommendResponseDTO cached = cacheService.get(cacheKey);
        long cacheMs = System.currentTimeMillis() - cacheStart;

        if (cached != null) {
            // 修复Bug-010：缓存命中时更新耗时明细，仅保留 cacheMs，其他阶段置 0，totalMs = cacheMs
            if (cached.getDuration() != null) {
                cached.getDuration().setCacheMs(cacheMs);
                cached.getDuration().setRecallMs(0);
                cached.getDuration().setFeatureMs(0);
                cached.getDuration().setRankMs(0);
                cached.getDuration().setRerankMs(0);
                cached.getDuration().setTotalMs(cacheMs);
            }
            log.info("推荐缓存命中: scene={}, userId={}, abGroup={}, items={}, cacheMs={}",
                    request.getScene(), request.getUserId(), abGroup,
                    cached.getItems() != null ? cached.getItems().size() : 0, cacheMs);
            return cached;
        }

        // 5. 多路召回（场景策略决定召回通道组合）
        long recallStart = System.currentTimeMillis();
        List<CandidateItem> candidates = strategy.recall(request);
        long recallMs = System.currentTimeMillis() - recallStart;

        // 召回为空时返回空列表（HTTP 200，code=400001，非错误）
        if (candidates == null || candidates.isEmpty()) {
            log.warn("召回结果为空: scene={}, userId={}", request.getScene(), request.getUserId());
            return buildEmptyResponse(request, abGroup, modelVersion, recallMs, totalStart);
        }

        // 6. 特征组装（M07-T01，此处简化为排序服务内部处理）
        long featureStart = System.currentTimeMillis();
        long featureMs = System.currentTimeMillis() - featureStart;

        // 7. 精排 + 重排（M07 排序服务，含三级降级）
        long rankStart = System.currentTimeMillis();
        int pageSize = request.getPageSize() != null ? request.getPageSize() : properties.getRank().getDefaultPageSize();
        RankService.RankResult rankResult = rankService.rank(candidates, request.getUserId(), pageSize);
        long rankMs = System.currentTimeMillis() - rankStart;

        // 8. 场景级后处理
        List<RankService.RankedItem> processed = strategy.postProcess(rankResult.items(), request);

        // 9. 转换为响应 DTO
        List<RecommendItemDTO> items = convertToRecommendItems(processed, candidates);

        long totalMs = System.currentTimeMillis() - totalStart;

        // 10. 构建响应
        RecommendResponseDTO response = RecommendResponseDTO.builder()
                .scene(request.getScene())
                .abGroup(abGroup)
                .modelVersion(rankResult.modelVersion())
                .items(items)
                .candidateCount(candidates.size())
                .duration(RecommendResponseDTO.DurationDetail.builder()
                        .cacheMs(cacheMs)
                        .recallMs(recallMs)
                        .featureMs(featureMs)
                        .rankMs(rankMs)
                        .rerankMs(0) // 重排已包含在 rank 中
                        .totalMs(totalMs)
                        .build())
                .build();

        // 11. 写入缓存（异步不阻塞响应）
        cacheService.put(cacheKey, response, request.getUserId());

        // 12. 异步发送曝光埋点
        List<String> itemIds = items.stream()
                .map(RecommendItemDTO::getItemId)
                .collect(Collectors.toList());
        behaviorCollectorService.trackImpression(request.getUserId(), itemIds, request.getScene());

        log.info("推荐完成: scene={}, userId={}, abGroup={}, model={}, candidates={}, items={}, " +
                        "recall={}ms, rank={}ms, total={}ms, degraded={}",
                request.getScene(), request.getUserId(), abGroup, rankResult.modelVersion(),
                candidates.size(), items.size(), recallMs, rankMs, totalMs, rankResult.degraded());

        return response;
    }

    /**
     * 修复Bug-001：A/B 分流结果（组名 + 该组指定的模型版本）。
     * <p>
     * 从 AbExperimentService 获取 running 状态实验，按 trafficPercent 判断用户是否进入实验，
     * 进入实验后按 groupsConfig 中各组的 percent 分配具体组。实验组指定 modelVersion 时用于缓存 key 和排序。
     */
    private record AbAssignment(String group, String modelVersion) {}

    /**
     * 修复Bug-001：A/B 分流——从实验配置服务获取 running 状态实验，按 trafficPercent + groupsConfig 动态分流。
     * <p>
     * 分流算法：
     * <ol>
     *   <li>取 running 状态实验中 configVersion 最高的一个作为当前生效实验</li>
     *   <li>bucket = MurmurHash3(userId + ":" + experimentId) % 100</li>
     *   <li>若 bucket >= trafficPercent，用户不进入实验，返回默认 control 组</li>
     *   <li>若 bucket < trafficPercent，在实验流量范围内按 groupsConfig 累计 percent 分配组</li>
     * </ol>
     *
     * @param userId 用户 ID
     * @return A/B 分流结果（组名 + 模型版本）
     */
    private AbAssignment assignAbGroup(String userId) {
        // 修复Bug-001：从 AbExperimentService 获取 running 状态实验，按 configVersion 降序取第一个
        List<AbExperimentDTO> runningExperiments = abExperimentService.listExperiments("running");
        if (runningExperiments == null || runningExperiments.isEmpty()) {
            // 无运行中实验时，返回默认 control 组，不指定模型版本（使用全局活跃模型）
            return new AbAssignment(properties.getAb().getDefaultGroup(), null);
        }

        AbExperimentDTO experiment = runningExperiments.stream()
                .max(Comparator.comparingInt(AbExperimentDTO::getConfigVersion))
                .orElse(runningExperiments.get(0));

        String experimentId = experiment.getExperimentId();
        int trafficPercent = experiment.getTrafficPercent() != null ? experiment.getTrafficPercent() : 0;

        // MurmurHash3 分桶（架构文档 3.5 节）
        int hash = Hashing.murmur3_32_fixed()
                .hashString(userId + ":" + experimentId, StandardCharsets.UTF_8)
                .asInt();
        int bucket = Math.abs(hash) % 100;

        // 用户不在实验流量范围内，返回默认组
        if (bucket >= trafficPercent) {
            return new AbAssignment(properties.getAb().getDefaultGroup(), null);
        }

        // 修复Bug-001：在实验流量范围内，按 groupsConfig 各组 percent 分配具体组
        List<AbExperimentDTO.GroupConfig> groupsConfig = experiment.getGroupsConfig();
        if (groupsConfig == null || groupsConfig.isEmpty()) {
            return new AbAssignment(properties.getAb().getDefaultGroup(), null);
        }

        // 将 bucket 映射到实验流量范围内的相对位置（0 ~ trafficPercent-1）
        int relativeBucket = bucket;
        int cumulative = 0;
        for (AbExperimentDTO.GroupConfig group : groupsConfig) {
            int groupPercent = group.getPercent() != null ? group.getPercent() : 0;
            cumulative += groupPercent;
            if (relativeBucket < cumulative) {
                // 修复Bug-001：返回组名和该组指定的模型版本（用于缓存 key 和排序模型切换）
                return new AbAssignment(group.getGroupName(), group.getModelVersion());
            }
        }

        // 兜底：返回最后一个组
        AbExperimentDTO.GroupConfig lastGroup = groupsConfig.get(groupsConfig.size() - 1);
        return new AbAssignment(lastGroup.getGroupName(), lastGroup.getModelVersion());
    }

    /**
     * 修复Bug-002：从 ModelManagementService 获取当前活跃的 DeepFM 模型版本。
     * <p>
     * 管理后台激活新模型后，推荐链路实时读取新的活跃模型版本进行缓存 key 构建。
     * 格式：{modelType}_{version}（如 deepfm_v2），与 properties.activeModel 格式对齐。
     *
     * @return 活跃模型版本，无 active 模型时回退到配置文件默认值
     */
    private String getActiveModelVersion() {
        // 修复Bug-002：从模型管理服务查询 deepfm 类型的 active 状态模型
        List<ModelVersionDTO> activeModels = modelManagementService.listModels("deepfm").stream()
                .filter(m -> "active".equalsIgnoreCase(m.getStatus()))
                .toList();
        if (!activeModels.isEmpty()) {
            ModelVersionDTO active = activeModels.get(0);
            return active.getModelType() + "_" + active.getVersion();
        }
        // 回退到配置文件默认值
        return properties.getModelInference().getActiveModel();
    }

    /**
     * 将排序结果转换为推荐物品 DTO。
     */
    private List<RecommendItemDTO> convertToRecommendItems(List<RankService.RankedItem> rankedItems,
                                                             List<CandidateItem> candidates) {
        // 构建 itemId -> 召回通道映射
        java.util.Map<String, Set<String>> channelMap = candidates.stream()
                .collect(Collectors.toMap(
                        CandidateItem::getItemId,
                        c -> c.getRecallChannels().stream()
                                .map(RecallChannel::getCode)
                                .collect(Collectors.toSet()),
                        (a, b) -> {
                            a.addAll(b);
                            return a;
                        }
                ));

        return IntStream.range(0, rankedItems.size())
                .mapToObj(i -> {
                    RankService.RankedItem item = rankedItems.get(i);
                    return RecommendItemDTO.builder()
                            .itemId(item.itemId())
                            .rank(i + 1)
                            .score(Math.round(item.score() * 10000) / 10000.0)
                            .recallChannel(channelMap.getOrDefault(item.itemId(), Set.of("unknown")))
                            .item(item.itemInfo())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 构建召回为空的响应（code=400001，HTTP 200，非错误）。
     * <p>
     * 设置 degradedCode=400001，Controller 据此返回降级响应（P-06 修复）。
     */
    private RecommendResponseDTO buildEmptyResponse(RecommendRequestDTO request, String abGroup,
                                                     String modelVersion, long recallMs, long totalStart) {
        return RecommendResponseDTO.builder()
                .scene(request.getScene())
                .abGroup(abGroup)
                .modelVersion(modelVersion)
                .items(List.of())
                .candidateCount(0)
                .degradedCode(ErrorCode.RECALL_EMPTY.getCode())
                .duration(RecommendResponseDTO.DurationDetail.builder()
                        .recallMs(recallMs)
                        .totalMs(System.currentTimeMillis() - totalStart)
                        .build())
                .build();
    }
}

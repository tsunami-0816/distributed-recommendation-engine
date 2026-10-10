package com.example.recommend.service.admin;

import com.example.recommend.common.BusinessException;
import com.example.recommend.common.ErrorCode;
import com.example.recommend.dto.admin.AbExperimentDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A/B 实验管理服务 Mock 实现（M11-T01）。
 * <p>
 * 内存存储实验数据，模拟完整的状态机流转和指标报告。
 * 生产环境由 M10 实验平台替换。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@Service
public class MockAbExperimentService implements AbExperimentService {

    /** 内存存储：experimentId → experiment */
    private final Map<String, AbExperimentDTO> store = new ConcurrentHashMap<>();

    public MockAbExperimentService() {
        initMockData();
    }

    @Override
    public List<AbExperimentDTO> listExperiments(String status) {
        List<AbExperimentDTO> result = new ArrayList<>(store.values());
        if (status != null && !status.isBlank()) {
            result = result.stream()
                    .filter(e -> status.equalsIgnoreCase(e.getStatus()))
                    .collect(Collectors.toList());
        }
        result.sort((a, b) -> b.getConfigVersion().compareTo(a.getConfigVersion()));
        return result;
    }

    @Override
    public AbExperimentDTO createExperiment(AbExperimentDTO experiment) {
        String experimentId = "exp_" + UUID.randomUUID().toString().substring(0, 8);
        experiment.setExperimentId(experimentId);
        experiment.setStatus("draft");
        experiment.setConfigVersion(1);
        if (experiment.getTrafficPercent() == null) {
            experiment.setTrafficPercent(0);
        }
        store.put(experimentId, experiment);
        log.info("创建实验: id={}, name={}", experimentId, experiment.getName());
        return experiment;
    }

    @Override
    public AbExperimentDTO updateExperiment(String experimentId, AbExperimentDTO experiment) {
        AbExperimentDTO existing = getOrThrow(experimentId);
        // 修复Bug-013：running 状态的实验不允许修改核心配置，仅允许 draft/paused 状态修改
        // 运行中修改 trafficPercent、groupsConfig 等会导致分流逻辑在实验过程中变化，实验数据被污染
        if ("running".equalsIgnoreCase(existing.getStatus())) {
            throw new BusinessException(ErrorCode.EXPERIMENT_STATUS_INVALID,
                    "实验当前状态为 running，不允许修改配置，请先暂停实验");
        }
        if (experiment.getName() != null) existing.setName(experiment.getName());
        if (experiment.getTrafficPercent() != null) existing.setTrafficPercent(experiment.getTrafficPercent());
        if (experiment.getGroupsConfig() != null) existing.setGroupsConfig(experiment.getGroupsConfig());
        if (experiment.getStartTime() != null) existing.setStartTime(experiment.getStartTime());
        if (experiment.getEndTime() != null) existing.setEndTime(experiment.getEndTime());
        existing.setConfigVersion(existing.getConfigVersion() + 1);
        log.info("更新实验: id={}, configVersion={}", experimentId, existing.getConfigVersion());
        return existing;
    }

    @Override
    public AbExperimentDTO startExperiment(String experimentId) {
        AbExperimentDTO exp = getOrThrow(experimentId);
        if (!"draft".equals(exp.getStatus()) && !"paused".equals(exp.getStatus())) {
            throw new BusinessException(ErrorCode.EXPERIMENT_STATUS_INVALID,
                    "实验当前状态为 " + exp.getStatus() + "，不允许启动");
        }
        exp.setStatus("running");
        if (exp.getStartTime() == null) {
            exp.setStartTime(Instant.now());
        }
        log.info("启动实验: id={}, name={}", experimentId, exp.getName());
        return exp;
    }

    @Override
    public AbExperimentDTO pauseExperiment(String experimentId) {
        AbExperimentDTO exp = getOrThrow(experimentId);
        if (!"running".equals(exp.getStatus())) {
            throw new BusinessException(ErrorCode.EXPERIMENT_STATUS_INVALID,
                    "实验当前状态为 " + exp.getStatus() + "，仅 running 状态可暂停");
        }
        exp.setStatus("paused");
        log.info("暂停实验: id={}, name={}", experimentId, exp.getName());
        return exp;
    }

    @Override
    public AbExperimentDTO endExperiment(String experimentId) {
        AbExperimentDTO exp = getOrThrow(experimentId);
        if (!"running".equals(exp.getStatus()) && !"paused".equals(exp.getStatus())) {
            throw new BusinessException(ErrorCode.EXPERIMENT_STATUS_INVALID,
                    "实验当前状态为 " + exp.getStatus() + "，不允许结束");
        }
        exp.setStatus("ended");
        exp.setEndTime(Instant.now());
        log.info("结束实验: id={}, name={}", experimentId, exp.getName());
        return exp;
    }

    @Override
    public AbExperimentDTO getExperimentReport(String experimentId) {
        return getOrThrow(experimentId);
    }

    private AbExperimentDTO getOrThrow(String experimentId) {
        AbExperimentDTO exp = store.get(experimentId);
        if (exp == null) {
            throw new BusinessException(ErrorCode.EXPERIMENT_NOT_FOUND,
                    "实验不存在: " + experimentId);
        }
        return exp;
    }

    /**
     * 初始化 Mock 实验数据（对齐前端 mock/index.js）。
     */
    private void initMockData() {
        Instant now = Instant.now();

        store.put("exp_001", AbExperimentDTO.builder()
                .experimentId("exp_001")
                .name("首页推荐模型对比实验")
                .status("running")
                .trafficPercent(50)
                .configVersion(3)
                .startTime(now.minus(6, ChronoUnit.DAYS))
                .endTime(now.plus(11, ChronoUnit.DAYS))
                .groupsConfig(List.of(
                        AbExperimentDTO.GroupConfig.builder().groupName("control").percent(50)
                                .modelVersion("deepfm_v1").description("对照组-当前模型").build(),
                        AbExperimentDTO.GroupConfig.builder().groupName("experiment").percent(50)
                                .modelVersion("deepfm_v2").description("实验组-新模型").build()
                ))
                .metrics(Map.of(
                        "ctr", AbExperimentDTO.MetricData.builder().control(0.125).experiment(0.142).lift(13.6).pValue(0.012).build(),
                        "cvr", AbExperimentDTO.MetricData.builder().control(0.032).experiment(0.038).lift(18.75).pValue(0.028).build()
                ))
                .build());

        store.put("exp_002", AbExperimentDTO.builder()
                .experimentId("exp_002")
                .name("搜索场景召回通道优化")
                .status("running")
                .trafficPercent(30)
                .configVersion(1)
                .startTime(now.minus(3, ChronoUnit.DAYS))
                .endTime(now.plus(16, ChronoUnit.DAYS))
                .groupsConfig(List.of(
                        AbExperimentDTO.GroupConfig.builder().groupName("control").percent(70).description("对照组-原召回策略").build(),
                        AbExperimentDTO.GroupConfig.builder().groupName("experiment").percent(30).description("实验组-新增语义召回").build()
                ))
                .metrics(Map.of(
                        "ctr", AbExperimentDTO.MetricData.builder().control(0.098).experiment(0.115).lift(17.3).pValue(0.045).build()
                ))
                .build());

        store.put("exp_003", AbExperimentDTO.builder()
                .experimentId("exp_003")
                .name("重排策略长尾加权测试")
                .status("paused")
                .trafficPercent(20)
                .configVersion(2)
                .startTime(now.minus(9, ChronoUnit.DAYS))
                .endTime(now.plus(6, ChronoUnit.DAYS))
                .groupsConfig(List.of(
                        AbExperimentDTO.GroupConfig.builder().groupName("control").percent(80).description("对照组").build(),
                        AbExperimentDTO.GroupConfig.builder().groupName("experiment").percent(20).description("实验组-长尾加权×1.1").build()
                ))
                .metrics(Map.of(
                        "ctr", AbExperimentDTO.MetricData.builder().control(0.11).experiment(0.108).lift(-1.8).pValue(0.32).build()
                ))
                .build());

        store.put("exp_004", AbExperimentDTO.builder()
                .experimentId("exp_004")
                .name("缓存TTL策略调优")
                .status("draft")
                .trafficPercent(0)
                .configVersion(1)
                .groupsConfig(List.of(
                        AbExperimentDTO.GroupConfig.builder().groupName("control").percent(50).description("当前TTL策略").build(),
                        AbExperimentDTO.GroupConfig.builder().groupName("experiment").percent(50).description("优化后TTL策略").build()
                ))
                .build());

        store.put("exp_005", AbExperimentDTO.builder()
                .experimentId("exp_005")
                .name("行为采集延迟优化验证")
                .status("ended")
                .trafficPercent(100)
                .configVersion(1)
                .startTime(now.minus(19, ChronoUnit.DAYS))
                .endTime(now.minus(9, ChronoUnit.DAYS))
                .groupsConfig(List.of(
                        AbExperimentDTO.GroupConfig.builder().groupName("control").percent(50).description("旧采集链路").build(),
                        AbExperimentDTO.GroupConfig.builder().groupName("experiment").percent(50).description("新采集链路").build()
                ))
                .metrics(Map.of(
                        "latencyP99", AbExperimentDTO.MetricData.builder().control(4500.0).experiment(2800.0).lift(-37.8).pValue(0.003).build()
                ))
                .build());
    }
}

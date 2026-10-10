package com.example.recommend.service.admin;

import com.example.recommend.common.BusinessException;
import com.example.recommend.common.ErrorCode;
import com.example.recommend.config.RecommendProperties;
import com.example.recommend.dto.admin.SystemConfigDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统配置管理服务 Mock 实现（M11-T03）。
 * <p>
 * 内存存储配置数据，模拟动态配置读写和热更新。
 * 生产环境由配置中心（Nacos / Apollo）替换，配置写入 Redis config:dynamic。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@Service
public class MockSystemConfigService implements SystemConfigService {

    /** 内存存储：configKey → config */
    private final Map<String, SystemConfigDTO> store = new ConcurrentHashMap<>();

    // 修复Bug-003：注入 RecommendProperties，配置更新时同步刷新运行中的配置对象，实现热更新
    private final RecommendProperties recommendProperties;

    // 修复Bug-003：构造函数注入 RecommendProperties（显式构造函数会覆盖 @RequiredArgsConstructor，需手动声明参数）
    public MockSystemConfigService(RecommendProperties recommendProperties) {
        this.recommendProperties = recommendProperties;
        initMockData();
    }

    @Override
    public List<SystemConfigDTO> listConfigs() {
        List<SystemConfigDTO> result = new ArrayList<>(store.values());
        result.sort((a, b) -> a.getConfigKey().compareTo(b.getConfigKey()));
        return result;
    }

    @Override
    public SystemConfigDTO updateConfig(String key, String value) {
        SystemConfigDTO existing = store.get(key);
        if (existing == null) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND, "配置项不存在: " + key);
        }
        existing.setConfigValue(value);
        existing.setUpdatedAt(Instant.now());
        existing.setUpdatedBy("admin");

        // 修复Bug-003：配置更新后同步刷新 RecommendProperties 对应字段，实现运行时热更新，无需重启服务
        syncToProperties(key, value);

        log.info("更新配置: key={}, value={}", key, value);
        return existing;
    }

    /**
     * 修复Bug-003：将配置值同步到 RecommendProperties 对应字段，实现动态配置热更新。
     * <p>
     * 仅同步已在 RecommendProperties 中定义的配置项；未知配置项仅更新 store，不影响运行时。
     *
     * @param key   配置 Key
     * @param value 配置值（字符串形式，按目标类型解析）
     */
    private void syncToProperties(String key, String value) {
        try {
            switch (key) {
                // 缓存分层 TTL
                case "recommend.cache.high-frequency-ttl" ->
                        recommendProperties.getCache().setHighFrequencyTtl(Integer.parseInt(value));
                case "recommend.cache.normal-ttl" ->
                        recommendProperties.getCache().setNormalTtl(Integer.parseInt(value));
                case "recommend.cache.fallback-ttl" ->
                        recommendProperties.getCache().setFallbackTtl(Integer.parseInt(value));
                // 排序参数
                case "recommend.rank.default-page-size" ->
                        recommendProperties.getRank().setDefaultPageSize(Integer.parseInt(value));
                case "recommend.rank.max-page-size" ->
                        recommendProperties.getRank().setMaxPageSize(Integer.parseInt(value));
                // 模型推理超时
                case "recommend.model-inference.deepfm-timeout-ms" ->
                        recommendProperties.getModelInference().setDeepfmTimeoutMs(Long.parseLong(value));
                case "recommend.model-inference.xgboost-timeout-ms" ->
                        recommendProperties.getModelInference().setXgboostTimeoutMs(Long.parseLong(value));
                default -> log.debug("配置项 {} 未映射到 RecommendProperties，仅更新 store", key);
            }
        } catch (NumberFormatException e) {
            log.warn("配置热更新失败，值格式不匹配: key={}, value={}, error={}", key, value, e.getMessage());
        }
    }

    /**
     * 初始化 Mock 配置数据（对齐前端 mock/index.js）。
     */
    private void initMockData() {
        Instant now = Instant.now();
        Instant old = now.minus(14, ChronoUnit.DAYS);

        put("model.rank.active", "deepfm_v2", "排序服务当前活跃模型", now.minus(2, ChronoUnit.DAYS));
        put("recommend.rank.default-page-size", "20", "默认返回数量", old);
        put("recommend.rank.max-page-size", "50", "最大返回数量", old);
        put("recommend.cache.high-frequency-ttl", "10", "高频用户缓存TTL（秒）", now.minus(9, ChronoUnit.DAYS));
        put("recommend.cache.normal-ttl", "60", "普通用户缓存TTL（秒）", old);
        put("recommend.cache.fallback-ttl", "120", "兜底用户缓存TTL（秒）", old);
        put("recommend.model-inference.deepfm-timeout-ms", "80", "DeepFM推理超时（毫秒）", old);
        put("recommend.model-inference.xgboost-timeout-ms", "20", "XGBoost推理超时（毫秒）", old);
        put("ab.experiment.default-exp-id", "exp_default", "默认A/B实验ID", old);
    }

    private void put(String key, String value, String description, Instant updatedAt) {
        store.put(key, SystemConfigDTO.builder()
                .configKey(key)
                .configValue(value)
                .description(description)
                .updatedAt(updatedAt)
                .updatedBy("admin")
                .build());
    }
}

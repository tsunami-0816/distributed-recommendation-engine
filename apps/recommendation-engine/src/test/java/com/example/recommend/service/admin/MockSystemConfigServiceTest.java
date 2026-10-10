package com.example.recommend.service.admin;

import com.example.recommend.common.BusinessException;
import com.example.recommend.config.RecommendProperties;
import com.example.recommend.dto.admin.SystemConfigDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 修复Bug-010：MockSystemConfigService 单元测试。
 * <p>
 * 覆盖配置查询、更新、热更新同步（修复Bug-003）等核心逻辑。
 *
 * @author recommend-team
 * @since 1.0.0
 */
class MockSystemConfigServiceTest {

    private MockSystemConfigService configService;
    private RecommendProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RecommendProperties();
        configService = new MockSystemConfigService(properties);
    }

    @Test
    @DisplayName("修复Bug-003：更新缓存 TTL 配置后，RecommendProperties 对应字段同步更新（热更新）")
    void updateConfig_cacheNormalTtl_shouldSyncToRecommendProperties() {
        // 验证初始值
        assertEquals(60, properties.getCache().getNormalTtl());

        // 更新配置
        SystemConfigDTO result = configService.updateConfig("recommend.cache.normal-ttl", "30");

        // 验证 store 中配置已更新
        assertEquals("30", result.getConfigValue());

        // 修复Bug-003：验证 RecommendProperties 同步更新，无需重启服务
        assertEquals(30, properties.getCache().getNormalTtl(),
                "配置更新后 RecommendProperties.normalTtl 应同步更新为 30");
    }

    @Test
    @DisplayName("修复Bug-003：更新高频用户 TTL 配置后同步更新")
    void updateConfig_highFrequencyTtl_shouldSyncToRecommendProperties() {
        assertEquals(10, properties.getCache().getHighFrequencyTtl());

        configService.updateConfig("recommend.cache.high-frequency-ttl", "5");

        assertEquals(5, properties.getCache().getHighFrequencyTtl());
    }

    @Test
    @DisplayName("修复Bug-003：更新默认 pageSize 配置后同步更新")
    void updateConfig_defaultPageSize_shouldSyncToRecommendProperties() {
        assertEquals(20, properties.getRank().getDefaultPageSize());

        configService.updateConfig("recommend.rank.default-page-size", "30");

        assertEquals(30, properties.getRank().getDefaultPageSize());
    }

    @Test
    @DisplayName("修复Bug-003：更新模型超时配置后同步更新")
    void updateConfig_deepfmTimeout_shouldSyncToRecommendProperties() {
        assertEquals(80, properties.getModelInference().getDeepfmTimeoutMs());

        configService.updateConfig("recommend.model-inference.deepfm-timeout-ms", "100");

        assertEquals(100, properties.getModelInference().getDeepfmTimeoutMs());
    }

    @Test
    @DisplayName("修复Bug-003：更新未映射的配置项不影响 RecommendProperties")
    void updateConfig_unknownKey_shouldNotAffectRecommendProperties() {
        int originalNormalTtl = properties.getCache().getNormalTtl();

        // 更新一个未映射到 RecommendProperties 的配置项
        configService.updateConfig("ab.experiment.default-exp-id", "exp_new");

        // RecommendProperties 不应受影响
        assertEquals(originalNormalTtl, properties.getCache().getNormalTtl());
    }

    @Test
    @DisplayName("修复Bug-003：配置值格式错误时不抛异常，仅记录警告，原配置不变")
    void updateConfig_invalidNumberFormat_shouldKeepOriginalValue() {
        int originalTtl = properties.getCache().getNormalTtl();

        // 传入非数字值，syncToProperties 应捕获 NumberFormatException
        SystemConfigDTO result = configService.updateConfig("recommend.cache.normal-ttl", "not_a_number");

        // store 中配置值已更新（字符串存储），但 RecommendProperties 保持原值
        assertEquals("not_a_number", result.getConfigValue());
        assertEquals(originalTtl, properties.getCache().getNormalTtl(),
                "格式错误时 RecommendProperties 应保持原值");
    }

    @Test
    @DisplayName("更新不存在的配置项应抛异常")
    void updateConfig_nonExistentKey_shouldThrowException() {
        assertThrows(BusinessException.class, () ->
                configService.updateConfig("nonexistent.config.key", "value"));
    }

    @Test
    @DisplayName("查询配置列表返回所有配置项")
    void listConfigs_shouldReturnAllConfigs() {
        List<SystemConfigDTO> configs = configService.listConfigs();

        assertFalse(configs.isEmpty());
        // Mock 数据初始化了 9 个配置项
        assertTrue(configs.size() >= 9);
    }

    @Test
    @DisplayName("更新配置后 updatedAt 和 updatedBy 被设置")
    void updateConfig_shouldSetAuditFields() {
        SystemConfigDTO result = configService.updateConfig("recommend.cache.normal-ttl", "45");

        assertNotNull(result.getUpdatedAt());
        assertEquals("admin", result.getUpdatedBy());
    }
}

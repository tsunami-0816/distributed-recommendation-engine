package com.example.recommend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 推荐引擎配置属性。
 * <p>
 * 对应 application.yml 中 recommend.* 配置项，所有超时、TTL、阈值均外部化，无魔法数字。
 * 架构文档附录 B 配置示例。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "recommend")
public class RecommendProperties {

    /** 召回配置 */
    private Recall recall = new Recall();

    /** 排序配置 */
    private Rank rank = new Rank();

    /** 缓存配置 */
    private Cache cache = new Cache();

    /** 模型推理配置 */
    private ModelInference modelInference = new ModelInference();

    /** A/B 实验配置 */
    private Ab ab = new Ab();

    @Data
    public static class Recall {
        /** 每路召回最小候选数 */
        private int minCandidatesPerChannel = 100;
        /** 合并后候选集上限 */
        private int maxCandidates = 300;
        /** 合并后候选集下限 */
        private int minCandidates = 200;
        /** 召回并行执行超时（毫秒） */
        private long parallelTimeoutMs = 50;
    }

    @Data
    public static class Rank {
        /** 默认返回数量 */
        private int defaultPageSize = 20;
        /** 最大返回数量 */
        private int maxPageSize = 50;
        /** 精排总预算（毫秒） */
        private long totalBudgetMs = 50;
        /** 长尾物品加权倍数（热度后 50% 物品） */
        private double longtailWeightMultiplier = 1.1;
        /** 类目打散最大尝试次数 */
        private int categoryDiversifyMaxAttempts = 3;
    }

    @Data
    public static class Cache {
        /** 高频用户 TTL（秒） */
        private int highFrequencyTtl = 10;
        /** 普通用户 TTL（秒） */
        private int normalTtl = 60;
        /** 兜底用户 TTL（秒） */
        private int fallbackTtl = 120;
        /** TTL 随机偏移范围（秒，±offset 防雪崩） */
        private int ttlRandomOffset = 10;
        /** 高频用户判定阈值（近 5 分钟访问次数） */
        private int highFrequencyThreshold = 10;
        /** 高频用户判定窗口（秒） */
        private int highFrequencyWindowSeconds = 300;
    }

    @Data
    public static class ModelInference {
        /** gRPC 服务地址 */
        private String grpcAddress = "localhost:50051";
        /** DeepFM 超时（毫秒） */
        private long deepfmTimeoutMs = 80;
        /** XGBoost 超时（毫秒） */
        private long xgboostTimeoutMs = 20;
        /** 当前活跃模型 */
        private String activeModel = "deepfm_v1";
        /** 三级降级链 */
        private String degradeChain = "deepfm,xgboost,rule";
        /** 预测结果缓存 TTL（秒） */
        private int predictTtlSeconds = 5;
    }

    @Data
    public static class Ab {
        /** 分流哈希算法 */
        private String hashAlgorithm = "murmur3_32";
        /** 哈希种子 */
        private long hashSeed = 0x9747b28cL;
        /** 默认实验组 */
        private String defaultGroup = "control";
    }
}

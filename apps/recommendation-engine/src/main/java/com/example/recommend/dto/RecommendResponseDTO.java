package com.example.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 推荐响应 DTO。
 * <p>
 * 架构文档 5.4.1 节推荐接口响应 data 结构。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendResponseDTO {

    /** 推荐场景 */
    private String scene;

    /** A/B 实验组 */
    private String abGroup;

    /** 实际使用的模型版本（降级时为降级后的模型） */
    private String modelVersion;

    /** 推荐物品列表 */
    private List<RecommendItemDTO> items;

    /** 召回候选数量（用于调试/监控） */
    private Integer candidateCount;

    /** 各阶段耗时（毫秒），用于性能分析 */
    private DurationDetail duration;

    /**
     * 降级码（P-06 修复）。
     * <p>
     * 当推荐链路发生业务降级时（如召回为空 code=400001），此字段非空，
     * Controller 据此返回 HTTP 200 + 对应业务降级码，而非 code=0。
     * 正常推荐时为 null。
     */
    private Integer degradedCode;

    /**
     * 各阶段耗时明细。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DurationDetail {
        /** 缓存检查耗时 */
        private long cacheMs;
        /** 召回阶段耗时 */
        private long recallMs;
        /** 特征组装耗时 */
        private long featureMs;
        /** 排序阶段耗时 */
        private long rankMs;
        /** 重排阶段耗时 */
        private long rerankMs;
        /** 总耗时 */
        private long totalMs;
    }
}

package com.example.recommend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * 特征一致性校验结果 DTO（M11-T05）。
 * <p>
 * 对应前端 FeatureConsistency.vue 及 /admin/feature-consistency 接口的数据结构。
 * 校验离线训练特征与在线推理特征的一致性，是模型发布的前置门禁。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureConsistencyDTO {

    /** 校验日期（YYYY-MM-DD） */
    private String date;

    /** 抽样数量 */
    private Integer sampleCount;

    /** 通过数量 */
    private Integer passedCount;

    /** 失败数量 */
    private Integer failedCount;

    /** 最大绝对误差 */
    private Double maxAbsError;

    /** 整体状态：PASSED / FAILED */
    private String status;

    /** 校验执行时间 */
    private Instant checkedAt;

    /** 特征明细 */
    private List<FeatureDetail> detail;

    /**
     * 单个特征的校验明细。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureDetail {
        /** 特征名 */
        private String featureName;
        /** 期望值（离线） */
        private Double expected;
        /** 实际值（在线） */
        private Double actual;
        /** 绝对误差 */
        private Double error;
        /** 单特征状态：PASSED / FAILED */
        private String status;
    }
}

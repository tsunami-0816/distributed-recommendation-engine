package com.example.recommend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * A/B 实验 DTO（M11-T01）。
 * <p>
 * 对应前端 AbExperiments.vue 及 /admin/ab/experiments 接口的数据结构。
 * 实验状态机：draft → running → paused → running / ended。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbExperimentDTO {

    /** 实验唯一 ID */
    private String experimentId;

    /** 实验名称 */
    private String name;

    /** 实验状态：draft / running / paused / ended */
    private String status;

    /** 流量占比（0-100） */
    private Integer trafficPercent;

    /** 配置版本号（每次更新自增） */
    private Integer configVersion;

    /** 开始时间 */
    private Instant startTime;

    /** 结束时间 */
    private Instant endTime;

    /** 实验组配置 */
    private List<GroupConfig> groupsConfig;

    /** 实验指标（metricName → 各组数据） */
    private Map<String, MetricData> metrics;

    /**
     * 实验组配置。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupConfig {
        /** 组名：control / experiment */
        private String groupName;
        /** 流量占比 */
        private Integer percent;
        /** 使用的模型版本（可选） */
        private String modelVersion;
        /** 组描述 */
        private String description;
    }

    /**
     * 指标数据（含提升度和显著性）。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricData {
        /** 对照组数值 */
        private Double control;
        /** 实验组数值 */
        private Double experiment;
        /** 相对提升度（%） */
        private Double lift;
        /** p 值（< 0.05 为显著） */
        private Double pValue;
    }
}

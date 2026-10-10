package com.example.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 用户画像 DTO（长期 + 短期融合结果）。
 * <p>
 * 架构文档 M05 用户画像服务：长期画像 TTL 300s，短期画像 TTL 5s，融合权重短期 0.6 / 长期 0.4。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    /** 用户 ID */
    private String userId;

    /** 长期画像（T+1 Spark 离线计算） */
    private LongTermProfile longTerm;

    /** 短期画像（Flink 实时计算，秒级更新） */
    private ShortTermProfile shortTerm;

    /** 融合后的标签权重（Top N） */
    private Map<String, Double> fusedTags;

    /** 画像版本号 */
    private Long version;

    /** 最后更新时间（毫秒时间戳） */
    private Long updatedAt;

    /**
     * 长期画像结构。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LongTermProfile {
        /** 类目偏好（Top 10，类目ID -> 偏好权重 0~1） */
        private Map<String, Double> categoryPreferences;
        /** 标签权重（Top 20，标签 -> TF-IDF 权重） */
        private Map<String, Double> tagWeights;
        /** 活跃度（近 30 天行为次数归一化 0~1） */
        private Double activityLevel;
        /** 活跃时段分布（24 小时，小时 -> 行为占比） */
        private Map<Integer, Double> activeHours;
        /** 价格偏好（交互物品价格分位数，低/中/高） */
        private String pricePreference;
        /** 注册时长（天） */
        private Integer registerDays;
    }

    /**
     * 短期画像结构。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShortTermProfile {
        /** 近 1 小时点击的物品 ID 列表 */
        private List<String> recentClickedItems;
        /** 近 1 小时收藏的物品 ID 列表 */
        private List<String> recentFavoritedItems;
        /** 近 1 小时交互的类目（类目ID -> 次数） */
        private Map<String, Integer> recentCategories;
        /** 近 1 小时行为总数 */
        private Integer recentActionCount;
    }
}

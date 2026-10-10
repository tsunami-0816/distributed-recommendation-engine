package com.example.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热门物品 DTO。
 * <p>
 * 热门物品榜单结构，用于 /api/v1/hot/items 接口和热门召回通道兜底。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotItemDTO {

    /** 物品 ID */
    private String itemId;

    /** 排名（从 1 开始） */
    private Integer rank;

    /** 热度分（click×0.7 + favorite×0.3） */
    private Double hotScore;

    /** 近 24 小时点击数 */
    private Long clickCount;

    /** 近 24 小时收藏数 */
    private Long favoriteCount;

    /** 物品详情 */
    private ItemInfoDTO item;
}

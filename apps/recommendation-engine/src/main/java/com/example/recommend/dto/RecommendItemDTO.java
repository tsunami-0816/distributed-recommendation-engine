package com.example.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 推荐物品 DTO。
 * <p>
 * 架构文档 5.4.1 节推荐物品结构，包含排名、分数、召回通道和物品详情。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendItemDTO {

    /** 物品 ID */
    private String itemId;

    /** 排名（从 1 开始） */
    private Integer rank;

    /** 推荐分数（模型预测的点击率/转化率） */
    private Double score;

    /** 召回通道（该物品来自哪路召回，可能有多路） */
    private Set<String> recallChannel;

    /** 物品详情 */
    private ItemInfoDTO item;
}

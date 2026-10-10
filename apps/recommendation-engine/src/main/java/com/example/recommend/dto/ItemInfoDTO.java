package com.example.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 物品信息 DTO。
 * <p>
 * 对应 MySQL item 表的物品元数据，用于推荐结果展示。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemInfoDTO {

    /** 物品 ID */
    private String itemId;

    /** 物品名称 */
    private String name;

    /** 物品描述 */
    private String description;

    /** 类目 ID */
    private Integer categoryId;

    /** 类目路径（如 "电子产品/手机/智能手机"） */
    private String categoryPath;

    /** 价格（元） */
    private Double price;

    /** 标签列表 */
    private List<String> tags;

    /** 物品状态（1=上架，0=下架） */
    private Integer status;

    /** 热度分（0~1，越高越热门） */
    private Double hotScore;
}

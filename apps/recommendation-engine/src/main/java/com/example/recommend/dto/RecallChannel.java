package com.example.recommend.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 召回通道枚举。
 * <p>
 * 架构文档 M06 召回服务：itemcf / tag / profile / hot / es_text / es_semantic。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Getter
public enum RecallChannel {

    /** 协同过滤召回 */
    ITEM_CF("itemcf"),
    /** 内容标签召回 */
    TAG("tag"),
    /** 用户画像召回 */
    PROFILE("profile"),
    /** 热门趋势召回（兜底） */
    HOT("hot"),
    /** ES 文本检索召回 */
    ES_TEXT("es_text"),
    /** ES 语义检索召回 */
    ES_SEMANTIC("es_semantic");

    @JsonValue
    private final String code;

    RecallChannel(String code) {
        this.code = code;
    }
}

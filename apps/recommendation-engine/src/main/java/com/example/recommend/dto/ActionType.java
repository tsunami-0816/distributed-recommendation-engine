package com.example.recommend.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 行为类型枚举。
 * <p>
 * 架构文档 5.4.2 节：click / view / favorite / impression / conversion。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Getter
public enum ActionType {

    /** 点击 */
    CLICK("click"),
    /** 浏览/曝光 */
    VIEW("view"),
    /** 收藏 */
    FAVORITE("favorite"),
    /** 曝光（推荐返回后埋点） */
    IMPRESSION("impression"),
    /** 转化/下单 */
    CONVERSION("conversion");

    @JsonValue
    private final String code;

    ActionType(String code) {
        this.code = code;
    }

    @JsonCreator
    public static ActionType fromCode(String code) {
        return Arrays.stream(values())
                .filter(a -> a.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知行为类型: " + code));
    }

    /**
     * 判断是否为正反馈行为（用于训练样本正样本）。
     */
    public boolean isPositive() {
        return this == CLICK || this == FAVORITE || this == CONVERSION;
    }
}

package com.example.recommend.dto;

import com.example.recommend.common.BusinessException;
import com.example.recommend.common.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 推荐场景枚举。
 * <p>
 * 架构文档 5.4.1 节：home / guess / related / search 四种场景。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Getter
public enum Scene {

    /** 首页推荐 */
    HOME("home"),
    /** 猜你喜欢 */
    GUESS("guess"),
    /** 相关推荐 */
    RELATED("related"),
    /** 搜索推荐 */
    SEARCH("search");

    @JsonValue
    private final String code;

    Scene(String code) {
        this.code = code;
    }

    /**
     * 根据 code 解析场景，不支持时抛出业务异常。
     *
     * @param code 场景 code
     * @return 场景枚举
     */
    @JsonCreator
    public static Scene fromCode(String code) {
        return Arrays.stream(values())
                .filter(s -> s.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.SCENE_NOT_SUPPORTED,
                        "不支持的推荐场景: " + code));
    }
}

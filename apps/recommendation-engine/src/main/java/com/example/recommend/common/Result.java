package com.example.recommend.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

import java.io.Serializable;

/**
 * 统一响应封装。
 * <p>
 * 所有对外接口均返回此结构，包含业务状态码、消息、追踪ID、时间戳和业务数据。
 * 架构文档 5.1 节统一响应格式。
 *
 * @param <T> 业务数据类型
 * @author recommend-team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    /** 业务状态码，0 表示成功，非 0 表示错误 */
    private int code;

    /** 状态描述，成功为 "success"，错误为具体错误信息 */
    private String message;

    /** 全链路追踪 ID，用于问题排查 */
    private String traceId;

    /** 服务端响应时间戳（毫秒） */
    private long timestamp;

    /** 业务数据，成功时返回具体数据，错误时为 null */
    private T data;

    /**
     * 成功响应（无数据）。
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功响应（带数据）。
     *
     * @param data 业务数据
     */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message(ErrorCode.SUCCESS.getMessage())
                .traceId(MDC.get("traceId"))
                .timestamp(System.currentTimeMillis())
                .data(data)
                .build();
    }

    /**
     * 成功响应（带自定义消息和数据）。
     */
    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message(message)
                .traceId(MDC.get("traceId"))
                .timestamp(System.currentTimeMillis())
                .data(data)
                .build();
    }

    /**
     * 失败响应（基于错误码枚举）。
     *
     * @param errorCode 错误码枚举
     */
    public static <T> Result<T> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * 失败响应（自定义错误码和消息）。
     *
     * @param code    业务状态码
     * @param message 错误描述
     */
    public static <T> Result<T> fail(int code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .traceId(MDC.get("traceId"))
                .timestamp(System.currentTimeMillis())
                .data(null)
                .build();
    }

    /**
     * 降级响应（HTTP 200 但 code 非 0，用于排序降级等场景）。
     *
     * @param errorCode 降级错误码（如 RANK_DEGRADED）
     * @param data      降级后的数据
     */
    public static <T> Result<T> degraded(ErrorCode errorCode, T data) {
        return degraded(errorCode.getCode(), errorCode.getMessage(), data);
    }

    /**
     * 降级响应（HTTP 200 但 code 非 0，自定义 code 和 message）。
     *
     * @param code    降级业务码（如 400001 召回为空）
     * @param message 降级描述
     * @param data    降级后的数据
     */
    public static <T> Result<T> degraded(int code, String message, T data) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .traceId(MDC.get("traceId"))
                .timestamp(System.currentTimeMillis())
                .data(data)
                .build();
    }
}

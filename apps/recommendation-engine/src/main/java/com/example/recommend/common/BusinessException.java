package com.example.recommend.common;

import lombok.Getter;

/**
 * 业务异常。
 * <p>
 * 所有业务层抛出的异常均使用此类，携带错误码枚举，由全局异常处理器统一转换为 Result 响应。
 * 禁止吞异常（catch 后必须记录日志或重新抛出）。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码枚举 */
    private final ErrorCode errorCode;

    /**
     * 基于错误码枚举构造业务异常。
     *
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 基于错误码枚举和自定义消息构造业务异常。
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误消息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 基于错误码枚举和原因构造业务异常。
     *
     * @param errorCode 错误码枚举
     * @param cause     原始异常
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}

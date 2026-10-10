package com.example.recommend.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 错误码枚举。
 * <p>
 * 架构文档 5.2 节错误码规范：6 位数字，前 2 位为模块，后 4 位为具体错误。
 * <ul>
 *   <li>10xxxx - 参数错误</li>
 *   <li>20xxxx - 鉴权错误</li>
 *   <li>30xxxx - 用户/物品/场景业务错误</li>
 *   <li>40xxxx - 推荐链路降级（HTTP 200，非错误）</li>
 *   <li>50xxxx - 系统/服务错误</li>
 *   <li>60xxxx - 限流/实验错误</li>
 * </ul>
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Getter
public enum ErrorCode {

    // ===== 成功 =====
    SUCCESS(0, "success", HttpStatus.OK),

    // ===== 10xxxx 参数错误 =====
    PARAM_INVALID(100001, "参数校验失败", HttpStatus.BAD_REQUEST),
    PARAM_MISSING(100002, "缺少必填参数", HttpStatus.BAD_REQUEST),
    PARAM_FORMAT_ERROR(100003, "参数格式错误", HttpStatus.BAD_REQUEST),

    // ===== 20xxxx 鉴权错误 =====
    UNAUTHORIZED(200001, "未授权，API Key 无效或缺失", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(200002, "无权限访问该资源", HttpStatus.FORBIDDEN),
    TOKEN_EXPIRED(200003, "令牌过期", HttpStatus.UNAUTHORIZED),

    // ===== 30xxxx 用户/物品/场景业务错误 =====
    USER_NOT_FOUND(300001, "用户不存在", HttpStatus.NOT_FOUND),
    ITEM_NOT_FOUND(300002, "物品不存在", HttpStatus.NOT_FOUND),
    SCENE_NOT_SUPPORTED(300003, "不支持的推荐场景", HttpStatus.BAD_REQUEST),

    // ===== 40xxxx 推荐链路降级（HTTP 200，非错误） =====
    RECALL_EMPTY(400001, "召回结果为空", HttpStatus.OK),
    RANK_DEGRADED(400002, "排序降级", HttpStatus.OK),
    EMBEDDING_SERVICE_UNAVAILABLE(400003, "Embedding 服务不可用，语义检索跳过", HttpStatus.OK),

    // ===== 50xxxx 系统/服务错误 =====
    INTERNAL_ERROR(500001, "服务内部错误", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(500002, "服务暂不可用", HttpStatus.SERVICE_UNAVAILABLE),
    MODEL_INFERENCE_TIMEOUT(500003, "模型推理超时", HttpStatus.GATEWAY_TIMEOUT),
    CACHE_SERVICE_ERROR(500004, "缓存服务异常", HttpStatus.SERVICE_UNAVAILABLE),
    DATABASE_ERROR(500005, "数据库操作失败", HttpStatus.INTERNAL_SERVER_ERROR),
    FEATURE_CONSISTENCY_FAILED(500006, "特征一致性校验失败，模型发布被阻断", HttpStatus.INTERNAL_SERVER_ERROR),

    // ===== 60xxxx 限流/实验错误 =====
    RATE_LIMITED(600001, "请求频率超限", HttpStatus.TOO_MANY_REQUESTS),
    EXPERIMENT_NOT_FOUND(600002, "实验不存在", HttpStatus.NOT_FOUND),
    EXPERIMENT_STATUS_INVALID(600003, "实验状态不允许该操作", HttpStatus.BAD_REQUEST);

    /** 业务状态码 */
    private final int code;

    /** 错误描述 */
    private final String message;

    /** 对应的 HTTP 状态码 */
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}

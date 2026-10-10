package com.example.recommend.common;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 * <p>
 * 捕获所有 Controller 层抛出的异常，统一转换为 Result 响应结构。
 * 架构文档 5.1 / 5.2 节统一响应与错误码规范。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     * <p>
     * 优先使用 BusinessException 的自定义 message（P-07 修复），
     * 仅当未设置自定义消息时回退到 ErrorCode 默认消息。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        String message = (e.getMessage() != null && !e.getMessage().isBlank())
                ? e.getMessage() : e.getErrorCode().getMessage();
        log.warn("业务异常: code={}, message={}", e.getErrorCode().getCode(), message);
        Result<Void> result = Result.fail(e.getErrorCode().getCode(), message);
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(result);
    }

    /**
     * 处理 @Valid 校验失败异常（@RequestBody）。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(ErrorCode.PARAM_INVALID.getCode(), message));
    }

    /**
     * 处理表单绑定校验异常。
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(ErrorCode.PARAM_INVALID.getCode(), message));
    }

    /**
     * 处理约束违反异常（@Validated @RequestParam）。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        log.warn("约束违反: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(ErrorCode.PARAM_INVALID.getCode(), message));
    }

    /**
     * 处理缺少必填请求参数异常。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少必填参数: {}", e.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(ErrorCode.PARAM_MISSING.getCode(),
                        "缺少必填参数: " + e.getParameterName()));
    }

    /**
     * 处理参数类型不匹配异常。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数格式错误: {}={}", e.getName(), e.getValue());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(ErrorCode.PARAM_FORMAT_ERROR.getCode(),
                        "参数格式错误: " + e.getName()));
    }

    /**
     * 处理 Resilience4j 限流异常。
     */
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<Result<Void>> handleRateLimit(RequestNotPermitted e) {
        log.warn("请求被限流: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Result.fail(ErrorCode.RATE_LIMITED));
    }

    /**
     * 处理资源不存在异常（P-03 修复）。
     * <p>
     * Spring Boot 3.x 对未匹配路径抛出 NoResourceFoundException，
     * 应返回 HTTP 404 而非落入兜底 500。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("资源不存在: {}", e.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.fail(ErrorCode.ITEM_NOT_FOUND.getCode(),
                        "请求的资源不存在: " + e.getResourcePath()));
    }

    /**
     * 处理请求体 JSON 解析失败异常（P-04 修复）。
     * <p>
     * 当 @RequestBody 收到格式错误的 JSON 时，Spring 抛出
     * HttpMessageNotReadableException，应返回 HTTP 400 而非 500。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(ErrorCode.PARAM_FORMAT_ERROR.getCode(),
                        "请求体 JSON 格式错误，请检查请求体内容"));
    }

    /**
     * 处理其他未捕获异常（兜底）。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("系统内部错误", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(ErrorCode.INTERNAL_ERROR));
    }
}

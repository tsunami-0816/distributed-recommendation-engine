package com.example.recommend.controller;

import com.example.recommend.common.BusinessException;
import com.example.recommend.common.ErrorCode;
import com.example.recommend.common.Result;
import com.example.recommend.dto.RecommendRequestDTO;
import com.example.recommend.dto.RecommendResponseDTO;
import com.example.recommend.service.RecommendationFacade;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 推荐接口 Controller。
 * <p>
 * 架构文档 5.4.1 节：GET /api/v1/recommend，支持 home / guess / related / search 四种场景。
 * 限流配置：推荐接口 1000 QPS（Resilience4j）。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/recommend")
@RequiredArgsConstructor
@Validated
@Tag(name = "推荐接口", description = "分布式智能推荐引擎 - 推荐主链路接口")
public class RecommendController {

    private final RecommendationFacade recommendationFacade;
    // 修复Bug-016：注入 RateLimiterRegistry，根据 scene 动态选择限流器
    // search 场景因 ES 查询开销大应使用独立的 search-api 限流器（500 QPS），
    // 其他场景使用 recommend-api 限流器（1000 QPS）
    private final RateLimiterRegistry rateLimiterRegistry;

    /**
     * 获取推荐结果。
     *
     * @param scene    场景：home / guess / related / search
     * @param userId   用户 ID
     * @param itemId   相关推荐的参考物品 ID（related 场景必填）
     * @param keyword  搜索关键词（search 场景必填）
     * @param pageSize 返回数量，默认 20，最大 50
     * @param device   设备类型，默认 web
     */
    @GetMapping
    @Operation(summary = "获取推荐结果", description = "支持首页推荐、猜你喜欢、相关推荐、搜索推荐四种场景")
    public Result<RecommendResponseDTO> recommend(
            @Parameter(description = "场景：home/guess/related/search", required = true)
            @RequestParam @NotBlank(message = "scene 不能为空") String scene,
            @Parameter(description = "用户 ID", required = true)
            @RequestParam @NotBlank(message = "userId 不能为空") String userId,
            @Parameter(description = "相关推荐的参考物品 ID（related 场景必填）")
            @RequestParam(required = false) String itemId,
            @Parameter(description = "搜索关键词（search 场景必填）")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "返回数量，默认 20，最大 50")
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer pageSize,
            @Parameter(description = "设备类型：mobile/pc/web")
            @RequestParam(defaultValue = "web") String device) {

        // 修复Bug-016：根据 scene 动态选择限流器
        // search 场景使用 search-api（500 QPS），其他场景使用 recommend-api（1000 QPS）
        // 原实现所有场景共用 @RateLimiter(name = "recommend-api")，search 场景可跑满 1000 QPS 导致 ES 压力过大
        String limiterName = "search".equalsIgnoreCase(scene) ? "search-api" : "recommend-api";
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(limiterName);

        RecommendRequestDTO request = RecommendRequestDTO.builder()
                .scene(scene)
                .userId(userId)
                .itemId(itemId)
                .keyword(keyword)
                .pageSize(pageSize)
                .device(device)
                .build();

        RecommendResponseDTO response;
        try {
            response = rateLimiter.executeCheckedSupplier(() -> recommendationFacade.recommend(request));
        } catch (RequestNotPermitted e) {
            // 修复Bug-016：限流触发时返回 600001 业务错误码
            log.warn("推荐接口限流触发: scene={}, userId={}, limiter={}", scene, userId, limiterName);
            throw new BusinessException(ErrorCode.RATE_LIMITED, "请求频率超限，请稍后重试");
        } catch (Throwable e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "推荐服务内部错误");
        }

        // P-06 修复：召回为空等降级场景返回 HTTP 200 + 业务降级码（如 400001），而非 code=0
        if (response.getDegradedCode() != null) {
            log.info("推荐降级响应: scene={}, userId={}, degradedCode={}",
                    request.getScene(), request.getUserId(), response.getDegradedCode());
            return Result.degraded(response.getDegradedCode(),
                    ErrorCode.RECALL_EMPTY.getMessage(), response);
        }

        return Result.success(response);
    }
}

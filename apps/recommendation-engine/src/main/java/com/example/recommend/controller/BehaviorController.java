package com.example.recommend.controller;

import com.example.recommend.common.Result;
import com.example.recommend.dto.BehaviorEventDTO;
import com.example.recommend.dto.BehaviorResponseDTO;
import com.example.recommend.service.behavior.BehaviorCollectorService;
import com.example.recommend.service.cache.RecommendCacheService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 行为采集 Controller。
 * <p>
 * 架构文档 5.4.2 节：POST /api/v1/behavior，异步写入 Kafka，立即返回 accepted=true。
 * 限流配置：行为采集 2000 QPS（Resilience4j）。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/behavior")
@RequiredArgsConstructor
@Tag(name = "行为采集接口", description = "用户行为事件采集（点击/浏览/收藏/曝光/转化）")
public class BehaviorController {

    private final BehaviorCollectorService behaviorCollectorService;
    private final RecommendCacheService recommendCacheService;

    /**
     * 采集行为事件。
     * <p>
     * 行为采集成功后，按架构文档 6.5 节约定失效该用户的推荐结果缓存，
     * 保证行为对推荐的实时影响（行为上报→缓存失效闭环）。
     *
     * @param event 行为事件（userId/itemId/actionType/scene/eventTime/extra）
     */
    @PostMapping
    @RateLimiter(name = "behavior-api")
    @Operation(summary = "采集行为事件", description = "异步写入 Kafka，立即返回。支持 click/view/favorite/impression/conversion")
    public Result<BehaviorResponseDTO> collect(@Valid @RequestBody BehaviorEventDTO event) {
        BehaviorResponseDTO response = behaviorCollectorService.collect(event);
        // 行为上报后失效该用户的推荐缓存（架构文档 6.5 节：行为触发失效）
        recommendCacheService.invalidateByUser(event.getUserId());
        return Result.success(response);
    }
}

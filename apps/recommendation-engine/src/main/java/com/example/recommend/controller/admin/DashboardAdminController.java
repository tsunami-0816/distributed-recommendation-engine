package com.example.recommend.controller.admin;

import com.example.recommend.common.Result;
import com.example.recommend.service.admin.AbExperimentService;
import com.example.recommend.service.admin.ModelManagementService;
import com.example.recommend.service.cache.RecommendCacheService;
import com.example.recommend.service.rank.MockRankService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 仪表盘管理 Controller（M18 / P-05 修复）。
 * <p>
 * 基础路径：/admin/dashboard
 * 提供系统概览统计数据汇总，从 Micrometer MeterRegistry 和各业务 Service 实时获取。
 * 前端 Dashboard.vue 调用此接口替代硬编码 Mock 数据。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "管理后台 - 仪表盘", description = "系统概览统计数据汇总（M18 / P-05）")
public class DashboardAdminController {

    private final MeterRegistry meterRegistry;
    private final RecommendCacheService recommendCacheService;
    private final AbExperimentService abExperimentService;
    private final ModelManagementService modelManagementService;
    // 修复Bug-005：注入 MockRankService 获取降级统计（RankService 接口未暴露统计方法，Mock 实现提供）
    private final MockRankService mockRankService;
    // 修复Bug-005：注入 HealthEndpoint 获取真实系统健康状态，替代硬编码 "UP"
    private final HealthEndpoint healthEndpoint;

    @GetMapping("/stats")
    @Operation(summary = "获取仪表盘统计数据", description = "汇总请求量、延迟、缓存命中率、活跃用户、实验/模型数等实时指标")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        // 修复Bug-005：服务运行时长（秒），用于 QPS 计算
        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        double uptimeSec = Math.max(1.0, uptimeSeconds);

        // 1. 推荐接口请求量（从 Micrometer Timer 获取）
        Timer recommendTimer = meterRegistry.find("http.server.requests")
                .tag("uri", "/api/v1/recommend")
                .timer();

        long totalRequests = 0;
        double avgLatencyMs = 0.0;
        double p99LatencyMs = 0.0;
        double recommendationQps = 0.0;

        if (recommendTimer != null) {
            totalRequests = recommendTimer.count();
            avgLatencyMs = recommendTimer.mean(TimeUnit.MILLISECONDS);
            // 修复Bug-005：P99 延迟使用 Timer.percentile(0.99) 获取真实分位数，而非 Timer.max() 历史最大值
            // 需要 application.yml 中配置 management.metrics.distribution.percentiles.http.server.requests
            p99LatencyMs = recommendTimer.percentile(0.99, TimeUnit.MILLISECONDS);
            if (Double.isNaN(p99LatencyMs) || Double.isInfinite(p99LatencyMs)) {
                p99LatencyMs = recommendTimer.max(TimeUnit.MILLISECONDS);
            }
            // 修复Bug-005：QPS = 总请求数 / 服务运行时长（秒），而非错误的 1/mean(seconds)
            recommendationQps = Math.round(totalRequests / uptimeSec * 100) / 100.0;
        }

        stats.put("totalRequests", totalRequests);
        stats.put("avgLatencyMs", Math.round(avgLatencyMs * 100) / 100.0);
        stats.put("p99LatencyMs", Math.round(p99LatencyMs));
        stats.put("recommendationQps", recommendationQps);

        // 2. 行为采集 QPS
        Timer behaviorTimer = meterRegistry.find("http.server.requests")
                .tag("uri", "/api/v1/behavior")
                .timer();
        long behaviorRequests = behaviorTimer != null ? behaviorTimer.count() : 0;
        // 修复Bug-005：行为 QPS = 行为请求数 / 服务运行时长，而非直接使用累计请求数 count()
        double behaviorQps = Math.round(behaviorRequests / uptimeSec * 100) / 100.0;
        stats.put("behaviorQps", behaviorQps);

        // 3. 缓存统计（从 RecommendCacheService 获取真实数据）
        Map<String, Object> cacheStats = recommendCacheService.getStats();
        int cacheSize = (int) cacheStats.getOrDefault("cacheSize", 0);
        int trackedUsers = (int) cacheStats.getOrDefault("trackedUsers", 0);
        // 修复Bug-005：缓存命中率 = 缓存命中次数 / 缓存查询总次数，而非缓存条目数 / 总请求数
        long cacheTotalCount = ((Number) cacheStats.getOrDefault("cacheTotalCount", 0L)).longValue();
        long cacheHitCount = ((Number) cacheStats.getOrDefault("cacheHitCount", 0L)).longValue();
        double cacheHitRate = cacheTotalCount > 0
                ? Math.round((double) cacheHitCount / cacheTotalCount * 1000) / 1000.0
                : 0.0;
        cacheHitRate = Math.min(Math.max(cacheHitRate, 0.0), 1.0);

        stats.put("cacheHitRate", cacheHitRate);
        stats.put("cacheSize", cacheSize);
        stats.put("activeUsers", trackedUsers);

        // 4. 活跃实验数（从 AbExperimentService 获取真实数据）
        int activeExperiments = abExperimentService.listExperiments("running").size();
        stats.put("activeExperiments", activeExperiments);

        // 5. 活跃模型数（从 ModelManagementService 获取真实数据）
        long activeModels = modelManagementService.listModels(null).stream()
                .filter(m -> "active".equalsIgnoreCase(m.getStatus()))
                .count();
        stats.put("activeModels", activeModels);

        // 6. 修复Bug-005：模型降级率从 MockRankService 计数器获取，而非硬编码 0.003
        Map<String, Long> rankStats = mockRankService.getRankStats();
        long totalRankCount = rankStats.getOrDefault("totalRankCount", 0L);
        long degradeCount = rankStats.getOrDefault("degradeCount", 0L);
        double modelDegradationRate = totalRankCount > 0
                ? Math.round((double) degradeCount / totalRankCount * 1000) / 1000.0
                : 0.0;
        stats.put("modelDegradationRate", modelDegradationRate);

        // 7. 修复Bug-005：系统健康状态从 HealthEndpoint 获取真实状态，而非硬编码 "UP"
        String systemHealth = healthEndpoint.health().getStatus().getCode();
        stats.put("systemHealth", systemHealth);

        log.debug("仪表盘统计: totalRequests={}, avgLatency={}ms, p99={}ms, cacheHitRate={}, " +
                        "recommendationQps={}, behaviorQps={}, degradationRate={}, health={}",
                totalRequests, avgLatencyMs, p99LatencyMs, cacheHitRate,
                recommendationQps, behaviorQps, modelDegradationRate, systemHealth);

        return Result.success(stats);
    }
}

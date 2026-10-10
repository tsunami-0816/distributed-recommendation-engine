package com.example.recommend.controller.admin;

import com.example.recommend.common.BusinessException;
import com.example.recommend.common.ErrorCode;
import com.example.recommend.common.Result;
import com.example.recommend.dto.admin.CacheRefreshRequestDTO;
import com.example.recommend.service.cache.RecommendCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 缓存管理 Controller（M11-T04）。
 * <p>
 * 基础路径：/admin/cache
 * 提供缓存刷新（按 Key 前缀批量失效 / 指定具体 Key 删除）。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/cache")
@RequiredArgsConstructor
@Tag(name = "管理后台 - 缓存管理", description = "缓存查询/失效/刷新（M11-T04）")
public class CacheAdminController {

    private final RecommendCacheService recommendCacheService;

    @PostMapping("/refresh")
    @Operation(summary = "刷新缓存", description = "支持按 Key 前缀批量失效（prefix）或指定具体 Key 删除（key），二者二选一")
    public Result<Map<String, Object>> refreshCache(@RequestBody CacheRefreshRequestDTO request) {
        int invalidatedCount;

        if (request.getPrefix() != null && !request.getPrefix().isBlank()) {
            invalidatedCount = recommendCacheService.invalidateByPrefix(request.getPrefix());
            log.info("管理后台按前缀刷新缓存: prefix={}, count={}", request.getPrefix(), invalidatedCount);
        } else if (request.getKey() != null && !request.getKey().isBlank()) {
            // 修复Bug-011：单 Key 失效使用精确匹配 cache.remove(key)，而非前缀匹配
            // 原实现复用 invalidateByPrefix(request.getKey())，将完整 key 作为前缀匹配，
            // 会误删所有以该字符串开头的缓存（例如 key 含 user1 时误删 user10、user11 的缓存）
            invalidatedCount = recommendCacheService.invalidateByKey(request.getKey());
            log.info("管理后台按 Key 刷新缓存(精确匹配): key={}, count={}", request.getKey(), invalidatedCount);
        } else {
            throw new BusinessException(ErrorCode.PARAM_MISSING, "必须指定 prefix 或 key 参数");
        }

        Map<String, Object> stats = recommendCacheService.getStats();
        return Result.success(Map.of(
                "invalidatedCount", invalidatedCount,
                "remainingCacheSize", stats.get("cacheSize"),
                "trackedUsers", stats.get("trackedUsers")
        ));
    }
}

package com.example.recommend.service.cache;

import com.example.recommend.config.RecommendProperties;
import com.example.recommend.dto.RecommendResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 推荐结果缓存服务。
 * <p>
 * 架构文档 M09-T03 / 6.5 节：分层 TTL（高频 10s / 普通 60s / 兜底 120s），
 * TTL 加随机偏移 ±10s 防雪崩，行为上报触发高活跃用户缓存失效。
 * 缓存 Key：rec:result:{scene}:{userId}:{abGroup}:{modelVersion}
 * <p>
 * 本实现使用内存缓存（ConcurrentHashMap + 过期时间戳），未安装 Redis 时也可运行。
 * 生产环境由 M03 模块的 RedisService 替换。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendCacheService {

    private final RecommendProperties properties;

    /** 内存缓存：Key -> (值, 过期时间戳) */
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /** 用户访问计数：userId -> (计数, 窗口开始时间)，用于高频用户判定 */
    private final Map<String, AccessWindow> accessCounter = new ConcurrentHashMap<>();

    // 修复Bug-009：维护 userId -> 该用户所有缓存 Key 的反向索引，失效时只遍历该用户的 Key，避免全表遍历
    private final Map<String, Set<String>> userKeyIndex = new ConcurrentHashMap<>();

    // 修复Bug-005：缓存查询总次数和命中次数计数器，用于仪表盘缓存命中率计算
    private final AtomicLong cacheTotalCount = new AtomicLong(0);
    private final AtomicLong cacheHitCount = new AtomicLong(0);

    /**
     * 缓存条目。
     */
    private record CacheEntry(RecommendResponseDTO value, long expireAt) {}

    /**
     * 用户访问窗口。
     */
    private static class AccessWindow {
        final AtomicInteger count = new AtomicInteger(0);
        volatile long windowStart = System.currentTimeMillis();
    }

    /**
     * 构建缓存 Key。
     *
     * @param scene        场景
     * @param userId       用户 ID
     * @param abGroup      A/B 实验组
     * @param modelVersion 模型版本
     */
    public String buildKey(String scene, String userId, String abGroup, String modelVersion) {
        return String.format("rec:result:%s:%s:%s:%s", scene, userId, abGroup, modelVersion);
    }

    /**
     * 从缓存获取推荐结果。
     *
     * @param key 缓存 Key
     * @return 缓存命中返回结果，未命中或已过期返回 null
     */
    public RecommendResponseDTO get(String key) {
        // 修复Bug-005：缓存查询总次数 +1
        cacheTotalCount.incrementAndGet();
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (System.currentTimeMillis() > entry.expireAt()) {
            cache.remove(key);
            // 修复Bug-009：过期删除时同步清理反向索引
            removeFromUserIndex(key);
            return null;
        }
        // 修复Bug-005：缓存命中次数 +1
        cacheHitCount.incrementAndGet();
        log.debug("缓存命中: key={}", key);
        return entry.value();
    }

    /**
     * 写入推荐结果缓存，根据用户活跃度自动选择分层 TTL。
     *
     * @param key    缓存 Key
     * @param value  推荐结果
     * @param userId 用户 ID（用于判定活跃度）
     */
    public void put(String key, RecommendResponseDTO value, String userId) {
        int ttl = determineTtl(userId);
        // TTL 加随机偏移 ±offset 防雪崩（架构文档 6.5 节）
        int offset = properties.getCache().getTtlRandomOffset();
        int actualTtl = ttl + (int) (Math.random() * 2 * offset) - offset;
        long expireAt = System.currentTimeMillis() + actualTtl * 1000L;

        cache.put(key, new CacheEntry(value, expireAt));

        // 修复Bug-009：写入缓存时同步维护 userId -> cacheKey 反向索引，用于按用户快速失效
        if (userId != null && !userId.isBlank()) {
            userKeyIndex.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(key);
        }

        log.debug("缓存写入: key={}, ttl={}s(实际{}s), userId={}", key, ttl, actualTtl, userId);
    }

    /**
     * 失效指定用户的推荐结果缓存（行为上报时触发，保证实时性）。
     * <p>
     * 修复Bug-009：使用 userId -> cacheKeys 反向索引，只遍历该用户的缓存 Key，
     * 时间复杂度从 O(缓存总量) 降为 O(该用户缓存数)，避免高并发行为上报时全表遍历成为性能瓶颈。
     *
     * @param userId 用户 ID
     */
    public void invalidateByUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        // 修复Bug-009：从反向索引获取该用户的所有缓存 Key，逐个删除
        Set<String> userKeys = userKeyIndex.remove(userId);
        if (userKeys != null && !userKeys.isEmpty()) {
            for (String key : userKeys) {
                cache.remove(key);
            }
            log.debug("按用户失效缓存: userId={}, invalidatedCount={}", userId, userKeys.size());
        } else {
            log.debug("按用户失效缓存: userId={}, 无缓存记录", userId);
        }
    }

    /**
     * 按 Key 前缀批量失效（管理后台缓存刷新接口使用）。
     *
     * @param prefix Key 前缀
     * @return 失效的 Key 数量
     */
    public int invalidateByPrefix(String prefix) {
        int count = 0;
        for (String key : cache.keySet()) {
            if (key.startsWith(prefix)) {
                cache.remove(key);
                // 修复Bug-009：前缀删除时同步清理反向索引
                removeFromUserIndex(key);
                count++;
            }
        }
        log.info("按前缀批量失效缓存: prefix={}, count={}", prefix, count);
        return count;
    }

    /**
     * 修复Bug-011：按精确 Key 单条失效（管理后台单 Key 刷新使用）。
     * <p>
     * 使用精确匹配 cache.remove(key)，而非前缀匹配，避免误删其他用户缓存
     * （例如 key 以 user1 开头时，前缀匹配会误删 user10、user11 等用户的缓存）。
     *
     * @param key 完整缓存 Key
     * @return 失效的 Key 数量（1 表示删除成功，0 表示 Key 不存在）
     */
    public int invalidateByKey(String key) {
        boolean removed = cache.remove(key) != null;
        if (removed) {
            // 修复Bug-009：单 Key 删除时同步清理反向索引
            removeFromUserIndex(key);
        }
        log.info("按精确 Key 失效缓存: key={}, removed={}", key, removed);
        return removed ? 1 : 0;
    }

    /**
     * 修复Bug-009：从所有用户的反向索引中移除指定 Key。
     * <p>
     * 缓存过期或被删除时调用，保持反向索引与实际缓存一致。
     *
     * @param key 被删除的缓存 Key
     */
    private void removeFromUserIndex(String key) {
        for (Map.Entry<String, Set<String>> entry : userKeyIndex.entrySet()) {
            entry.getValue().remove(key);
            if (entry.getValue().isEmpty()) {
                userKeyIndex.remove(entry.getKey());
            }
        }
    }

    /**
     * 记录用户访问，用于高频用户判定。
     *
     * @param userId 用户 ID
     */
    public void recordAccess(String userId) {
        AccessWindow window = accessCounter.computeIfAbsent(userId, k -> new AccessWindow());
        long now = System.currentTimeMillis();
        // 窗口过期则重置
        if (now - window.windowStart > properties.getCache().getHighFrequencyWindowSeconds() * 1000L) {
            window.count.set(0);
            window.windowStart = now;
        }
        window.count.incrementAndGet();
    }

    /**
     * 根据用户活跃度确定 TTL（架构文档 6.5 节分层 TTL）。
     * <ul>
     *   <li>高频用户（近 5 分钟访问 > 阈值）：10s（对实时特征友好）</li>
     *   <li>普通用户：60s</li>
     *   <li>兜底（无行为数据）：120s</li>
     * </ul>
     */
    private int determineTtl(String userId) {
        AccessWindow window = accessCounter.get(userId);
        if (window != null && window.count.get() > properties.getCache().getHighFrequencyThreshold()) {
            return properties.getCache().getHighFrequencyTtl();
        }
        // 简化判定：有访问记录的为普通用户，无记录的为兜底
        if (window != null && window.count.get() > 0) {
            return properties.getCache().getNormalTtl();
        }
        return properties.getCache().getFallbackTtl();
    }

    /**
     * 获取缓存统计信息（用于监控/调试）。
     * <p>
     * 修复Bug-005：新增 cacheTotalCount / cacheHitCount 字段，供仪表盘计算真实缓存命中率。
     */
    public Map<String, Object> getStats() {
        return Map.of(
                "cacheSize", cache.size(),
                "trackedUsers", accessCounter.size(),
                "cacheTotalCount", cacheTotalCount.get(),
                "cacheHitCount", cacheHitCount.get()
        );
    }
}

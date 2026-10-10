package com.example.recommend.service.cache;

import com.example.recommend.config.RecommendProperties;
import com.example.recommend.dto.RecommendResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 修复Bug-010：RecommendCacheService 单元测试。
 * <p>
 * 覆盖缓存读写、按用户失效（反向索引）、单 Key 精确删除、缓存命中率统计等核心逻辑。
 *
 * @author recommend-team
 * @since 1.0.0
 */
class RecommendCacheServiceTest {

    private RecommendCacheService cacheService;
    private RecommendProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RecommendProperties();
        cacheService = new RecommendCacheService(properties);
    }

    @Test
    @DisplayName("修复Bug-009：按用户失效缓存使用反向索引，只删除该用户的缓存，不影响其他用户")
    void invalidateByUser_shouldOnlyInvalidateSpecifiedUserCache() {
        // 准备：写入 user1 和 user2 的缓存
        String user1Key = cacheService.buildKey("home", "user1", "control", "deepfm_v1");
        String user2Key = cacheService.buildKey("home", "user2", "control", "deepfm_v1");
        RecommendResponseDTO response = buildResponse();

        cacheService.put(user1Key, response, "user1");
        cacheService.put(user2Key, response, "user2");

        // 验证：两个缓存都存在
        assertNotNull(cacheService.get(user1Key));
        assertNotNull(cacheService.get(user2Key));

        // 执行：失效 user1 的缓存
        cacheService.invalidateByUser("user1");

        // 验证：user1 缓存被删除，user2 缓存不受影响
        assertNull(cacheService.get(user1Key), "user1 的缓存应被失效");
        assertNotNull(cacheService.get(user2Key), "user2 的缓存不应受影响");
    }

    @Test
    @DisplayName("修复Bug-009：按用户失效空用户ID不抛异常")
    void invalidateByUser_withNullUserId_shouldNotThrow() {
        assertDoesNotThrow(() -> cacheService.invalidateByUser(null));
        assertDoesNotThrow(() -> cacheService.invalidateByUser(""));
    }

    @Test
    @DisplayName("修复Bug-011：单 Key 精确删除，不误删其他前缀匹配的缓存")
    void invalidateByKey_shouldOnlyRemoveExactKey() {
        // 准备：写入 user1 和 user10 的缓存（user1 是 user10 的前缀）
        String user1Key = cacheService.buildKey("home", "user1", "control", "deepfm_v1");
        String user10Key = cacheService.buildKey("home", "user10", "control", "deepfm_v1");
        RecommendResponseDTO response = buildResponse();

        cacheService.put(user1Key, response, "user1");
        cacheService.put(user10Key, response, "user10");

        // 执行：精确删除 user1 的缓存
        int removed = cacheService.invalidateByKey(user1Key);

        // 验证：只删除 user1，user10 不受影响
        assertEquals(1, removed, "应删除 1 条缓存");
        assertNull(cacheService.get(user1Key), "user1 的缓存应被删除");
        assertNotNull(cacheService.get(user10Key), "user10 的缓存不应被误删");
    }

    @Test
    @DisplayName("修复Bug-011：删除不存在的 Key 返回 0")
    void invalidateByKey_withNonExistentKey_shouldReturnZero() {
        int removed = cacheService.invalidateByKey("nonexistent:key");
        assertEquals(0, removed);
    }

    @Test
    @DisplayName("修复Bug-005：缓存命中率统计正确，命中次数和总次数准确")
    void getStats_shouldReturnCorrectHitRateCounters() {
        String key = cacheService.buildKey("home", "user1", "control", "deepfm_v1");
        RecommendResponseDTO response = buildResponse();

        // 写入缓存
        cacheService.put(key, response, "user1");

        // 查询 3 次：前 2 次命中，第 3 次查询不存在的 key 不命中
        cacheService.get(key);
        cacheService.get(key);
        cacheService.get("nonexistent");

        Map<String, Object> stats = cacheService.getStats();
        long totalCount = ((Number) stats.get("cacheTotalCount")).longValue();
        long hitCount = ((Number) stats.get("cacheHitCount")).longValue();

        assertEquals(3, totalCount, "总查询次数应为 3");
        assertEquals(2, hitCount, "命中次数应为 2");
        assertEquals(1, stats.get("cacheSize"), "缓存大小应为 1");
    }

    @Test
    @DisplayName("缓存写入和读取正常工作")
    void putAndGet_shouldStoreAndRetrieveValue() {
        String key = "test:key";
        RecommendResponseDTO response = buildResponse();

        cacheService.put(key, response, "user1");
        RecommendResponseDTO retrieved = cacheService.get(key);

        assertNotNull(retrieved);
        assertEquals(response.getScene(), retrieved.getScene());
    }

    @Test
    @DisplayName("按前缀批量删除正常工作")
    void invalidateByPrefix_shouldRemoveAllMatchingKeys() {
        cacheService.put("rec:result:home:user1:control:v1", buildResponse(), "user1");
        cacheService.put("rec:result:home:user2:control:v1", buildResponse(), "user2");
        cacheService.put("other:key", buildResponse(), "user3");

        int count = cacheService.invalidateByPrefix("rec:result:home");

        assertEquals(2, count, "应删除 2 条前缀匹配的缓存");
        assertNull(cacheService.get("rec:result:home:user1:control:v1"));
        assertNotNull(cacheService.get("other:key"), "非前缀匹配的缓存不应被删除");
    }

    private RecommendResponseDTO buildResponse() {
        return RecommendResponseDTO.builder()
                .scene("home")
                .abGroup("control")
                .modelVersion("deepfm_v1")
                .items(java.util.List.of())
                .candidateCount(0)
                .duration(RecommendResponseDTO.DurationDetail.builder().totalMs(10).build())
                .build();
    }
}

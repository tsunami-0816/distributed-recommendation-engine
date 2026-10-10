package com.example.recommend.controller;

import com.example.recommend.dto.HotItemDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 修复Bug-010：HotItemsController 集成测试。
 * <p>
 * 覆盖热门榜单热度分非负（修复Bug-006）、limit 参数下限校验（修复Bug-007）等核心逻辑。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HotItemsControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("修复Bug-006：limit=100 时所有物品的 hotScore、clickCount、favoriteCount 均非负")
    void getHotItems_withLimit100_allScoresShouldBeNonNegative() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/hot/items?limit=100",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().get("code"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("data");
        assertEquals(100, items.size());

        // 修复Bug-006：验证所有物品的 hotScore >= 0，clickCount >= 0，favoriteCount >= 0
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            double hotScore = ((Number) item.get("hotScore")).doubleValue();
            long clickCount = ((Number) item.get("clickCount")).longValue();
            long favoriteCount = ((Number) item.get("favoriteCount")).longValue();

            assertTrue(hotScore >= 0,
                    "第 " + (i + 1) + " 个物品 hotScore 应为非负，实际: " + hotScore);
            assertTrue(clickCount >= 0,
                    "第 " + (i + 1) + " 个物品 clickCount 应为非负，实际: " + clickCount);
            assertTrue(favoriteCount >= 0,
                    "第 " + (i + 1) + " 个物品 favoriteCount 应为非负，实际: " + favoriteCount);
        }
    }

    @Test
    @DisplayName("修复Bug-007：limit=0 应返回参数校验错误（code=100001）")
    void getHotItems_withLimitZero_shouldReturnValidationError() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/hot/items?limit=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});

        // 修复Bug-007：limit=0 应触发 @Min(1) 校验，返回 400 Bad Request
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100001, response.getBody().get("code"),
                "limit=0 应返回 100001 参数校验失败");
    }

    @Test
    @DisplayName("修复Bug-007：limit 为负数应返回参数校验错误")
    void getHotItems_withNegativeLimit_shouldReturnValidationError() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/hot/items?limit=-1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100001, response.getBody().get("code"),
                "limit=-1 应返回 100001 参数校验失败");
    }

    @Test
    @DisplayName("默认 limit=20 返回 20 条数据")
    void getHotItems_withDefaultLimit_shouldReturn20Items() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/hot/items",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("data");
        assertEquals(20, items.size());
    }

    @Test
    @DisplayName("按类目筛选返回该类目的物品")
    void getHotItems_withCategoryId_shouldReturnFilteredItems() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/hot/items?categoryId=101&limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("data");
        assertEquals(10, items.size());

        // 验证所有物品的 categoryId 都是 101
        for (Map<String, Object> item : items) {
            @SuppressWarnings("unchecked")
            Map<String, Object> itemInfo = (Map<String, Object>) item.get("item");
            assertEquals(101, itemInfo.get("categoryId"));
        }
    }

    @Test
    @DisplayName("热门榜单物品按 rank 升序排列")
    void getHotItems_itemsShouldBeSortedByRank() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/hot/items?limit=20",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("data");

        for (int i = 0; i < items.size(); i++) {
            assertEquals(i + 1, items.get(i).get("rank"),
                    "第 " + i + " 个物品的 rank 应为 " + (i + 1));
        }
    }
}

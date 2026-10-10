package com.example.recommend.controller;

import com.example.recommend.common.Result;
import com.example.recommend.dto.HotItemDTO;
import com.example.recommend.dto.ItemInfoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * 热门物品 Controller。
 * <p>
 * 架构文档 5.4.4 节：GET /api/v1/hot/items，按类目或全局获取热门物品榜单。
 * 本实现为 Mock 版本，实际由 M13 Spark 离线任务计算热度分并写入 Redis ZSET。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/hot")
@RequiredArgsConstructor
@Validated
@Tag(name = "热门物品接口", description = "热门物品榜单查询（全局/按类目）")
public class HotItemsController {

    private static final int[] CATEGORY_POOL = {101, 102, 103, 201, 202, 301, 302, 401};

    /**
     * 获取热门物品榜单。
     *
     * @param categoryId 类目 ID（可选，不传则返回全局热门）
     * @param limit      返回数量，默认 20，最大 100
     */
    @GetMapping("/items")
    @Operation(summary = "获取热门物品榜单", description = "按类目或全局获取热门物品，热度分=click×0.7+favorite×0.3")
    public Result<List<HotItemDTO>> getHotItems(
            @Parameter(description = "类目 ID（可选，不传则返回全局热门）")
            @RequestParam(required = false) Integer categoryId,
            // 修复Bug-007：limit 参数添加 @Min(1) 校验，非正数返回 100001 参数校验失败，与推荐接口 pageSize 校验标准一致
            @Parameter(description = "返回数量，默认 20，最大 100")
            @RequestParam(defaultValue = "20") @Min(1) Integer limit) {

        int actualLimit = Math.min(limit, 100);
        Random random = new Random(categoryId != null ? categoryId : 42);

        List<HotItemDTO> items = IntStream.range(0, actualLimit)
                .mapToObj(i -> {
                    String itemId = "i_" + (10000 + random.nextInt(90000));
                    int cat = categoryId != null ? categoryId : CATEGORY_POOL[random.nextInt(CATEGORY_POOL.length)];
                    // 修复Bug-006：热度分添加 Math.max(0, ...) 下限截断，避免 limit=100 时尾部物品 hotScore 为负数
                    // 原公式 1.0 - i*0.01 - random*0.02 在 i=99 时基础值仅 0.01，减去随机数后可能为负
                    double hotScore = Math.max(0.0, 1.0 - i * 0.01 - random.nextDouble() * 0.02);
                    long clickCount = (long) (10000 * hotScore);
                    long favoriteCount = (long) (3000 * hotScore);

                    return HotItemDTO.builder()
                            .itemId(itemId)
                            .rank(i + 1)
                            .hotScore(Math.round(hotScore * 10000) / 10000.0)
                            .clickCount(clickCount)
                            .favoriteCount(favoriteCount)
                            .item(ItemInfoDTO.builder()
                                    .itemId(itemId)
                                    .name("热门商品-" + itemId.substring(2))
                                    .categoryId(cat)
                                    .price(10.0 + random.nextInt(990))
                                    .status(1)
                                    .hotScore(hotScore)
                                    .build())
                            .build();
                })
                .toList();

        log.debug("热门物品查询: categoryId={}, limit={}, result={}", categoryId, limit, items.size());
        return Result.success(items);
    }
}

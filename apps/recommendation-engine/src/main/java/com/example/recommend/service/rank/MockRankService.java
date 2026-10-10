package com.example.recommend.service.rank;

import com.example.recommend.dto.ItemInfoDTO;
import com.example.recommend.service.recall.CandidateItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 排序服务 Mock 实现。
 * <p>
 * 模拟 M07 排序服务的行为：基于召回分数生成模拟预测分数，执行类目打散和长尾加权，
 * 供 M09 推荐场景编排联调使用。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@Service
public class MockRankService implements RankService {

    // 修复Bug-005：排序总次数和降级次数计数器，供仪表盘计算模型降级率
    private final AtomicLong totalRankCount = new AtomicLong(0);
    private final AtomicLong degradeCount = new AtomicLong(0);

    @Override
    public RankResult rank(List<CandidateItem> candidates, String userId, int pageSize) {
        // 修复Bug-005：排序总次数 +1
        totalRankCount.incrementAndGet();

        if (candidates == null || candidates.isEmpty()) {
            // 修复Bug-005：召回为空时使用规则降级，降级次数 +1
            degradeCount.incrementAndGet();
            return new RankResult("rule", true, List.of());
        }

        // 模拟模型推理：基于召回分数 + 随机扰动生成预测分数（模拟 DeepFM 输出）
        // P-08 修复：分数截断到 [0, 1]，真实模型推理分数应在此区间
        Random random = new Random(userId.hashCode());
        List<RankedItem> ranked = candidates.stream()
                .map(c -> new RankedItem(
                        c.getItemId(),
                        // 预测分数 = 召回分数 * 0.6 + 随机分 * 0.4，模拟模型推理
                        Math.min(c.getRecallScore() * 0.6 + random.nextDouble() * 0.4, 1.0),
                        c.getItemInfo()
                ))
                .sorted(Comparator.comparingDouble(RankedItem::score).reversed())
                .collect(Collectors.toList());

        // 模拟长尾加权：热度后 50% 物品分数 × 1.1（架构文档 M07-T05）
        double medianHotScore = ranked.stream()
                .mapToDouble(r -> r.itemInfo() != null && r.itemInfo().getHotScore() != null
                        ? r.itemInfo().getHotScore() : 0.5)
                .sorted()
                .skip(ranked.size() / 2)
                .findFirst()
                .orElse(0.5);

        List<RankedItem> longtailBoosted = ranked.stream()
                .map(r -> {
                    double hot = r.itemInfo() != null && r.itemInfo().getHotScore() != null
                            ? r.itemInfo().getHotScore() : 0.5;
                    if (hot < medianHotScore) {
                        // P-08 修复：长尾加权后仍截断到 [0, 1]
                        return new RankedItem(r.itemId(), Math.min(r.score() * 1.1, 1.0), r.itemInfo());
                    }
                    return r;
                })
                .sorted(Comparator.comparingDouble(RankedItem::score).reversed())
                .collect(Collectors.toList());

        // 模拟类目打散：相邻物品不同类目，最多尝试 3 次（架构文档 M07-T05）
        List<RankedItem> diversified = diversifyByCategory(longtailBoosted, 3);

        // 截断至 pageSize
        List<RankedItem> result = diversified.stream()
                .limit(pageSize)
                .collect(Collectors.toList());

        log.debug("排序完成: userId={}, candidates={}, result={}, model=deepfm_v1",
                userId, candidates.size(), result.size());

        return new RankResult("deepfm_v1", false, result);
    }

    /**
     * 修复Bug-005：获取排序统计信息（总次数 + 降级次数），供仪表盘计算模型降级率。
     *
     * @return 包含 totalRankCount 和 degradeCount 的 Map
     */
    public java.util.Map<String, Long> getRankStats() {
        return java.util.Map.of(
                "totalRankCount", totalRankCount.get(),
                "degradeCount", degradeCount.get()
        );
    }

    /**
     * 类目打散：尽量保证相邻物品类目不同。
     *
     * @param items       排序后的物品列表
     * @param maxAttempts 每个位置最大尝试次数
     */
    private List<RankedItem> diversifyByCategory(List<RankedItem> items, int maxAttempts) {
        List<RankedItem> result = new ArrayList<>();
        Set<Integer> usedIndexes = new HashSet<>();
        Integer lastCategory = null;

        for (int i = 0; i < items.size(); i++) {
            RankedItem selected = null;
            int attempts = 0;

            // 尝试找一个与上一个类目不同的物品
            for (int j = 0; j < items.size() && attempts < maxAttempts; j++) {
                if (usedIndexes.contains(j)) continue;
                RankedItem candidate = items.get(j);
                Integer cat = candidate.itemInfo() != null ? candidate.itemInfo().getCategoryId() : null;

                if (lastCategory == null || !lastCategory.equals(cat) || attempts == maxAttempts - 1) {
                    selected = candidate;
                    usedIndexes.add(j);
                    lastCategory = cat;
                    break;
                }
                attempts++;
            }

            // 如果没找到（剩余物品都是同类目），直接按顺序取
            if (selected == null) {
                for (int j = 0; j < items.size(); j++) {
                    if (!usedIndexes.contains(j)) {
                        selected = items.get(j);
                        usedIndexes.add(j);
                        lastCategory = selected.itemInfo() != null
                                ? selected.itemInfo().getCategoryId() : null;
                        break;
                    }
                }
            }

            if (selected != null) {
                result.add(selected);
            }
        }

        return result;
    }
}

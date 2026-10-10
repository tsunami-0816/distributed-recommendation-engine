package com.example.recommend.service.recall;

import com.example.recommend.dto.ItemInfoDTO;
import com.example.recommend.dto.RecommendRequestDTO;
import com.example.recommend.dto.RecallChannel;
import com.example.recommend.dto.Scene;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 召回服务 Mock 实现。
 * <p>
 * 模拟 M06 召回服务的行为，根据场景生成模拟候选物品，供 M09 推荐场景编排联调使用。
 * 实际生产环境由 M06 模块替换此实现。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@Service
public class MockRecallService implements RecallService {

    /** 模拟物品类目池 */
    private static final int[] CATEGORY_POOL = {101, 102, 103, 201, 202, 301, 302, 401};

    /** 模拟标签池 */
    private static final String[] TAG_POOL = {"新品", "热销", "好评", "限量", "精选", "推荐", "特惠", "品质"};

    @Override
    public List<CandidateItem> recall(RecommendRequestDTO request) {
        Scene scene = Scene.fromCode(request.getScene());
        // 根据场景选择召回通道组合（架构文档 M06-T08）
        Set<RecallChannel> channels = getChannelsForScene(scene);

        // 模拟每路召回生成候选，合并去重后取 250 个
        List<CandidateItem> candidates = new ArrayList<>();
        int baseId = Math.abs(request.getUserId().hashCode()) % 10000;

        for (RecallChannel channel : channels) {
            int count = 80 + new Random().nextInt(40); // 每路 80~120 候选
            for (int i = 0; i < count; i++) {
                String itemId = "i_" + (10000 + ((baseId + i * 7 + channel.ordinal() * 13) % 90000));
                double score = 0.3 + new Random().nextDouble() * 0.7;
                candidates.add(buildCandidate(itemId, score, channel));
            }
        }

        // 按 itemId 去重，合并召回通道，保留最高分
        Map<String, CandidateItem> merged = new LinkedHashMap<>();
        for (CandidateItem c : candidates) {
            merged.merge(c.getItemId(), c, (old, newOne) -> {
                Set<RecallChannel> allChannels = new HashSet<>(old.getRecallChannels());
                allChannels.addAll(newOne.getRecallChannels());
                old.setRecallChannels(allChannels);
                old.setRecallScore(Math.max(old.getRecallScore(), newOne.getRecallScore()));
                return old;
            });
        }

        // 按召回分数降序，截断至 250 个
        List<CandidateItem> result = merged.values().stream()
                .sorted(Comparator.comparingDouble(CandidateItem::getRecallScore).reversed())
                .limit(250)
                .collect(Collectors.toList());

        log.debug("召回完成: scene={}, userId={}, channels={}, candidates={}",
                scene, request.getUserId(), channels, result.size());
        return result;
    }

    /**
     * 根据场景选择召回通道组合（架构文档 M06-T08）。
     */
    private Set<RecallChannel> getChannelsForScene(Scene scene) {
        return switch (scene) {
            case HOME -> EnumSet.of(RecallChannel.ITEM_CF, RecallChannel.TAG,
                    RecallChannel.PROFILE, RecallChannel.HOT);
            case GUESS -> EnumSet.of(RecallChannel.ITEM_CF, RecallChannel.TAG,
                    RecallChannel.PROFILE, RecallChannel.HOT);
            case RELATED -> EnumSet.of(RecallChannel.ITEM_CF, RecallChannel.TAG,
                    RecallChannel.ES_TEXT);
            case SEARCH -> EnumSet.of(RecallChannel.ES_TEXT, RecallChannel.ES_SEMANTIC,
                    RecallChannel.HOT);
        };
    }

    /**
     * 构建模拟候选物品。
     */
    private CandidateItem buildCandidate(String itemId, double score, RecallChannel channel) {
        return CandidateItem.builder()
                .itemId(itemId)
                .recallScore(score)
                .recallChannels(EnumSet.of(channel))
                .itemInfo(buildMockItemInfo(itemId))
                .build();
    }

    /**
     * 构建模拟物品详情。
     */
    private ItemInfoDTO buildMockItemInfo(String itemId) {
        int idx = Math.abs(itemId.hashCode());
        int categoryId = CATEGORY_POOL[idx % CATEGORY_POOL.length];
        List<String> tags = IntStream.range(0, 2 + idx % 3)
                .mapToObj(i -> TAG_POOL[(idx + i) % TAG_POOL.length])
                .distinct()
                .collect(Collectors.toList());

        return ItemInfoDTO.builder()
                .itemId(itemId)
                .name("模拟商品-" + itemId.substring(2))
                .description("这是一个用于联调的模拟商品，类目ID=" + categoryId)
                .categoryId(categoryId)
                .categoryPath("模拟类目/cat_" + categoryId)
                .price(10.0 + (idx % 990))
                .tags(tags)
                .status(1)
                .hotScore(0.1 + (idx % 90) / 100.0)
                .build();
    }
}

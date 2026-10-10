package com.example.recommend.service.profile;

import com.example.recommend.dto.UserProfileDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 用户画像服务 Mock 实现。
 * <p>
 * 模拟 M05 用户画像服务，生成模拟的长期/短期画像和融合标签，供联调使用。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@Service
public class MockUserProfileService implements UserProfileService {

    private static final String[] CATEGORY_POOL = {"101", "102", "103", "201", "202", "301", "302", "401"};
    private static final String[] TAG_POOL = {"新品", "热销", "好评", "限量", "精选", "推荐", "特惠", "品质", "高端", "性价比"};

    @Override
    public UserProfileDTO getUserProfile(String userId) {
        int seed = Math.abs(userId.hashCode());
        Random random = new Random(seed);

        // 模拟长期画像
        Map<String, Double> categoryPrefs = IntStream.range(0, 5)
                .boxed()
                .collect(Collectors.toMap(
                        i -> CATEGORY_POOL[(seed + i) % CATEGORY_POOL.length],
                        i -> 0.3 + random.nextDouble() * 0.7,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<String, Double> tagWeights = IntStream.range(0, 8)
                .boxed()
                .collect(Collectors.toMap(
                        i -> TAG_POOL[(seed + i * 2) % TAG_POOL.length],
                        i -> 0.2 + random.nextDouble() * 0.8,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        UserProfileDTO.LongTermProfile longTerm = UserProfileDTO.LongTermProfile.builder()
                .categoryPreferences(categoryPrefs)
                .tagWeights(tagWeights)
                .activityLevel(0.3 + random.nextDouble() * 0.7)
                .activeHours(Map.of(10, 0.1, 14, 0.15, 20, 0.25, 22, 0.2))
                .pricePreference(random.nextBoolean() ? "中" : "高")
                .registerDays(30 + random.nextInt(365))
                .build();

        // 模拟短期画像
        List<String> recentClicked = IntStream.range(0, 3 + random.nextInt(5))
                .mapToObj(i -> "i_" + (10000 + (seed + i * 17) % 90000))
                .collect(Collectors.toList());

        UserProfileDTO.ShortTermProfile shortTerm = UserProfileDTO.ShortTermProfile.builder()
                .recentClickedItems(recentClicked)
                .recentFavoritedItems(List.of("i_" + (10000 + seed % 90000)))
                .recentCategories(Map.of(CATEGORY_POOL[seed % CATEGORY_POOL.length], 2 + random.nextInt(5)))
                .recentActionCount(5 + random.nextInt(20))
                .build();

        // 模拟融合标签（短期 0.6 / 长期 0.4）
        Map<String, Double> fusedTags = new LinkedHashMap<>();
        tagWeights.forEach((tag, weight) -> fusedTags.merge(tag, weight * 0.4, Double::sum));
        fusedTags.merge("近期热门", 0.6, Double::sum);

        return UserProfileDTO.builder()
                .userId(userId)
                .longTerm(longTerm)
                .shortTerm(shortTerm)
                .fusedTags(fusedTags)
                .version(System.currentTimeMillis() / 1000)
                .updatedAt(System.currentTimeMillis())
                .build();
    }

    @Override
    public void invalidateProfileCache(String userId) {
        log.debug("失效用户画像缓存: userId={}", userId);
        // Mock 实现：无实际缓存，仅记录日志
    }
}

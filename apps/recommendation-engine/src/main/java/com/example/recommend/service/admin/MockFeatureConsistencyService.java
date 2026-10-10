package com.example.recommend.service.admin;

import com.example.recommend.dto.admin.FeatureConsistencyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 特征一致性校验服务 Mock 实现（M11-T05）。
 * <p>
 * 内存存储校验结果，模拟查询和手动触发校验。
 * 生产环境由 M12 特征平台替换，校验离线训练特征与在线推理特征的一致性。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@Service
public class MockFeatureConsistencyService implements FeatureConsistencyService {

    /** 内存存储：date → result */
    private final Map<String, FeatureConsistencyDTO> store = new ConcurrentHashMap<>();

    public MockFeatureConsistencyService() {
        initMockData();
    }

    @Override
    public FeatureConsistencyDTO getConsistencyResult(String date) {
        FeatureConsistencyDTO result = store.get(date);
        if (result == null) {
            // 未查询到指定日期数据时返回最近一次的结果
            result = store.values().stream()
                    .max((a, b) -> a.getCheckedAt().compareTo(b.getCheckedAt()))
                    .orElseGet(this::buildEmptyResult);
        }
        return result;
    }

    @Override
    public FeatureConsistencyDTO triggerConsistencyCheck() {
        String today = LocalDate.now(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ISO_LOCAL_DATE);
        log.info("手动触发特征一致性校验: date={}", today);

        // 模拟异步校验，立即返回初始状态（实际由 Flink 离线任务执行）
        FeatureConsistencyDTO result = FeatureConsistencyDTO.builder()
                .date(today)
                .sampleCount(1000)
                .passedCount(null)
                .failedCount(null)
                .maxAbsError(null)
                .status("RUNNING")
                .checkedAt(Instant.now())
                .detail(List.of())
                .build();
        store.put(today, result);
        return result;
    }

    private FeatureConsistencyDTO buildEmptyResult() {
        return FeatureConsistencyDTO.builder()
                .date(LocalDate.now(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ISO_LOCAL_DATE))
                .sampleCount(0)
                .passedCount(0)
                .failedCount(0)
                .maxAbsError(0.0)
                .status("NOT_CHECKED")
                .checkedAt(Instant.now())
                .detail(List.of())
                .build();
    }

    /**
     * 初始化 Mock 校验数据（对齐前端 mock/index.js）。
     */
    private void initMockData() {
        String date = LocalDate.now(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ISO_LOCAL_DATE);

        store.put(date, FeatureConsistencyDTO.builder()
                .date(date)
                .sampleCount(1000)
                .passedCount(998)
                .failedCount(2)
                .maxAbsError(8.5e-7)
                .status("PASSED")
                .checkedAt(Instant.now().minus(3, java.time.temporal.ChronoUnit.HOURS))
                .detail(List.of(
                        FeatureConsistencyDTO.FeatureDetail.builder()
                                .featureName("user_click_count_1h").expected(5.0).actual(5.0).error(0.0).status("PASSED").build(),
                        FeatureConsistencyDTO.FeatureDetail.builder()
                                .featureName("item_hot_score").expected(0.8723).actual(0.8723000001).error(1e-10).status("PASSED").build(),
                        FeatureConsistencyDTO.FeatureDetail.builder()
                                .featureName("user_category_pref_101").expected(0.85).actual(0.8499999).error(1e-7).status("PASSED").build(),
                        FeatureConsistencyDTO.FeatureDetail.builder()
                                .featureName("item_price_norm").expected(0.4231).actual(0.42310085).error(8.5e-7).status("PASSED").build(),
                        FeatureConsistencyDTO.FeatureDetail.builder()
                                .featureName("user_active_level").expected(0.72).actual(0.719999).error(1e-6).status("FAILED").build(),
                        FeatureConsistencyDTO.FeatureDetail.builder()
                                .featureName("context_hour_norm").expected(0.5833).actual(0.5834).error(1e-4).status("FAILED").build()
                ))
                .build());
    }
}

package com.example.recommend.service.behavior;

import com.example.recommend.dto.ActionType;
import com.example.recommend.dto.BehaviorEventDTO;
import com.example.recommend.dto.BehaviorResponseDTO;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

/**
 * 行为采集服务 Mock 实现。
 * <p>
 * 模拟 M08 行为采集服务，生成 eventId 并记录日志，实际 Kafka 写入由 M08 模块完成。
 * eventId = MurmurHash3(userId + itemId + actionType + eventTime)（架构文档 M08-T01）。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@Service
public class MockBehaviorCollectorService implements BehaviorCollectorService {

    @Override
    public BehaviorResponseDTO collect(BehaviorEventDTO event) {
        // 修复Bug-015：eventId 生成各字段间使用 "|" 分隔符，避免字段拼接碰撞
        // 原逻辑 userId+itemId+actionType+eventTime 直接拼接，
        // 例如 userId="ab"+itemId="c" 与 userId="a"+itemId="bc" 会产生相同 raw 字符串
        String raw = event.getUserId() + "|" + event.getItemId() + "|" + event.getActionType()
                + "|" + event.getEventTime().toString();
        String eventId = Hashing.murmur3_32_fixed().hashString(raw, StandardCharsets.UTF_8).toString();

        // 模拟异步写入 Kafka，立即返回
        log.info("行为事件已采集(模拟写入Kafka): eventId={}, userId={}, itemId={}, actionType={}, scene={}",
                eventId, event.getUserId(), event.getItemId(), event.getActionType(), event.getScene());

        return BehaviorResponseDTO.builder()
                .accepted(true)
                .eventId(eventId)
                .build();
    }

    @Override
    // 修复Bug-008：曝光埋点使用 @Async 异步执行，不阻塞推荐主链路响应
    // 架构文档明确要求"异步发送曝光埋点"，原实现为同步循环调用 collect()，
    // pageSize=50 时 50 次 MurmurHash3 计算 + 50 条 INFO 日志直接阻塞响应时间
    @Async
    public void trackImpression(String userId, List<String> itemIds, String scene) {
        if (itemIds == null || itemIds.isEmpty()) {
            return;
        }
        // 异步发送曝光埋点
        itemIds.forEach(itemId -> {
            BehaviorEventDTO impression = BehaviorEventDTO.builder()
                    .userId(userId)
                    .itemId(itemId)
                    .actionType(ActionType.IMPRESSION.getCode())
                    .scene(scene)
                    .eventTime(Instant.now())
                    .build();
            collect(impression);
        });
        log.debug("曝光埋点已发送(异步): userId={}, itemCount={}, scene={}", userId, itemIds.size(), scene);
    }
}

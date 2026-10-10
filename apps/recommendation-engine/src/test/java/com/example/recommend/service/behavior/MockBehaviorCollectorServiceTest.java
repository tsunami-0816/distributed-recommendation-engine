package com.example.recommend.service.behavior;

import com.example.recommend.dto.ActionType;
import com.example.recommend.dto.BehaviorEventDTO;
import com.example.recommend.dto.BehaviorResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 修复Bug-010：MockBehaviorCollectorService 单元测试。
 * <p>
 * 覆盖 eventId 生成唯一性（修复Bug-015 分隔符碰撞问题）、行为事件采集等核心逻辑。
 *
 * @author recommend-team
 * @since 1.0.0
 */
class MockBehaviorCollectorServiceTest {

    private MockBehaviorCollectorService behaviorCollectorService;

    @BeforeEach
    void setUp() {
        behaviorCollectorService = new MockBehaviorCollectorService();
    }

    @Test
    @DisplayName("修复Bug-015：不同用户和物品组合的 eventId 不碰撞（字段间使用分隔符）")
    void collect_withDifferentUserIdItemIdCombinations_shouldProduceDifferentEventIds() {
        // 修复Bug-015：原逻辑 userId+itemId 直接拼接，
        // userId="ab"+itemId="c" 与 userId="a"+itemId="bc" 会产生相同 raw 字符串
        Instant eventTime = Instant.parse("2026-10-04T10:00:00Z");

        BehaviorEventDTO event1 = BehaviorEventDTO.builder()
                .userId("ab")
                .itemId("c")
                .actionType(ActionType.CLICK.getCode())
                .scene("home")
                .eventTime(eventTime)
                .build();

        BehaviorEventDTO event2 = BehaviorEventDTO.builder()
                .userId("a")
                .itemId("bc")
                .actionType(ActionType.CLICK.getCode())
                .scene("home")
                .eventTime(eventTime)
                .build();

        BehaviorResponseDTO response1 = behaviorCollectorService.collect(event1);
        BehaviorResponseDTO response2 = behaviorCollectorService.collect(event2);

        // 修复Bug-015：使用 "|" 分隔符后，两个事件的 raw 字符串不同，eventId 应不同
        assertNotEquals(response1.getEventId(), response2.getEventId(),
                "userId='ab'+itemId='c' 与 userId='a'+itemId='bc' 的 eventId 不应相同");
    }

    @Test
    @DisplayName("修复Bug-015：相同事件参数生成相同 eventId（幂等性）")
    void collect_withSameEvent_shouldProduceSameEventId() {
        Instant eventTime = Instant.parse("2026-10-04T10:00:00Z");
        BehaviorEventDTO event = BehaviorEventDTO.builder()
                .userId("user1")
                .itemId("item1")
                .actionType(ActionType.CLICK.getCode())
                .scene("home")
                .eventTime(eventTime)
                .build();

        BehaviorResponseDTO response1 = behaviorCollectorService.collect(event);
        BehaviorResponseDTO response2 = behaviorCollectorService.collect(event);

        assertEquals(response1.getEventId(), response2.getEventId(),
                "相同事件参数应生成相同 eventId，保证幂等性");
    }

    @Test
    @DisplayName("修复Bug-015：不同行为类型生成不同 eventId")
    void collect_withDifferentActionType_shouldProduceDifferentEventIds() {
        Instant eventTime = Instant.parse("2026-10-04T10:00:00Z");

        BehaviorEventDTO clickEvent = BehaviorEventDTO.builder()
                .userId("user1")
                .itemId("item1")
                .actionType(ActionType.CLICK.getCode())
                .scene("home")
                .eventTime(eventTime)
                .build();

        BehaviorEventDTO viewEvent = BehaviorEventDTO.builder()
                .userId("user1")
                .itemId("item1")
                .actionType(ActionType.VIEW.getCode())
                .scene("home")
                .eventTime(eventTime)
                .build();

        BehaviorResponseDTO clickResponse = behaviorCollectorService.collect(clickEvent);
        BehaviorResponseDTO viewResponse = behaviorCollectorService.collect(viewEvent);

        assertNotEquals(clickResponse.getEventId(), viewResponse.getEventId(),
                "不同行为类型应生成不同 eventId");
    }

    @Test
    @DisplayName("collect 返回 accepted=true 且 eventId 非空")
    void collect_shouldReturnAcceptedWithEventId() {
        BehaviorEventDTO event = BehaviorEventDTO.builder()
                .userId("user1")
                .itemId("item1")
                .actionType(ActionType.CLICK.getCode())
                .scene("home")
                .eventTime(Instant.now())
                .build();

        BehaviorResponseDTO response = behaviorCollectorService.collect(event);

        assertTrue(response.isAccepted(), "采集结果应为 accepted=true");
        assertNotNull(response.getEventId(), "eventId 不应为空");
        assertFalse(response.getEventId().isBlank(), "eventId 不应为空字符串");
    }

    @Test
    @DisplayName("trackImpression 空列表不抛异常")
    void trackImpression_withEmptyItemIds_shouldNotThrow() {
        assertDoesNotThrow(() ->
                behaviorCollectorService.trackImpression("user1", java.util.List.of(), "home"));
        assertDoesNotThrow(() ->
                behaviorCollectorService.trackImpression("user1", null, "home"));
    }
}

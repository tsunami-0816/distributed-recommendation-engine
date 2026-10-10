package com.example.recommend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * 行为事件 DTO。
 * <p>
 * 架构文档 5.4.2 节行为采集接口请求体。
 * eventId 由服务端根据 userId+itemId+actionType+eventTime 通过 MurmurHash3 生成，客户端无需传入。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorEventDTO {

    /** 用户 ID */
    @NotBlank(message = "userId 不能为空")
    private String userId;

    /** 物品 ID */
    @NotBlank(message = "itemId 不能为空")
    private String itemId;

    /** 行为类型：click / view / favorite / impression / conversion */
    @NotBlank(message = "actionType 不能为空")
    private String actionType;

    /** 场景 */
    @NotBlank(message = "scene 不能为空")
    private String scene;

    /** 事件发生时间（ISO 8601，如 2026-10-03T14:30:00Z） */
    @NotNull(message = "eventTime 不能为空")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant eventTime;

    /** 扩展字段（如设备信息、页面位置等） */
    private Map<String, Object> extra;
}

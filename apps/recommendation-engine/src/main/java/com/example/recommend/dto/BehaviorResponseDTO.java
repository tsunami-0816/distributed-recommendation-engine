package com.example.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行为采集响应 DTO。
 * <p>
 * 异步写入 Kafka，立即返回 accepted=true。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorResponseDTO {

    /** 是否接受（异步写入，立即返回） */
    private boolean accepted;

    /** 服务端生成的事件唯一 ID */
    private String eventId;
}

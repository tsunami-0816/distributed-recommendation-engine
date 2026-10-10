package com.example.recommend.service.behavior;

import com.example.recommend.dto.BehaviorEventDTO;
import com.example.recommend.dto.BehaviorResponseDTO;

/**
 * 行为采集服务接口。
 * <p>
 * 架构文档 M08 行为采集服务：接收行为事件，校验后写入 Kafka（acks=all + 幂等），
 * Kafka 不可用时本地文件缓冲兜底，异步发送立即返回。
 * 本次 M09 模块仅依赖此接口，具体实现在 M08 模块中完成。
 *
 * @author recommend-team
 * @since 1.0.0
 */
public interface BehaviorCollectorService {

    /**
     * 采集行为事件，异步写入 Kafka，立即返回。
     *
     * @param event 行为事件
     * @return 采集结果（accepted + eventId）
     */
    BehaviorResponseDTO collect(BehaviorEventDTO event);

    /**
     * 发送曝光埋点（推荐返回后异步发送 impression 事件）。
     *
     * @param userId  用户 ID
     * @param itemIds 曝光的物品 ID 列表
     * @param scene   场景
     */
    void trackImpression(String userId, java.util.List<String> itemIds, String scene);
}

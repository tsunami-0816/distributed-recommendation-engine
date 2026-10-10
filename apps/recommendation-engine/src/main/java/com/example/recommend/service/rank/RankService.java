package com.example.recommend.service.rank;

import com.example.recommend.service.recall.CandidateItem;

import java.util.List;

/**
 * 排序服务接口。
 * <p>
 * 架构文档 M07 排序服务：特征组装 → 模型推理（三级降级）→ 重排（类目打散+长尾加权+过滤），
 * 输出 Top-K 最终结果。
 * 本次 M09 模块仅依赖此接口，具体实现在 M07 模块中完成，此处提供 Mock 实现。
 *
 * @author recommend-team
 * @since 1.0.0
 */
public interface RankService {

    /**
     * 对候选集进行精排和重排，返回最终推荐结果。
     *
     * @param candidates 召回候选集（200~300 个）
     * @param userId     用户 ID（用于特征组装和业务过滤）
     * @param pageSize   返回数量
     * @return 排序结果（包含实际使用的模型版本和物品列表）
     */
    RankResult rank(List<CandidateItem> candidates, String userId, int pageSize);

    /**
     * 排序结果。
     */
    record RankResult(
            /** 实际使用的模型版本（降级时为降级后的模型） */
            String modelVersion,
            /** 是否发生降级 */
            boolean degraded,
            /** 排序后的物品列表（已重排，Top-K） */
            List<RankedItem> items
    ) {}

    /**
     * 排序后的物品。
     */
    record RankedItem(
            /** 物品 ID */
            String itemId,
            /** 预测分数（模型输出） */
            double score,
            /** 物品详情 */
            com.example.recommend.dto.ItemInfoDTO itemInfo
    ) {}
}

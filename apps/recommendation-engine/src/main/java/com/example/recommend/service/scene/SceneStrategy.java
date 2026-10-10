package com.example.recommend.service.scene;

import com.example.recommend.dto.RecommendRequestDTO;
import com.example.recommend.service.recall.CandidateItem;

import java.util.List;

/**
 * 场景策略接口。
 * <p>
 * 架构文档 M09-T02：4 种场景策略（home / guess / related / search），
 * 场景差异化代码 ≤ 30%，公共逻辑复用 ≥ 70%。
 * 每种场景定义自己的召回通道组合和后处理逻辑。
 *
 * @author recommend-team
 * @since 1.0.0
 */
public interface SceneStrategy {

    /**
     * 获取该策略支持的场景。
     */
    com.example.recommend.dto.Scene getScene();

    /**
     * 执行该场景的召回逻辑。
     * <p>
     * 默认实现委托给 RecallService，子类可覆盖以定制召回通道组合。
     *
     * @param request 推荐请求
     * @return 候选物品列表
     */
    List<CandidateItem> recall(RecommendRequestDTO request);

    /**
     * 场景级后处理（在重排之后、返回之前执行）。
     * <p>
     * 默认不做任何处理，子类可覆盖实现场景特定的过滤/调整。
     *
     * @param items   排序后的物品
     * @param request 推荐请求
     * @return 后处理后的物品
     */
    default List<com.example.recommend.service.rank.RankService.RankedItem> postProcess(
            List<com.example.recommend.service.rank.RankService.RankedItem> items,
            RecommendRequestDTO request) {
        return items;
    }

    /**
     * 校验该场景的请求参数（如 related 场景需 itemId，search 场景需 keyword）。
     *
     * @param request 推荐请求
     * @throws com.example.recommend.common.BusinessException 参数校验失败时抛出
     */
    default void validate(RecommendRequestDTO request) {
        // 默认不做额外校验，子类可覆盖
    }
}

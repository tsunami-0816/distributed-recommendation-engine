package com.example.recommend.service.recall;

import com.example.recommend.dto.RecommendRequestDTO;

import java.util.List;

/**
 * 召回服务接口。
 * <p>
 * 架构文档 M06 召回服务：实现 5 路召回通道（ItemCF / 标签 / 画像 / 热门 / ES），
 * 合并去重截断，输出 200~300 候选集。
 * 本次 M09 模块仅依赖此接口，具体实现在 M06 模块中完成，此处提供 Mock 实现供联调。
 *
 * @author recommend-team
 * @since 1.0.0
 */
public interface RecallService {

    /**
     * 执行多路召回，返回合并去重后的候选集。
     *
     * @param request 推荐请求（包含 scene / userId / itemId / keyword）
     * @return 候选物品列表（200~300 个，按召回分数降序）
     */
    List<CandidateItem> recall(RecommendRequestDTO request);
}

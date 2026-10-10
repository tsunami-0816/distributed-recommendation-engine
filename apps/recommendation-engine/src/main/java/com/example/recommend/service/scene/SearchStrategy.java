package com.example.recommend.service.scene;

import com.example.recommend.common.BusinessException;
import com.example.recommend.common.ErrorCode;
import com.example.recommend.dto.RecommendRequestDTO;
import com.example.recommend.dto.Scene;
import com.example.recommend.service.recall.CandidateItem;
import com.example.recommend.service.recall.RecallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 搜索推荐策略。
 * <p>
 * 架构文档 M09-T02：ES 文本 + 语义 RRF 融合 + 精排。
 * search 场景必须传入 keyword。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchStrategy implements SceneStrategy {

    private final RecallService recallService;

    @Override
    public Scene getScene() {
        return Scene.SEARCH;
    }

    @Override
    public List<CandidateItem> recall(RecommendRequestDTO request) {
        log.debug("搜索推荐召回: keyword={}, userId={}", request.getKeyword(), request.getUserId());
        return recallService.recall(request);
    }

    @Override
    public void validate(RecommendRequestDTO request) {
        if (request.getKeyword() == null || request.getKeyword().isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_MISSING, "search 场景需要 keyword");
        }
    }
}

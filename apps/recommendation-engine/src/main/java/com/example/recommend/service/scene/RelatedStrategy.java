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
 * 相关推荐策略。
 * <p>
 * 架构文档 M09-T02：基于 itemId 的物品相似度 + 内容标签 + 精排，无需 userId。
 * related 场景必须传入 itemId。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RelatedStrategy implements SceneStrategy {

    private final RecallService recallService;

    @Override
    public Scene getScene() {
        return Scene.RELATED;
    }

    @Override
    public List<CandidateItem> recall(RecommendRequestDTO request) {
        log.debug("相关推荐召回: itemId={}", request.getItemId());
        return recallService.recall(request);
    }

    @Override
    public void validate(RecommendRequestDTO request) {
        if (request.getItemId() == null || request.getItemId().isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_MISSING, "related 场景需要 itemId");
        }
    }
}

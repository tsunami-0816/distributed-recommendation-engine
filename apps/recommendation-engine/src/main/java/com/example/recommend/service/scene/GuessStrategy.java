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
 * 猜你喜欢策略。
 * <p>
 * 架构文档 M09-T02：ItemCF 为主 + 画像召回 + 精排。
 * 与首页类似，但更强调个性化（ItemCF 权重更高）。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GuessStrategy implements SceneStrategy {

    private final RecallService recallService;

    @Override
    public Scene getScene() {
        return Scene.GUESS;
    }

    @Override
    public List<CandidateItem> recall(RecommendRequestDTO request) {
        log.debug("猜你喜欢召回: userId={}", request.getUserId());
        return recallService.recall(request);
    }

    @Override
    public void validate(RecommendRequestDTO request) {
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_MISSING, "guess 场景需要 userId");
        }
    }
}

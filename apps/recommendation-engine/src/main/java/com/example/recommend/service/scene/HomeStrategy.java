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
 * 首页推荐策略。
 * <p>
 * 架构文档 M09-T02：4 路召回（ItemCF + 标签 + 画像 + 热门）+ 精排 + 多样性重排。
 * 首页是默认场景，需要 userId。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HomeStrategy implements SceneStrategy {

    private final RecallService recallService;

    @Override
    public Scene getScene() {
        return Scene.HOME;
    }

    @Override
    public List<CandidateItem> recall(RecommendRequestDTO request) {
        log.debug("首页召回: userId={}", request.getUserId());
        return recallService.recall(request);
    }

    @Override
    public void validate(RecommendRequestDTO request) {
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_MISSING, "home 场景需要 userId");
        }
    }
}

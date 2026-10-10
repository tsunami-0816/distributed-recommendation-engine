package com.example.recommend.service.scene;

import com.example.recommend.common.BusinessException;
import com.example.recommend.common.ErrorCode;
import com.example.recommend.dto.Scene;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 场景策略工厂。
 * <p>
 * 根据 scene 参数选择对应的场景策略实现。新增场景只需实现 SceneStrategy 接口并注册为 Spring Bean，
 * 工厂自动发现，无需修改工厂代码（开闭原则）。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Component
public class SceneStrategyFactory {

    private final Map<Scene, SceneStrategy> strategyMap = new EnumMap<>(Scene.class);

    /**
     * 构造函数自动注入所有 SceneStrategy 实现，按场景注册。
     */
    public SceneStrategyFactory(List<SceneStrategy> strategies) {
        for (SceneStrategy strategy : strategies) {
            strategyMap.put(strategy.getScene(), strategy);
        }
    }

    /**
     * 根据场景 code 获取策略。
     *
     * @param sceneCode 场景 code（home/guess/related/search）
     * @return 场景策略
     * @throws BusinessException 不支持的场景
     */
    public SceneStrategy getStrategy(String sceneCode) {
        Scene scene = Scene.fromCode(sceneCode);
        SceneStrategy strategy = strategyMap.get(scene);
        if (strategy == null) {
            throw new BusinessException(ErrorCode.SCENE_NOT_SUPPORTED,
                    "场景策略未实现: " + sceneCode);
        }
        return strategy;
    }

    /**
     * 根据场景枚举获取策略。
     */
    public SceneStrategy getStrategy(Scene scene) {
        SceneStrategy strategy = strategyMap.get(scene);
        if (strategy == null) {
            throw new BusinessException(ErrorCode.SCENE_NOT_SUPPORTED,
                    "场景策略未实现: " + scene.getCode());
        }
        return strategy;
    }
}

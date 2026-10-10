package com.example.recommend.service.admin;

import com.example.recommend.common.BusinessException;
import com.example.recommend.common.ErrorCode;
import com.example.recommend.dto.admin.ModelVersionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 模型版本管理服务 Mock 实现（M11-T02）。
 * <p>
 * 内存存储模型版本数据，模拟激活/回滚/指标查询。
 * 模型激活前校验 featureConsistencyPassed 门禁。
 * 生产环境由 M07 模型服务替换。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@Service
public class MockModelManagementService implements ModelManagementService {

    /** 内存存储：modelType:version → model */
    private final Map<String, ModelVersionDTO> store = new ConcurrentHashMap<>();

    public MockModelManagementService() {
        initMockData();
    }

    @Override
    public List<ModelVersionDTO> listModels(String modelType) {
        List<ModelVersionDTO> result = new ArrayList<>(store.values());
        if (modelType != null && !modelType.isBlank()) {
            result = result.stream()
                    .filter(m -> modelType.equalsIgnoreCase(m.getModelType()))
                    .collect(Collectors.toList());
        }
        result.sort((a, b) -> {
            int typeCompare = a.getModelType().compareTo(b.getModelType());
            if (typeCompare != 0) return typeCompare;
            return b.getVersion().compareTo(a.getVersion());
        });
        return result;
    }

    @Override
    public ModelVersionDTO activateModel(String modelType, String version) {
        ModelVersionDTO target = getOrThrow(modelType, version);

        // 激活前置门禁：特征一致性校验必须通过
        if (target.getFeatureConsistencyPassed() == null || !target.getFeatureConsistencyPassed()) {
            throw new BusinessException(ErrorCode.FEATURE_CONSISTENCY_FAILED,
                    "模型 " + modelType + ":" + version + " 未通过特征一致性校验，无法激活");
        }

        // 同类型其他模型变为 inactive
        store.values().stream()
                .filter(m -> modelType.equalsIgnoreCase(m.getModelType()))
                .forEach(m -> m.setStatus("inactive"));

        target.setStatus("active");
        log.info("激活模型: {}:{}", modelType, version);
        return target;
    }

    @Override
    public ModelVersionDTO rollbackModel(String modelType, String version) {
        log.info("回滚模型: {}:{}（等价于激活）", modelType, version);
        return activateModel(modelType, version);
    }

    @Override
    public ModelVersionDTO getModelMetrics(String modelType, String version) {
        return getOrThrow(modelType, version);
    }

    private ModelVersionDTO getOrThrow(String modelType, String version) {
        String key = modelType + ":" + version;
        ModelVersionDTO model = store.get(key);
        if (model == null) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND,
                    "模型不存在: " + modelType + ":" + version);
        }
        return model;
    }

    /**
     * 初始化 Mock 模型数据（对齐前端 mock/index.js）。
     */
    private void initMockData() {
        Instant now = Instant.now();

        put(ModelVersionDTO.builder()
                .modelType("deepfm").version("v2")
                .filePath("/models/active/deepfm_v2_20261002.onnx")
                .auc(0.812).logLoss(0.423).featureDim(32)
                .featureConsistencyPassed(true).status("active")
                .trainedAt(now.minus(2, ChronoUnit.DAYS))
                .build());

        put(ModelVersionDTO.builder()
                .modelType("deepfm").version("v1")
                .filePath("/models/active/deepfm_v1_20260920.onnx")
                .auc(0.805).logLoss(0.431).featureDim(32)
                .featureConsistencyPassed(true).status("inactive")
                .trainedAt(now.minus(14, ChronoUnit.DAYS))
                .build());

        put(ModelVersionDTO.builder()
                .modelType("xgboost").version("v3")
                .filePath("/models/active/xgboost_v3_20261001.onnx")
                .auc(0.791).logLoss(0.445).featureDim(32)
                .featureConsistencyPassed(true).status("active")
                .trainedAt(now.minus(3, ChronoUnit.DAYS))
                .build());

        put(ModelVersionDTO.builder()
                .modelType("xgboost").version("v2")
                .filePath("/models/active/xgboost_v2_20260915.onnx")
                .auc(0.785).logLoss(0.452).featureDim(30)
                .featureConsistencyPassed(true).status("inactive")
                .trainedAt(now.minus(19, ChronoUnit.DAYS))
                .build());

        put(ModelVersionDTO.builder()
                .modelType("deepfm").version("v3")
                .filePath("/models/active/deepfm_v3_20261003.onnx")
                .auc(null).logLoss(null).featureDim(36)
                .featureConsistencyPassed(false).status("training")
                .trainedAt(now.minus(1, ChronoUnit.DAYS))
                .build());
    }

    private void put(ModelVersionDTO model) {
        store.put(model.getModelType() + ":" + model.getVersion(), model);
    }
}

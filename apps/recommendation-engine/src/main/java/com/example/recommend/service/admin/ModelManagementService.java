package com.example.recommend.service.admin;

import com.example.recommend.dto.admin.ModelVersionDTO;

import java.util.List;

/**
 * 模型版本管理服务接口（M11-T02）。
 * <p>
 * 提供模型列表查询、激活/回滚、指标查询。
 * 模型激活前必须通过特征一致性校验（featureConsistencyPassed=true）。
 *
 * @author recommend-team
 * @since 1.0.0
 */
public interface ModelManagementService {

    /**
     * 获取模型版本列表。
     *
     * @param modelType 可选模型类型过滤（deepfm/xgboost/rule）
     * @return 模型版本列表
     */
    List<ModelVersionDTO> listModels(String modelType);

    /**
     * 激活模型（同类型其他模型变为 inactive）。
     * <p>
     * 激活前置门禁：featureConsistencyPassed 必须为 true，
     * 否则抛出 FEATURE_CONSISTENCY_FAILED 异常。
     *
     * @param modelType 模型类型
     * @param version   模型版本
     * @return 激活后的模型信息
     */
    ModelVersionDTO activateModel(String modelType, String version);

    /**
     * 回滚模型（将指定版本重新激活，等价于 activate）。
     *
     * @param modelType 模型类型
     * @param version   模型版本
     * @return 回滚后的模型信息
     */
    ModelVersionDTO rollbackModel(String modelType, String version);

    /**
     * 获取模型评估指标（AUC / LogLoss 等）。
     *
     * @param modelType 模型类型
     * @param version   模型版本
     * @return 模型信息（含指标字段）
     */
    ModelVersionDTO getModelMetrics(String modelType, String version);
}

package com.example.recommend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 模型版本 DTO（M11-T02）。
 * <p>
 * 对应前端 ModelManagement.vue 及 /admin/models 接口的数据结构。
 * 模型激活前必须通过特征一致性校验（featureConsistencyPassed=true）。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelVersionDTO {

    /** 模型类型：deepfm / xgboost / rule */
    private String modelType;

    /** 模型版本：v1 / v2 / v3 */
    private String version;

    /** 模型文件路径 */
    private String filePath;

    /** AUC 指标（训练完成后填充） */
    private Double auc;

    /** LogLoss 指标（训练完成后填充） */
    private Double logLoss;

    /** 特征维度 */
    private Integer featureDim;

    /** 特征一致性校验是否通过（激活前置门禁） */
    private Boolean featureConsistencyPassed;

    /** 模型状态：active / inactive / training */
    private String status;

    /** 训练完成时间 */
    private Instant trainedAt;
}

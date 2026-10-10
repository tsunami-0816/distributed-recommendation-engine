package com.example.recommend.controller.admin;

import com.example.recommend.common.Result;
import com.example.recommend.dto.admin.ModelVersionDTO;
import com.example.recommend.service.admin.ModelManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模型管理 Controller（M11-T02）。
 * <p>
 * 基础路径：/admin/models
 * 提供模型版本列表、激活/回滚、指标查询。
 * 模型激活前必须通过特征一致性校验（featureConsistencyPassed=true）。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/models")
@RequiredArgsConstructor
@Tag(name = "管理后台 - 模型管理", description = "模型版本列表、激活/回滚、指标查询（M11-T02）")
public class ModelAdminController {

    private final ModelManagementService modelManagementService;

    @GetMapping
    @Operation(summary = "获取模型版本列表", description = "支持按模型类型过滤（deepfm/xgboost/rule）")
    public Result<List<ModelVersionDTO>> listModels(
            @Parameter(description = "模型类型过滤")
            @RequestParam(required = false) String modelType) {
        return Result.success(modelManagementService.listModels(modelType));
    }

    @PostMapping("/{modelType}/{version}/activate")
    @Operation(summary = "激活模型", description = "激活前校验 featureConsistencyPassed=true，同类型其他模型变为 inactive")
    public Result<ModelVersionDTO> activateModel(
            @Parameter(description = "模型类型") @PathVariable String modelType,
            @Parameter(description = "模型版本") @PathVariable String version) {
        return Result.success(modelManagementService.activateModel(modelType, version));
    }

    @PostMapping("/{modelType}/{version}/rollback")
    @Operation(summary = "回滚模型", description = "将指定版本重新激活（等价于 activate）")
    public Result<ModelVersionDTO> rollbackModel(
            @Parameter(description = "模型类型") @PathVariable String modelType,
            @Parameter(description = "模型版本") @PathVariable String version) {
        return Result.success(modelManagementService.rollbackModel(modelType, version));
    }

    @GetMapping("/{modelType}/{version}/metrics")
    @Operation(summary = "获取模型评估指标", description = "含 AUC / LogLoss / 特征维度 / 一致性校验状态")
    public Result<ModelVersionDTO> getModelMetrics(
            @Parameter(description = "模型类型") @PathVariable String modelType,
            @Parameter(description = "模型版本") @PathVariable String version) {
        return Result.success(modelManagementService.getModelMetrics(modelType, version));
    }
}

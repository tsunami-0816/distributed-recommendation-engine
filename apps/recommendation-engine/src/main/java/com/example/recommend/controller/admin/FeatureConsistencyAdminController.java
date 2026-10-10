package com.example.recommend.controller.admin;

import com.example.recommend.common.Result;
import com.example.recommend.dto.admin.FeatureConsistencyDTO;
import com.example.recommend.service.admin.FeatureConsistencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 特征一致性校验 Controller（M11-T05）。
 * <p>
 * 基础路径：/admin/feature-consistency
 * 提供校验结果查询和手动触发校验。
 * 校验离线训练特征与在线推理特征的一致性，是模型发布的前置门禁。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/feature-consistency")
@RequiredArgsConstructor
@Tag(name = "管理后台 - 特征一致性校验", description = "校验结果查询与手动触发，模型发布前置门禁（M11-T05）")
public class FeatureConsistencyAdminController {

    private final FeatureConsistencyService featureConsistencyService;

    @GetMapping("/{date}")
    @Operation(summary = "查询特征一致性校验结果", description = "按日期（YYYY-MM-DD）查询校验结果，含明细")
    public Result<FeatureConsistencyDTO> getFeatureConsistency(
            @Parameter(description = "日期（YYYY-MM-DD）") @PathVariable String date) {
        return Result.success(featureConsistencyService.getConsistencyResult(date));
    }

    @PostMapping("/trigger")
    @Operation(summary = "手动触发特征一致性校验", description = "触发离线校验任务，立即返回初始状态（RUNNING）")
    public Result<FeatureConsistencyDTO> triggerFeatureConsistency() {
        return Result.success(featureConsistencyService.triggerConsistencyCheck());
    }
}

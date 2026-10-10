package com.example.recommend.controller.admin;

import com.example.recommend.common.Result;
import com.example.recommend.dto.admin.AbExperimentDTO;
import com.example.recommend.service.admin.AbExperimentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * A/B 实验管理 Controller（M11-T01）。
 * <p>
 * 基础路径：/admin/ab/experiments
 * 提供实验 CRUD、状态机流转（draft→running→paused→ended）及实验报告查询。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/ab/experiments")
@RequiredArgsConstructor
@Tag(name = "管理后台 - A/B实验管理", description = "实验 CRUD、状态机流转、实验报告（M11-T01）")
public class AbExperimentAdminController {

    private final AbExperimentService abExperimentService;

    @GetMapping
    @Operation(summary = "获取实验列表", description = "支持按状态过滤（draft/running/paused/ended）")
    public Result<List<AbExperimentDTO>> listExperiments(
            @Parameter(description = "实验状态过滤")
            @RequestParam(required = false) String status) {
        return Result.success(abExperimentService.listExperiments(status));
    }

    @PostMapping
    @Operation(summary = "创建实验", description = "初始状态 draft，configVersion=1")
    public Result<AbExperimentDTO> createExperiment(@RequestBody AbExperimentDTO experiment) {
        return Result.success(abExperimentService.createExperiment(experiment));
    }

    @PutMapping("/{experimentId}")
    @Operation(summary = "更新实验", description = "configVersion 自增，失效旧分流缓存")
    public Result<AbExperimentDTO> updateExperiment(
            @Parameter(description = "实验 ID") @PathVariable String experimentId,
            @RequestBody AbExperimentDTO experiment) {
        return Result.success(abExperimentService.updateExperiment(experimentId, experiment));
    }

    @PostMapping("/{experimentId}/start")
    @Operation(summary = "启动实验", description = "draft/paused → running")
    public Result<AbExperimentDTO> startExperiment(
            @Parameter(description = "实验 ID") @PathVariable String experimentId) {
        return Result.success(abExperimentService.startExperiment(experimentId));
    }

    @PostMapping("/{experimentId}/pause")
    @Operation(summary = "暂停实验", description = "running → paused")
    public Result<AbExperimentDTO> pauseExperiment(
            @Parameter(description = "实验 ID") @PathVariable String experimentId) {
        return Result.success(abExperimentService.pauseExperiment(experimentId));
    }

    @PostMapping("/{experimentId}/end")
    @Operation(summary = "结束实验", description = "running/paused → ended")
    public Result<AbExperimentDTO> endExperiment(
            @Parameter(description = "实验 ID") @PathVariable String experimentId) {
        return Result.success(abExperimentService.endExperiment(experimentId));
    }

    @GetMapping("/{experimentId}/report")
    @Operation(summary = "获取实验报告", description = "含各指标的对照组/实验组数据、提升度、p 值")
    public Result<AbExperimentDTO> getExperimentReport(
            @Parameter(description = "实验 ID") @PathVariable String experimentId) {
        return Result.success(abExperimentService.getExperimentReport(experimentId));
    }
}

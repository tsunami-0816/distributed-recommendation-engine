package com.example.recommend.controller.admin;

import com.example.recommend.common.BusinessException;
import com.example.recommend.common.ErrorCode;
import com.example.recommend.common.Result;
import com.example.recommend.dto.admin.SystemConfigDTO;
import com.example.recommend.service.admin.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置管理 Controller（M11-T03）。
 * <p>
 * 基础路径：/admin/configs
 * 提供动态配置的查询和更新，更新后写入 Redis config:dynamic 支持热更新。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/configs")
@RequiredArgsConstructor
@Tag(name = "管理后台 - 系统配置", description = "动态配置查询与更新，支持热更新（M11-T03）")
public class SystemConfigAdminController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    @Operation(summary = "获取系统配置列表", description = "返回所有动态配置项")
    public Result<List<SystemConfigDTO>> listConfigs() {
        return Result.success(systemConfigService.listConfigs());
    }

    @PutMapping("/{key}")
    @Operation(summary = "更新系统配置", description = "更新 Redis config:dynamic，支持热更新")
    public Result<SystemConfigDTO> updateConfig(
            @Parameter(description = "配置 Key") @PathVariable String key,
            @RequestBody Map<String, String> body) {
        // 修复Bug-012：校验请求体和 configValue 字段非空，避免配置值被置空导致 NPE 或逻辑异常
        if (body == null || body.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_MISSING, "请求体不能为空");
        }
        String value = body.get("configValue");
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_MISSING, "configValue 字段不能为空");
        }
        return Result.success(systemConfigService.updateConfig(key, value));
    }
}

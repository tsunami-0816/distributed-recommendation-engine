package com.example.recommend.service.admin;

import com.example.recommend.dto.admin.SystemConfigDTO;

import java.util.List;

/**
 * 系统配置管理服务接口（M11-T03）。
 * <p>
 * 提供动态配置的查询和更新，更新后写入 Redis config:dynamic 支持热更新。
 *
 * @author recommend-team
 * @since 1.0.0
 */
public interface SystemConfigService {

    /**
     * 获取系统配置列表。
     *
     * @return 配置列表
     */
    List<SystemConfigDTO> listConfigs();

    /**
     * 更新系统配置（更新 Redis config:dynamic，支持热更新）。
     *
     * @param key   配置 Key
     * @param value 配置 Value（请求体中的 configValue 字段）
     * @return 更新后的配置
     */
    SystemConfigDTO updateConfig(String key, String value);
}

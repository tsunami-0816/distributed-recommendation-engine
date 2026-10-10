package com.example.recommend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 系统配置 DTO（M11-T03）。
 * <p>
 * 对应前端 SystemConfig.vue 及 /admin/configs 接口的数据结构。
 * 配置更新后写入 Redis config:dynamic，支持热更新。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigDTO {

    /** 配置 Key（如 recommend.rank.default-page-size） */
    private String configKey;

    /** 配置 Value */
    private String configValue;

    /** 配置描述 */
    private String description;

    /** 最后更新时间 */
    private Instant updatedAt;

    /** 最后更新人 */
    private String updatedBy;
}

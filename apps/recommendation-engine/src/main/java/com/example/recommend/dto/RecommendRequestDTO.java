package com.example.recommend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推荐请求 DTO。
 * <p>
 * 架构文档 5.4.1 节推荐接口请求参数。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendRequestDTO {

    /** 场景：home / guess / related / search */
    @NotBlank(message = "scene 不能为空")
    private String scene;

    /** 用户 ID */
    @NotBlank(message = "userId 不能为空")
    private String userId;

    /** 相关推荐的参考物品 ID（related 场景必填） */
    private String itemId;

    /** 搜索关键词（search 场景必填） */
    private String keyword;

    /** 返回数量，默认 20，最大 50 */
    @Min(value = 1, message = "pageSize 最小为 1")
    @Max(value = 50, message = "pageSize 最大为 50")
    @Builder.Default
    private Integer pageSize = 20;

    /** 设备类型：mobile / pc / web，默认 web */
    @Builder.Default
    private String device = "web";
}

package com.example.recommend.controller;

import com.example.recommend.common.Result;
import com.example.recommend.dto.UserProfileDTO;
import com.example.recommend.service.profile.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户画像 Controller。
 * <p>
 * 架构文档 5.4.3 节：GET /api/v1/user/{userId}/profile，返回用户画像（长期 + 短期融合结果）。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "用户画像接口", description = "用户画像查询（长期+短期融合）")
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * 获取用户画像。
     *
     * @param userId 用户 ID
     */
    @GetMapping("/{userId}/profile")
    @Operation(summary = "获取用户画像", description = "返回长期画像+短期画像的融合结果，融合权重短期0.6/长期0.4")
    public Result<UserProfileDTO> getUserProfile(
            @Parameter(description = "用户 ID", required = true)
            @PathVariable String userId) {
        UserProfileDTO profile = userProfileService.getUserProfile(userId);
        return Result.success(profile);
    }
}

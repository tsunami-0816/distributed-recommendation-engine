package com.example.recommend.service.profile;

import com.example.recommend.dto.UserProfileDTO;

/**
 * 用户画像服务接口。
 * <p>
 * 架构文档 M05 用户画像服务：长期/短期双字段隔离、缓存分层管理、加权融合（短期 0.6 / 长期 0.4）。
 * 本次 M09 模块仅依赖此接口，具体实现在 M05 模块中完成。
 *
 * @author recommend-team
 * @since 1.0.0
 */
public interface UserProfileService {

    /**
     * 获取用户画像（长期 + 短期融合结果）。
     *
     * @param userId 用户 ID
     * @return 融合后的用户画像，用户不存在时返回默认画像
     */
    UserProfileDTO getUserProfile(String userId);

    /**
     * 失效用户画像缓存（行为上报时触发，保证实时性）。
     *
     * @param userId 用户 ID
     */
    void invalidateProfileCache(String userId);
}

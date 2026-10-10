import request from './request'

/**
 * 用户画像 API 封装
 * 对应后端 UserProfileController
 * 基础路径: /api/v1/user
 */

/**
 * 查询用户画像
 * GET /api/v1/user/{userId}/profile
 * @param {string} userId - 用户ID
 * @returns {Promise<Object>} Result<UserProfileDTO>
 */
export function getUserProfile(userId) {
  return request({
    url: `/api/v1/user/${userId}/profile`,
    method: 'get'
  })
}

export default { getUserProfile }

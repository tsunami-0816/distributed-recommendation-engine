import request from '../request'

/**
 * 管理后台 - 缓存管理 API
 * 对应后端 CacheAdminController (M11-T04)
 * 基础路径: /admin/cache
 */

/**
 * 刷新缓存（支持按 Key 前缀批量失效 / 指定具体 Key 删除）
 * POST /admin/cache/refresh
 * @param {Object} data
 * @param {string} [data.prefix] - Key 前缀（如 rec:result / user:profile:long）
 * @param {string} [data.key] - 具体 Key
 */
export function refreshCache(data) {
  return request({ url: '/admin/cache/refresh', method: 'post', data })
}

export default { refreshCache }

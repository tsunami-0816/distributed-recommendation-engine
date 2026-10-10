import request from '../request'

/**
 * 管理后台 - 系统配置 API
 * 对应后端 SystemConfigAdminController (M11-T03)
 * 基础路径: /admin/configs
 */

/**
 * 获取系统配置列表
 * GET /admin/configs
 */
export function getConfigList() {
  return request({ url: '/admin/configs', method: 'get' })
}

/**
 * 更新系统配置（更新 Redis config:dynamic，支持热更新）
 * PUT /admin/configs/{key}
 */
export function updateConfig(key, data) {
  return request({ url: `/admin/configs/${key}`, method: 'put', data })
}

export default { getConfigList, updateConfig }

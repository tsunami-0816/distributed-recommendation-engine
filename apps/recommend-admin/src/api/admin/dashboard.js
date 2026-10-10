import request from '../request'

/**
 * 管理后台 - 仪表盘 API
 * 对应后端 DashboardAdminController (M18 / P-05)
 * 基础路径: /admin/dashboard
 */

/**
 * 获取仪表盘统计数据
 * GET /admin/dashboard/stats
 */
export function getDashboardStats() {
  return request({ url: '/admin/dashboard/stats', method: 'get' })
}

/**
 * 获取系统健康状态（Actuator 端点）
 * GET /actuator/health
 */
export function getSystemHealth() {
  return request({ url: '/actuator/health', method: 'get' })
}

export default { getDashboardStats, getSystemHealth }

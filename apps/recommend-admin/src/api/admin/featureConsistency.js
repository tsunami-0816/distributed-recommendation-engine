import request from '../request'

/**
 * 管理后台 - 特征一致性校验 API
 * 对应后端 FeatureConsistencyAdminController (M11-T05)
 * 基础路径: /admin/feature-consistency
 */

/**
 * 查询特征一致性校验结果
 * GET /admin/feature-consistency/{date}
 * @param {string} date - 日期 (YYYY-MM-DD)
 */
export function getFeatureConsistency(date) {
  return request({ url: `/admin/feature-consistency/${date}`, method: 'get' })
}

/**
 * 手动触发特征一致性校验
 * POST /admin/feature-consistency/trigger
 */
export function triggerFeatureConsistency() {
  return request({ url: '/admin/feature-consistency/trigger', method: 'post' })
}

export default { getFeatureConsistency, triggerFeatureConsistency }

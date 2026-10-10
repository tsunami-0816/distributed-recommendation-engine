import request from './request'

/**
 * 行为采集 API 封装
 * 对应后端 BehaviorController
 * 基础路径: /api/v1/behavior
 */

/**
 * 上报行为事件
 * POST /api/v1/behavior
 * @param {Object} data
 * @param {string} data.userId - 用户ID（必填）
 * @param {string} data.itemId - 物品ID（必填）
 * @param {string} data.actionType - 行为类型: click/view/favorite/impression/conversion（必填）
 * @param {string} data.scene - 场景（必填）
 * @param {string} data.eventTime - 事件时间 ISO 8601（必填）
 * @param {Object} [data.extra] - 扩展字段
 * @returns {Promise<Object>} Result<{accepted, eventId}>
 */
export function reportBehavior(data) {
  return request({
    url: '/api/v1/behavior',
    method: 'post',
    data
  })
}

export default { reportBehavior }

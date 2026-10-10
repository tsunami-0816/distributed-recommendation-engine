import request from '../request'

/**
 * 管理后台 - A/B 实验管理 API
 * 对应后端 AbExperimentAdminController (M11-T01)
 * 基础路径: /admin/ab/experiments
 */

/**
 * 获取实验列表
 * GET /admin/ab/experiments
 */
export function getExperimentList(params) {
  return request({ url: '/admin/ab/experiments', method: 'get', params })
}

/**
 * 创建实验
 * POST /admin/ab/experiments
 */
export function createExperiment(data) {
  return request({ url: '/admin/ab/experiments', method: 'post', data })
}

/**
 * 更新实验（自动 configVersion 自增 + 失效旧分流缓存）
 * PUT /admin/ab/experiments/{experimentId}
 */
export function updateExperiment(experimentId, data) {
  return request({ url: `/admin/ab/experiments/${experimentId}`, method: 'put', data })
}

/**
 * 启动实验
 * POST /admin/ab/experiments/{id}/start
 */
export function startExperiment(id) {
  return request({ url: `/admin/ab/experiments/${id}/start`, method: 'post' })
}

/**
 * 暂停实验
 * POST /admin/ab/experiments/{id}/pause
 */
export function pauseExperiment(id) {
  return request({ url: `/admin/ab/experiments/${id}/pause`, method: 'post' })
}

/**
 * 结束实验
 * POST /admin/ab/experiments/{id}/end
 */
export function endExperiment(id) {
  return request({ url: `/admin/ab/experiments/${id}/end`, method: 'post' })
}

/**
 * 获取实验报告
 * GET /admin/ab/experiments/{id}/report
 */
export function getExperimentReport(id) {
  return request({ url: `/admin/ab/experiments/${id}/report`, method: 'get' })
}

export default {
  getExperimentList,
  createExperiment,
  updateExperiment,
  startExperiment,
  pauseExperiment,
  endExperiment,
  getExperimentReport
}

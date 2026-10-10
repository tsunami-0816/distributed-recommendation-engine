import request from '../request'

/**
 * 管理后台 - 模型管理 API
 * 对应后端 ModelAdminController (M11-T02)
 * 基础路径: /admin/models
 */

/**
 * 获取模型版本列表
 * GET /admin/models
 */
export function getModelList(params) {
  return request({ url: '/admin/models', method: 'get', params })
}

/**
 * 激活模型（激活前校验 feature_consistency_passed=1）
 * POST /admin/models/{modelType}/{version}/activate
 */
export function activateModel(modelType, version) {
  return request({
    url: `/admin/models/${modelType}/${version}/activate`,
    method: 'post'
  })
}

/**
 * 回滚模型
 * POST /admin/models/{modelType}/{version}/rollback
 */
export function rollbackModel(modelType, version) {
  return request({
    url: `/admin/models/${modelType}/${version}/rollback`,
    method: 'post'
  })
}

/**
 * 获取模型评估指标
 * GET /admin/models/{modelType}/{version}/metrics
 */
export function getModelMetrics(modelType, version) {
  return request({
    url: `/admin/models/${modelType}/${version}/metrics`,
    method: 'get'
  })
}

export default {
  getModelList,
  activateModel,
  rollbackModel,
  getModelMetrics
}

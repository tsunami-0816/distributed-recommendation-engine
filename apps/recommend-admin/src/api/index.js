/**
 * API 统一导出
 * 用法: import { recommendApi, behaviorApi } from '@/api'
 */
export { default as recommendApi } from './recommend'
export { default as behaviorApi } from './behavior'
export { default as userProfileApi } from './userProfile'
export { default as hotItemsApi } from './hotItems'

// 管理后台 API
export { default as experimentApi } from './admin/experiment'
export { default as modelApi } from './admin/model'
export { default as configApi } from './admin/config'
export { default as cacheApi } from './admin/cache'
export { default as featureConsistencyApi } from './admin/featureConsistency'

import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * Axios 实例配置
 * - baseURL: 开发环境走 Vite proxy，生产环境可通过环境变量配置
 * - timeout: 15s（推荐接口 P99 < 200ms，留足余量）
 */
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：附加 TraceId、API Key
service.interceptors.request.use(
  (config) => {
    // 透传或生成 TraceId（与后端 X-Trace-Id 对齐）
    const traceId = sessionStorage.getItem('traceId') || generateTraceId()
    config.headers['X-Trace-Id'] = traceId

    // 管理后台接口附加管理员 API Key（开发环境默认值）
    if (config.url.startsWith('/admin')) {
      config.headers['X-API-Key'] = import.meta.env.VITE_ADMIN_API_KEY || 'admin-dev-key'
    }
    return config
  },
  (error) => {
    console.error('[Request Error]', error)
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理 Result<T> 结构
service.interceptors.response.use(
  (response) => {
    const res = response.data

    // 非标准响应（如 Actuator 端点）直接返回
    if (res === undefined || res.code === undefined) {
      return res
    }

    // code = 0 成功
    if (res.code === 0) {
      return res
    }

    // 业务降级码（400001 召回为空 / 400002 排序降级）：不报错，返回数据供页面处理
    if (res.code === 400001 || res.code === 400002 || res.code === 400003) {
      return res
    }

    // 鉴权失败
    if (res.code === 200001 || res.code === 200003) {
      ElMessage.error('登录已过期，请重新登录')
      router.push('/')
      return Promise.reject(new Error(res.message || 'Unauthorized'))
    }

    // 限流
    if (res.code === 600001) {
      ElMessage.warning('请求过于频繁，请稍后再试')
      return Promise.reject(new Error(res.message || 'Rate limited'))
    }

    // 其他业务错误
    ElMessage.error(res.message || `请求失败 (code: ${res.code})`)
    return Promise.reject(new Error(res.message || 'Error'))
  },
  (error) => {
    // HTTP 层错误
    if (error.response) {
      const status = error.response.status
      const message = error.response.data?.message || error.message
      if (status === 429) {
        ElMessage.warning('请求频率超限，请稍后再试')
      } else if (status === 504) {
        ElMessage.error('服务超时，请稍后重试')
      } else if (status === 503) {
        ElMessage.error('服务暂不可用，请稍后重试')
      } else if (status >= 500) {
        ElMessage.error(`服务器错误 (${status})，请联系管理员`)
      } else {
        ElMessage.error(message || `请求失败 (${status})`)
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络连接')
    } else {
      ElMessage.error('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

/**
 * 生成 TraceId（与后端 UUID 格式对齐）
 */
function generateTraceId() {
  return 'xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

export default service

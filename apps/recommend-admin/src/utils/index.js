/**
 * 通用工具函数
 */

/**
 * 格式化时间戳为可读字符串
 * @param {number|string} timestamp - 时间戳（毫秒）
 * @param {string} format - 格式化模板
 * @returns {string}
 */
export function formatTime(timestamp, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!timestamp) return '-'
  const date = new Date(Number(timestamp))
  const pad = (n) => String(n).padStart(2, '0')
  return format
    .replace('YYYY', date.getFullYear())
    .replace('MM', pad(date.getMonth() + 1))
    .replace('DD', pad(date.getDate()))
    .replace('HH', pad(date.getHours()))
    .replace('mm', pad(date.getMinutes()))
    .replace('ss', pad(date.getSeconds()))
}

/**
 * 格式化数字（千分位）
 * @param {number} num
 * @returns {string}
 */
export function formatNumber(num) {
  if (num === null || num === undefined) return '-'
  return Number(num).toLocaleString('zh-CN')
}

/**
 * 格式化百分比
 * @param {number} value - 0~1 之间的小数
 * @param {number} decimals - 小数位数
 * @returns {string}
 */
export function formatPercent(value, decimals = 2) {
  if (value === null || value === undefined) return '-'
  return `${(Number(value) * 100).toFixed(decimals)}%`
}

/**
 * 格式化价格
 * @param {number} price
 * @returns {string}
 */
export function formatPrice(price) {
  if (price === null || price === undefined) return '-'
  return `¥${Number(price).toFixed(2)}`
}

/**
 * 防抖函数
 * @param {Function} fn
 * @param {number} delay
 * @returns {Function}
 */
export function debounce(fn, delay = 300) {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn.apply(this, args), delay)
  }
}

/**
 * 节流函数
 * @param {Function} fn
 * @param {number} interval
 * @returns {Function}
 */
export function throttle(fn, interval = 300) {
  let last = 0
  return function (...args) {
    const now = Date.now()
    if (now - last >= interval) {
      last = now
      fn.apply(this, args)
    }
  }
}

/**
 * 深拷贝
 * @param {*} obj
 * @returns {*}
 */
export function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj.getTime())
  if (obj instanceof Array) return obj.map((item) => deepClone(item))
  const result = {}
  for (const key in obj) {
    if (Object.prototype.hasOwnProperty.call(obj, key)) {
      result[key] = deepClone(obj[key])
    }
  }
  return result
}

/**
 * 生成随机 ID
 * @returns {string}
 */
export function generateId() {
  return Math.random().toString(36).substring(2, 10) + Date.now().toString(36)
}

/**
 * 下载 JSON 文件
 * @param {object} data
 * @param {string} filename
 */
export function downloadJson(data, filename) {
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

/**
 * 召回通道中文映射
 */
export const RECALL_CHANNEL_MAP = {
  itemcf: '协同过滤',
  tag: '内容标签',
  profile: '用户画像',
  hot: '热门趋势',
  es_text: 'ES文本',
  es_semantic: 'ES语义'
}

/**
 * 行为类型中文映射
 */
export const ACTION_TYPE_MAP = {
  click: '点击',
  view: '浏览',
  favorite: '收藏',
  impression: '曝光',
  conversion: '转化'
}

/**
 * 实验状态映射
 */
export const EXPERIMENT_STATUS_MAP = {
  draft: { label: '草稿', type: 'info' },
  running: { label: '运行中', type: 'success' },
  paused: { label: '已暂停', type: 'warning' },
  ended: { label: '已结束', type: 'danger' }
}

/**
 * 模型状态映射
 */
export const MODEL_STATUS_MAP = {
  training: { label: '训练中', type: 'warning' },
  trained: { label: '已训练', type: 'info' },
  active: { label: '已激活', type: 'success' },
  inactive: { label: '未激活', type: 'info' },
  rolled_back: { label: '已回滚', type: 'danger' }
}

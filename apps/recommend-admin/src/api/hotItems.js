import request from './request'

/**
 * 热门物品 API 封装
 * 对应后端 HotItemsController
 * 基础路径: /api/v1/hot/items
 */

/**
 * 获取热门物品列表
 * GET /api/v1/hot/items
 * @param {Object} [params]
 * @param {number} [params.categoryId] - 类目ID，不传返回全局热门
 * @param {number} [params.limit=20] - 返回数量，最大100
 * @returns {Promise<Object>} Result<HotItemDTO[]>
 */
export function getHotItems(params) {
  return request({
    url: '/api/v1/hot/items',
    method: 'get',
    params
  })
}

export default { getHotItems }

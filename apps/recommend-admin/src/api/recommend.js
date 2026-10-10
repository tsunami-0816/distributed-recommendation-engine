import request from './request'

/**
 * 推荐接口 API 封装
 * 对应后端 RecommendController
 * 基础路径: /api/v1/recommend
 */

/**
 * 获取推荐结果
 * GET /api/v1/recommend
 * @param {Object} params
 * @param {string} params.scene - 场景: home/guess/related/search（必填）
 * @param {string} params.userId - 用户ID（必填）
 * @param {string} [params.itemId] - 相关推荐参考物品ID（related场景必填）
 * @param {string} [params.keyword] - 搜索关键词（search场景必填）
 * @param {number} [params.pageSize=20] - 返回数量，1~50
 * @param {string} [params.device='web'] - 设备类型
 * @returns {Promise<Object>} Result<RecommendResponseDTO>
 */
export function getRecommend(params) {
  return request({
    url: '/api/v1/recommend',
    method: 'get',
    params
  })
}

export default { getRecommend }

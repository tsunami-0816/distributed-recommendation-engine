import { defineStore } from 'pinia'
import { recommendApi, behaviorApi } from '@/api'
import { mockRecommendResponse } from '@/mock'

/**
 * 推荐结果 Store
 * 管理当前推荐场景、请求参数、推荐结果、加载状态
 */
export const useRecommendStore = defineStore('recommend', {
  state: () => ({
    // 当前场景: home / guess / related / search
    currentScene: 'home',
    // 当前用户ID
    userId: 'u_10001',
    // 推荐结果
    result: null,
    // 加载状态
    loading: false,
    // 错误信息
    error: null,
    // 是否使用 Mock 数据（后端不可用时自动降级）
    useMock: import.meta.env.VITE_USE_MOCK === 'true'
  }),

  getters: {
    // 推荐物品列表
    items: (state) => state.result?.data?.items || [],
    // A/B 实验组
    abGroup: (state) => state.result?.data?.abGroup || '-',
    // 模型版本
    modelVersion: (state) => state.result?.data?.modelVersion || '-',
    // 候选数量
    candidateCount: (state) => state.result?.data?.candidateCount || 0,
    // 各阶段耗时
    duration: (state) => state.result?.data?.duration || null,
    // 总耗时
    totalMs: (state) => state.result?.data?.duration?.totalMs || 0
  },

  actions: {
    /**
     * 获取推荐结果
     * @param {Object} params - 请求参数
     */
    async fetchRecommend(params = {}) {
      this.loading = true
      this.error = null

      const requestParams = {
        scene: this.currentScene,
        userId: this.userId,
        pageSize: 20,
        device: 'web',
        ...params
      }

      try {
        if (this.useMock) {
          // Mock 模式：模拟网络延迟
          await new Promise((resolve) => setTimeout(resolve, 300))
          this.result = {
            ...mockRecommendResponse,
            data: {
              ...mockRecommendResponse.data,
              scene: requestParams.scene
            }
          }
        } else {
          this.result = await recommendApi.getRecommend(requestParams)
        }
      } catch (err) {
        this.error = err.message || '获取推荐结果失败'
        // 真实请求失败时自动降级到 Mock，保证页面可用
        if (!this.useMock) {
          console.warn('[Recommend] 真实接口失败，自动降级到 Mock 数据')
          this.result = { ...mockRecommendResponse, data: { ...mockRecommendResponse.data, scene: requestParams.scene } }
        }
      } finally {
        this.loading = false
      }
    },

    /**
     * 切换场景并重新获取推荐
     * @param {string} scene
     * @param {Object} extraParams - 场景特有参数（itemId / keyword）
     */
    async switchScene(scene, extraParams = {}) {
      this.currentScene = scene
      await this.fetchRecommend(extraParams)
    },

    /**
     * 上报用户行为（点击/收藏等）
     * @param {Object} behaviorData
     */
    async reportBehavior(behaviorData) {
      try {
        if (this.useMock) {
          return { code: 0, data: { accepted: true, eventId: 'mock_' + Date.now() } }
        }
        return await behaviorApi.reportBehavior({
          userId: this.userId,
          scene: this.currentScene,
          eventTime: new Date().toISOString(),
          ...behaviorData
        })
      } catch (err) {
        console.error('[Behavior] 行为上报失败:', err)
        return null
      }
    },

    /**
     * 重置状态
     */
    reset() {
      this.result = null
      this.error = null
      this.loading = false
    }
  }
})

import { defineStore } from 'pinia'

/**
 * 应用全局 Store
 * 管理侧边栏折叠、主题、用户信息等全局状态
 */
export const useAppStore = defineStore('app', {
  state: () => ({
    // 侧边栏折叠状态
    sidebarCollapsed: false,
    // 当前模块: demo / admin
    currentModule: 'demo',
    // 页面加载进度
    pageLoading: false
  }),

  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
    setModule(module) {
      this.currentModule = module
    },
    setPageLoading(loading) {
      this.pageLoading = loading
    }
  }
})

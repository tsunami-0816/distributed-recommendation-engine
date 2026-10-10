<template>
  <el-container class="demo-layout">
    <!-- 顶部导航 -->
    <el-header class="demo-header">
      <div class="header-left">
        <el-icon class="logo-icon"><Cpu /></el-icon>
        <span class="logo-text">推荐引擎演示</span>
        <el-tag size="small" type="info" class="env-tag">Demo</el-tag>
      </div>
      <div class="header-center">
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          router
          class="demo-menu"
          :ellipsis="false"
        >
          <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-menu>
      </div>
      <div class="header-right">
        <span class="user-id-label">用户ID:</span>
        <el-input
          v-model="userId"
          size="small"
          class="user-id-input"
          placeholder="输入用户ID"
          @keyup.enter="handleUserIdChange"
        />
        <el-button size="small" type="primary" @click="handleUserIdChange">切换</el-button>
        <el-divider direction="vertical" />
        <el-button size="small" text @click="goAdmin">
          <el-icon><Setting /></el-icon>
          管理后台
        </el-button>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <el-main class="demo-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" :key="$route.path" />
        </transition>
      </router-view>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useRecommendStore } from '@/stores/recommend'

const route = useRoute()
const router = useRouter()
const recommendStore = useRecommendStore()

const userId = ref(recommendStore.userId)

// 导航菜单
const menuItems = [
  { path: '/demo/home', title: '首页推荐', icon: 'House' },
  { path: '/demo/guess', title: '猜你喜欢', icon: 'MagicStick' },
  { path: '/demo/related', title: '相关推荐', icon: 'Connection' },
  { path: '/demo/search', title: '搜索推荐', icon: 'Search' },
  { path: '/demo/hot', title: '热门榜单', icon: 'TrendCharts' },
  { path: '/demo/profile', title: '用户画像', icon: 'User' }
]

const activeMenu = computed(() => route.path)

// 切换用户ID
function handleUserIdChange() {
  if (!userId.value.trim()) {
    ElMessage.warning('请输入用户ID')
    return
  }
  recommendStore.userId = userId.value.trim()
  ElMessage.success(`已切换到用户: ${userId.value}`)
  // 触发当前页面刷新
  router.replace({ path: route.path, query: { ...route.query, _t: Date.now() } })
}

// 跳转到管理后台
function goAdmin() {
  router.push('/admin/dashboard')
}

// 监听路由变化，同步场景
watch(
  () => route.path,
  (newPath) => {
    const sceneMap = {
      '/demo/home': 'home',
      '/demo/guess': 'guess',
      '/demo/related': 'related',
      '/demo/search': 'search'
    }
    if (sceneMap[newPath]) {
      recommendStore.currentScene = sceneMap[newPath]
    }
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
.demo-layout {
  height: 100%;
}

.demo-header {
  display: flex;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid var(--border-color);
  padding: 0 20px;
  height: 60px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
  min-width: 200px;

  .logo-icon {
    font-size: 24px;
    color: var(--primary-color);
    margin-right: 8px;
  }

  .logo-text {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
    margin-right: 8px;
  }

  .env-tag {
    margin-left: 4px;
  }
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;

  .demo-menu {
    border-bottom: none;
  }
}

.header-right {
  display: flex;
  align-items: center;
  min-width: 320px;
  justify-content: flex-end;

  .user-id-label {
    font-size: 13px;
    color: var(--text-secondary);
    margin-right: 6px;
  }

  .user-id-input {
    width: 120px;
  }
}

.demo-main {
  background: var(--bg-color);
  padding: 0;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

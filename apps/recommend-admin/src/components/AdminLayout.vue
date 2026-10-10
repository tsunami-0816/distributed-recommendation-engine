<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="sidebarCollapsed ? '64px' : '220px'" class="admin-sidebar">
      <div class="sidebar-logo" @click="goHome">
        <el-icon class="logo-icon"><DataAnalysis /></el-icon>
        <span v-show="!sidebarCollapsed" class="logo-text">推荐引擎管理台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="sidebarCollapsed"
        router
        class="sidebar-menu"
        background-color="#001529"
        text-color="#b8c2cc"
        active-text-color="#409eff"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧主区域 -->
    <el-container>
      <!-- 顶部栏 -->
      <el-header class="admin-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleSidebar">
            <Fold v-if="!sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">管理后台</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tag :type="healthStatus === 'UP' ? 'success' : 'danger'" size="small" effect="dark">
            系统{{ healthStatus }}
          </el-tag>
          <el-divider direction="vertical" />
          <el-button size="small" text @click="goDemo">
            <el-icon><Monitor /></el-icon>
            推荐演示
          </el-button>
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="28" icon="UserFilled" />
              <span class="username">admin</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goDemo">推荐演示</el-dropdown-item>
                <el-dropdown-item divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="admin-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" :key="$route.path" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const healthStatus = ref('UP')

const menuItems = [
  { path: '/admin/dashboard', title: '系统概览', icon: 'DataAnalysis' },
  { path: '/admin/experiments', title: 'A/B 实验管理', icon: 'Histogram' },
  { path: '/admin/models', title: '模型管理', icon: 'Cpu' },
  { path: '/admin/configs', title: '系统配置', icon: 'Setting' },
  { path: '/admin/cache', title: '缓存管理', icon: 'Coin' },
  { path: '/admin/feature-consistency', title: '特征一致性', icon: 'Checked' }
]

const activeMenu = computed(() => route.path)
const currentPageTitle = computed(() => route.meta?.title || '')

function toggleSidebar() {
  appStore.toggleSidebar()
}

function goHome() {
  router.push('/admin/dashboard')
}

function goDemo() {
  router.push('/demo/home')
}
</script>

<style scoped lang="scss">
.admin-layout {
  height: 100%;
}

.admin-sidebar {
  background: #001529;
  transition: width 0.3s;
  overflow: hidden;
}

.sidebar-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);

  .logo-icon {
    font-size: 24px;
    color: #409eff;
  }

  .logo-text {
    color: #fff;
    font-size: 15px;
    font-weight: 600;
    margin-left: 10px;
    white-space: nowrap;
  }
}

.sidebar-menu {
  border-right: none;
  height: calc(100% - 60px);
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid var(--border-color);
  padding: 0 20px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;

  .collapse-btn {
    font-size: 20px;
    cursor: pointer;
    color: var(--text-regular);

    &:hover {
      color: var(--primary-color);
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;

  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;

    .username {
      font-size: 14px;
      color: var(--text-regular);
    }
  }
}

.admin-main {
  background: var(--bg-color);
  padding: 20px;
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

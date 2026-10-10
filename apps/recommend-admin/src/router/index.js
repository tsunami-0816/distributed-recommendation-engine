import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/demo/home'
  },
  // ===== 推荐演示前台 =====
  {
    path: '/demo',
    component: () => import('@/components/DemoLayout.vue'),
    redirect: '/demo/home',
    children: [
      {
        path: 'home',
        name: 'DemoHome',
        component: () => import('@/views/demo/HomeRecommend.vue'),
        meta: { title: '首页推荐', icon: 'House' }
      },
      {
        path: 'guess',
        name: 'DemoGuess',
        component: () => import('@/views/demo/GuessYouLike.vue'),
        meta: { title: '猜你喜欢', icon: 'MagicStick' }
      },
      {
        path: 'related',
        name: 'DemoRelated',
        component: () => import('@/views/demo/RelatedRecommend.vue'),
        meta: { title: '相关推荐', icon: 'Connection' }
      },
      {
        path: 'search',
        name: 'DemoSearch',
        component: () => import('@/views/demo/SearchRecommend.vue'),
        meta: { title: '搜索推荐', icon: 'Search' }
      },
      {
        path: 'hot',
        name: 'DemoHot',
        component: () => import('@/views/demo/HotItems.vue'),
        meta: { title: '热门榜单', icon: 'TrendCharts' }
      },
      {
        path: 'profile',
        name: 'DemoProfile',
        component: () => import('@/views/demo/UserProfile.vue'),
        meta: { title: '用户画像', icon: 'User' }
      }
    ]
  },
  // ===== 管理后台 =====
  {
    path: '/admin',
    component: () => import('@/components/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '系统概览', icon: 'DataAnalysis' }
      },
      {
        path: 'experiments',
        name: 'AdminExperiments',
        component: () => import('@/views/admin/AbExperiments.vue'),
        meta: { title: 'A/B 实验管理', icon: 'Histogram' }
      },
      {
        path: 'models',
        name: 'AdminModels',
        component: () => import('@/views/admin/ModelManagement.vue'),
        meta: { title: '模型管理', icon: 'Cpu' }
      },
      {
        path: 'configs',
        name: 'AdminConfigs',
        component: () => import('@/views/admin/SystemConfig.vue'),
        meta: { title: '系统配置', icon: 'Setting' }
      },
      {
        path: 'cache',
        name: 'AdminCache',
        component: () => import('@/views/admin/CacheManage.vue'),
        meta: { title: '缓存管理', icon: 'Coin' }
      },
      {
        path: 'feature-consistency',
        name: 'AdminFeatureConsistency',
        component: () => import('@/views/admin/FeatureConsistency.vue'),
        meta: { title: '特征一致性', icon: 'Checked' }
      }
    ]
  },
  // 404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫：设置页面标题
router.beforeEach((to, from, next) => {
  const title = to.meta?.title
  if (title) {
    document.title = `${title} - 推荐引擎管理台`
  } else {
    document.title = '推荐引擎管理台'
  }
  next()
})

export default router

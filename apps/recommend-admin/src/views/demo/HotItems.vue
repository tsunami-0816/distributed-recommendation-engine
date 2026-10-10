<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">热门榜单</div>
      <div class="page-desc">基于近24小时点击量与收藏量计算的热度排行榜（热度分 = click×0.7 + favorite×0.3）</div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar common-card">
      <el-form :inline="true" :model="queryForm" @submit.prevent>
        <el-form-item label="类目筛选">
          <el-select v-model="queryForm.categoryId" placeholder="全部类目" clearable style="width: 180px">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="返回数量">
          <el-input-number v-model="queryForm.limit" :min="1" :max="100" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Refresh" :loading="loading" @click="loadData">
            刷新榜单
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 榜单内容 -->
    <div class="hot-list mt-16">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-wrap">
        <el-skeleton v-for="i in 5" :key="i" :rows="2" animated class="skeleton-row" />
      </div>

      <!-- 空状态 -->
      <el-empty v-else-if="!hotItems.length" description="暂无热门数据" />

      <!-- 榜单列表 -->
      <template v-else>
        <!-- Top 3 突出展示 -->
        <div class="top-three">
          <div
            v-for="item in hotItems.slice(0, 3)"
            :key="item.itemId"
            class="top-card"
            :class="`top-${item.rank}`"
          >
            <div class="top-rank">{{ item.rank }}</div>
            <div class="top-icon">
              <el-icon :size="36"><Trophy v-if="item.rank === 1" /><Medal v-else /></el-icon>
            </div>
            <div class="top-name">{{ item.item.name }}</div>
            <div class="top-score">热度: {{ item.hotScore.toFixed(2) }}</div>
            <div class="top-stats">
              <span><el-icon><View /></el-icon> {{ formatNumber(item.clickCount) }}</span>
              <span><el-icon><Star /></el-icon> {{ formatNumber(item.favoriteCount) }}</span>
            </div>
          </div>
        </div>

        <!-- 4名以后列表 -->
        <el-table v-if="hotItems.length > 3" :data="hotItems.slice(3)" class="hot-table" stripe>
          <el-table-column label="排名" width="80" align="center">
            <template #default="{ row }">
              <span class="rank-num">{{ row.rank }}</span>
            </template>
          </el-table-column>
          <el-table-column label="物品名称" min-width="200">
            <template #default="{ row }">
              <span class="item-name">{{ row.item.name }}</span>
            </template>
          </el-table-column>
          <el-table-column label="类目" width="120" align="center">
            <template #default="{ row }">
              <el-tag size="small">{{ row.item.categoryId }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="价格" width="120" align="right">
            <template #default="{ row }">
              <span class="price">{{ formatPrice(row.item.price) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="点击量" width="120" align="right">
            <template #default="{ row }">{{ formatNumber(row.clickCount) }}</template>
          </el-table-column>
          <el-table-column label="收藏量" width="120" align="right">
            <template #default="{ row }">{{ formatNumber(row.favoriteCount) }}</template>
          </el-table-column>
          <el-table-column label="热度分" width="120" align="right">
            <template #default="{ row }">
              <el-progress
                :percentage="row.hotScore * 100"
                :stroke-width="8"
                :show-text="false"
                color="#f56c6c"
              />
              <span class="hot-score-text">{{ row.hotScore.toFixed(2) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { Refresh, Trophy, Medal, View, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { hotItemsApi } from '@/api'
import { mockHotItems } from '@/mock'
import { formatNumber, formatPrice } from '@/utils'

const loading = ref(false)
const hotItems = ref([])
const useMock = import.meta.env.VITE_USE_MOCK === 'true'

const queryForm = reactive({
  categoryId: null,
  limit: 20
})

const categories = [
  { id: 101, name: '电子产品/音频' },
  { id: 102, name: '电子产品/外设' },
  { id: 103, name: '电子产品/穿戴' },
  { id: 104, name: '电子产品/显示' },
  { id: 201, name: '家居/办公' }
]

async function loadData() {
  loading.value = true
  try {
    if (useMock) {
      await new Promise((resolve) => setTimeout(resolve, 300))
      let data = mockHotItems.data
      if (queryForm.categoryId) {
        data = data.filter((item) => item.item.categoryId === queryForm.categoryId)
      }
      hotItems.value = data.slice(0, queryForm.limit)
    } else {
      const res = await hotItemsApi.getHotItems({
        categoryId: queryForm.categoryId,
        limit: queryForm.limit
      })
      hotItems.value = res.data || []
    }
  } catch (err) {
    ElMessage.error('获取热门榜单失败')
    if (!useMock) {
      hotItems.value = mockHotItems.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.filter-bar {
  padding: 16px 20px;
}

.loading-wrap {
  .skeleton-row {
    padding: 16px;
    background: #fff;
    border-radius: 8px;
    margin-bottom: 12px;
  }
}

.top-three {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.top-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  position: relative;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-4px);
  }

  &.top-1 {
    background: linear-gradient(135deg, #fff9e6 0%, #fff3cd 100%);
    border: 2px solid #ffd700;
  }

  &.top-2 {
    background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
    border: 2px solid #c0c0c0;
  }

  &.top-3 {
    background: linear-gradient(135deg, #faf0e6 0%, #f5deb3 100%);
    border: 2px solid #cd7f32;
  }
}

.top-rank {
  position: absolute;
  top: 12px;
  left: 16px;
  font-size: 28px;
  font-weight: 800;
  color: rgba(0, 0, 0, 0.15);
}

.top-icon {
  margin-bottom: 12px;
  color: #f56c6c;
}

.top-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}

.top-score {
  font-size: 14px;
  color: #f56c6c;
  font-weight: 600;
  margin-bottom: 8px;
}

.top-stats {
  display: flex;
  justify-content: center;
  gap: 16px;
  font-size: 13px;
  color: var(--text-secondary);

  span {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.hot-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.rank-num {
  font-weight: 700;
  color: var(--text-secondary);
}

.item-name {
  font-weight: 500;
}

.price {
  color: #f56c6c;
  font-weight: 600;
}

.hot-score-text {
  font-size: 12px;
  color: var(--text-secondary);
  margin-left: 4px;
}
</style>

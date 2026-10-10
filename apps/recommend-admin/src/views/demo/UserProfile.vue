<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">用户画像</div>
      <div class="page-desc">长期画像 + 短期画像双字段隔离，加权融合（短期0.6 / 长期0.4）生成用户标签</div>
    </div>

    <!-- 用户信息栏 -->
    <div class="user-bar common-card">
      <div class="user-avatar">
        <el-avatar :size="56" icon="UserFilled" />
      </div>
      <div class="user-info">
        <div class="user-id">{{ profile?.userId || '-' }}</div>
        <div class="user-meta">
          <el-tag size="small" type="info">注册 {{ profile?.longTerm?.registerDays || 0 }} 天</el-tag>
          <el-tag size="small" type="success">活跃度 {{ (profile?.longTerm?.activityLevel * 100).toFixed(0) }}%</el-tag>
          <el-tag size="small" type="warning">价格偏好: {{ profile?.longTerm?.pricePreference || '-' }}</el-tag>
          <el-tag size="small">画像版本: {{ profile?.version || '-' }}</el-tag>
        </div>
      </div>
      <div class="user-actions">
        <el-button type="primary" :icon="Refresh" :loading="loading" @click="loadData">刷新画像</el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-wrap mt-16">
      <el-skeleton :rows="8" animated class="skeleton-card" />
    </div>

    <!-- 画像内容 -->
    <div v-else-if="profile" class="profile-content mt-16">
      <el-row :gutter="16">
        <!-- 长期画像 -->
        <el-col :span="12">
          <div class="common-card profile-card">
            <div class="card-title">
              <el-icon color="#409eff"><Histogram /></el-icon>
              长期画像（近30天）
            </div>

            <!-- 类目偏好 -->
            <div class="profile-section">
              <div class="section-label">类目偏好 Top</div>
              <div class="bar-list">
                <div v-for="(weight, catId) in sortedCategoryPrefs" :key="catId" class="bar-item">
                  <span class="bar-label">类目 {{ catId }}</span>
                  <div class="bar-track">
                    <div class="bar-fill" :style="{ width: `${weight * 100}%` }"></div>
                  </div>
                  <span class="bar-value">{{ (weight * 100).toFixed(0) }}%</span>
                </div>
              </div>
            </div>

            <!-- 标签权重 -->
            <div class="profile-section">
              <div class="section-label">标签权重 Top</div>
              <div class="tag-cloud">
                <el-tag
                  v-for="(weight, tag) in profile.longTerm?.tagWeights"
                  :key="tag"
                  :size="weight > 0.6 ? 'large' : weight > 0.4 ? 'default' : 'small'"
                  :type="weight > 0.6 ? 'danger' : weight > 0.4 ? 'warning' : 'info'"
                  effect="light"
                  class="profile-tag"
                >
                  {{ tag }} ({{ weight.toFixed(2) }})
                </el-tag>
              </div>
            </div>

            <!-- 活跃时段 -->
            <div class="profile-section">
              <div class="section-label">活跃时段分布</div>
              <div class="active-hours">
                <div
                  v-for="h in 24"
                  :key="h"
                  class="hour-bar"
                  :style="{ height: `${(profile.longTerm?.activeHours?.[h] || 0) * 300 + 4}px` }"
                  :title="`${h}时: ${((profile.longTerm?.activeHours?.[h] || 0) * 100).toFixed(1)}%`"
                ></div>
              </div>
              <div class="hour-labels">
                <span>0时</span><span>6时</span><span>12时</span><span>18时</span><span>23时</span>
              </div>
            </div>
          </div>
        </el-col>

        <!-- 短期画像 + 融合标签 -->
        <el-col :span="12">
          <div class="common-card profile-card">
            <div class="card-title">
              <el-icon color="#67c23a"><Lightning /></el-icon>
              短期画像（近1小时，TTL 5s）
            </div>

            <!-- 近期行为统计 -->
            <div class="profile-section">
              <div class="section-label">近期行为统计</div>
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="近期行为数">
                  {{ profile.shortTerm?.recentActionCount || 0 }}
                </el-descriptions-item>
                <el-descriptions-item label="近期点击物品">
                  {{ profile.shortTerm?.recentClickedItems?.length || 0 }} 个
                </el-descriptions-item>
                <el-descriptions-item label="近期收藏物品">
                  {{ profile.shortTerm?.recentFavoritedItems?.length || 0 }} 个
                </el-descriptions-item>
                <el-descriptions-item label="近期活跃类目">
                  {{ Object.keys(profile.shortTerm?.recentCategories || {}).length }} 个
                </el-descriptions-item>
              </el-descriptions>
            </div>

            <!-- 近期点击物品 -->
            <div class="profile-section">
              <div class="section-label">近期点击物品</div>
              <div class="item-tags">
                <el-tag
                  v-for="itemId in profile.shortTerm?.recentClickedItems"
                  :key="itemId"
                  size="small"
                  type="primary"
                  effect="plain"
                  class="item-id-tag"
                >
                  {{ itemId }}
                </el-tag>
              </div>
            </div>

            <!-- 近期类目 -->
            <div class="profile-section">
              <div class="section-label">近期类目交互次数</div>
              <div class="bar-list">
                <div v-for="(count, catId) in profile.shortTerm?.recentCategories" :key="catId" class="bar-item">
                  <span class="bar-label">类目 {{ catId }}</span>
                  <div class="bar-track">
                    <div class="bar-fill bar-short" :style="{ width: `${Math.min(count * 20, 100)}%` }"></div>
                  </div>
                  <span class="bar-value">{{ count }} 次</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 融合标签 -->
          <div class="common-card profile-card mt-16">
            <div class="card-title">
              <el-icon color="#e6a23c"><MagicStick /></el-icon>
              融合标签（短期0.6 + 长期0.4）
            </div>
            <div class="tag-cloud">
              <el-tag
                v-for="(weight, tag) in profile.fusedTags"
                :key="tag"
                :size="weight > 0.65 ? 'large' : 'default'"
                type="warning"
                effect="dark"
                class="profile-tag"
              >
                {{ tag }} ({{ weight.toFixed(2) }})
              </el-tag>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Refresh, Histogram, Lightning, MagicStick } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { userProfileApi } from '@/api'
import { mockUserProfile } from '@/mock'
import { useRecommendStore } from '@/stores/recommend'

const recommendStore = useRecommendStore()
const loading = ref(false)
const profile = ref(null)
const useMock = import.meta.env.VITE_USE_MOCK === 'true'

// 类目偏好按权重排序
const sortedCategoryPrefs = computed(() => {
  const prefs = profile.value?.longTerm?.categoryPreferences || {}
  return Object.fromEntries(Object.entries(prefs).sort((a, b) => b[1] - a[1]))
})

async function loadData() {
  loading.value = true
  try {
    const userId = recommendStore.userId
    if (useMock) {
      await new Promise((resolve) => setTimeout(resolve, 300))
      profile.value = { ...mockUserProfile.data, userId }
    } else {
      const res = await userProfileApi.getUserProfile(userId)
      profile.value = res.data
    }
  } catch (err) {
    ElMessage.error('获取用户画像失败')
    if (!useMock) profile.value = mockUserProfile.data
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.user-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.user-avatar {
  flex-shrink: 0;
}

.user-info {
  flex: 1;

  .user-id {
    font-size: 20px;
    font-weight: 700;
    margin-bottom: 8px;
  }

  .user-meta {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
}

.loading-wrap {
  .skeleton-card {
    padding: 20px;
    background: #fff;
    border-radius: 8px;
  }
}

.profile-card {
  padding: 20px;
  height: 100%;
}

.profile-section {
  margin-bottom: 20px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 10px;
  font-weight: 500;
}

.bar-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.bar-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bar-label {
  width: 70px;
  font-size: 12px;
  color: var(--text-regular);
  flex-shrink: 0;
}

.bar-track {
  flex: 1;
  height: 16px;
  background: #f0f2f5;
  border-radius: 8px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #66b1ff);
  border-radius: 8px;
  transition: width 0.3s;

  &.bar-short {
    background: linear-gradient(90deg, #67c23a, #85ce61);
  }
}

.bar-value {
  width: 50px;
  font-size: 12px;
  color: var(--text-secondary);
  text-align: right;
  flex-shrink: 0;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.profile-tag {
  margin: 0;
}

.active-hours {
  display: flex;
  align-items: flex-end;
  gap: 2px;
  height: 80px;
  padding: 8px 0;
}

.hour-bar {
  flex: 1;
  background: linear-gradient(180deg, #409eff, #79bbff);
  border-radius: 2px 2px 0 0;
  min-height: 4px;
  transition: height 0.3s;
}

.hour-labels {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.item-id-tag {
  font-family: monospace;
}
</style>

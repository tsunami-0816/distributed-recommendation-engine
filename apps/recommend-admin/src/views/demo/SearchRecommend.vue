<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">搜索推荐</div>
      <div class="page-desc">基于关键词的 ES 文本检索 + 语义检索 RRF 融合推荐</div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar common-card">
      <el-input
        v-model="keyword"
        size="large"
        placeholder="输入搜索关键词，例如: 耳机、键盘、智能手表"
        :prefix-icon="Search"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleClear"
      >
        <template #append>
          <el-button type="primary" :loading="recommendStore.loading" @click="handleSearch">
            搜索
          </el-button>
        </template>
      </el-input>

      <!-- 热门搜索词 -->
      <div class="hot-keywords mt-8">
        <span class="hk-label">热门搜索:</span>
        <el-tag
          v-for="kw in hotKeywords"
          :key="kw"
          class="hk-tag"
          effect="plain"
          @click="selectKeyword(kw)"
        >
          {{ kw }}
        </el-tag>
      </div>
    </div>

    <!-- 搜索结果统计 -->
    <div v-if="hasSearched && !recommendStore.loading" class="result-stats mt-16">
      <el-alert
        :title="`搜索“${keyword}”共找到 ${recommendStore.items.length} 条相关推荐，候选数 ${recommendStore.candidateCount}，耗时 ${recommendStore.totalMs}ms`"
        type="info"
        :closable="false"
        show-icon
      />
    </div>

    <!-- 推荐结果 -->
    <div class="mt-16">
      <RecommendResult
        v-if="hasSearched"
        :items="recommendStore.items"
        :loading="recommendStore.loading"
        :scene="recommendStore.currentScene"
        :ab-group="recommendStore.abGroup"
        :model-version="recommendStore.modelVersion"
        :candidate-count="recommendStore.candidateCount"
        :total-ms="recommendStore.totalMs"
        :duration="recommendStore.duration"
        @refresh="handleSearch"
      />
    </div>

    <!-- 初始空状态 -->
    <el-empty
      v-if="!hasSearched"
      description="输入关键词开始搜索推荐"
      :image-size="120"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRecommendStore } from '@/stores/recommend'
import RecommendResult from '@/components/RecommendResult.vue'

const recommendStore = useRecommendStore()

const keyword = ref('')
const hasSearched = ref(false)

const hotKeywords = ['耳机', '机械键盘', '智能手表', '显示器', '人体工学椅', '蓝牙音箱']

function selectKeyword(kw) {
  keyword.value = kw
  handleSearch()
}

function handleClear() {
  hasSearched.value = false
}

async function handleSearch() {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  hasSearched.value = true
  await recommendStore.fetchRecommend({
    scene: 'search',
    keyword: keyword.value.trim(),
    pageSize: 20
  })
}
</script>

<style scoped lang="scss">
.search-bar {
  padding: 20px;
}

.hot-keywords {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;

  .hk-label {
    font-size: 13px;
    color: var(--text-secondary);
  }

  .hk-tag {
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: var(--primary-color);
      color: #fff;
      border-color: var(--primary-color);
    }
  }
}

.result-stats {
  :deep(.el-alert) {
    border-radius: 8px;
  }
}
</style>

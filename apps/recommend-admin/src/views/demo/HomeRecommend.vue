<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">首页推荐</div>
      <div class="page-desc">基于用户长期画像 + 短期行为的多路召回推荐（ItemCF / 标签 / 画像 / 热门）</div>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar common-card">
      <el-form :inline="true" :model="queryForm" @submit.prevent>
        <el-form-item label="返回数量">
          <el-input-number v-model="queryForm.pageSize" :min="1" :max="50" size="default" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Refresh" :loading="recommendStore.loading" @click="loadData">
            刷新推荐
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 推荐结果 -->
    <div class="mt-16">
      <RecommendResult
        :items="recommendStore.items"
        :loading="recommendStore.loading"
        :scene="recommendStore.currentScene"
        :ab-group="recommendStore.abGroup"
        :model-version="recommendStore.modelVersion"
        :candidate-count="recommendStore.candidateCount"
        :total-ms="recommendStore.totalMs"
        :duration="recommendStore.duration"
        @refresh="loadData"
      />
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { useRecommendStore } from '@/stores/recommend'
import RecommendResult from '@/components/RecommendResult.vue'

const recommendStore = useRecommendStore()

const queryForm = reactive({
  pageSize: 20
})

async function loadData() {
  await recommendStore.fetchRecommend({
    scene: 'home',
    pageSize: queryForm.pageSize
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.action-bar {
  padding: 16px 20px;
}
</style>

<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">相关推荐</div>
      <div class="page-desc">基于指定物品的相似度推荐（ItemCF + 内容标签 + ES 文本检索），无需用户ID</div>
    </div>

    <!-- 参数输入 -->
    <div class="action-bar common-card">
      <el-form :inline="true" :model="queryForm" :rules="rules" ref="formRef" @submit.prevent>
        <el-form-item label="参考物品ID" prop="itemId">
          <el-input
            v-model="queryForm.itemId"
            placeholder="例如: i_16094"
            style="width: 200px"
            clearable
          />
        </el-form-item>
        <el-form-item label="返回数量">
          <el-input-number v-model="queryForm.pageSize" :min="1" :max="50" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" :loading="recommendStore.loading" @click="handleSearch">
            获取相关推荐
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 参考物品信息 -->
    <div v-if="referenceItem" class="reference-card common-card mt-16">
      <div class="ref-label">参考物品</div>
      <div class="ref-content">
        <el-icon :size="32" color="#c0c4cc"><Goods /></el-icon>
        <div class="ref-info">
          <div class="ref-name">{{ referenceItem.name }}</div>
          <div class="ref-desc">{{ referenceItem.description }}</div>
          <div class="ref-meta">
            <span class="ref-price">{{ formatPrice(referenceItem.price) }}</span>
            <el-tag size="small">{{ referenceItem.categoryPath }}</el-tag>
          </div>
        </div>
      </div>
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
        @refresh="handleSearch"
      />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRecommendStore } from '@/stores/recommend'
import RecommendResult from '@/components/RecommendResult.vue'
import { formatPrice } from '@/utils'

const recommendStore = useRecommendStore()
const formRef = ref(null)

const queryForm = reactive({
  itemId: 'i_16094',
  pageSize: 20
})

const rules = {
  itemId: [{ required: true, message: '请输入参考物品ID', trigger: 'blur' }]
}

// 从推荐结果中找到参考物品（如果返回的列表中包含）
const referenceItem = computed(() => {
  const items = recommendStore.items
  if (!items || items.length === 0) return null
  // 实际场景中参考物品不会出现在推荐列表中，这里用第一个物品展示
  return items[0]?.item || null
})

async function handleSearch() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请填写参考物品ID')
    return
  }
  await recommendStore.fetchRecommend({
    scene: 'related',
    itemId: queryForm.itemId,
    pageSize: queryForm.pageSize
  })
}
</script>

<style scoped lang="scss">
.action-bar {
  padding: 16px 20px;
}

.reference-card {
  padding: 16px 20px;

  .ref-label {
    font-size: 13px;
    color: var(--text-secondary);
    margin-bottom: 12px;
  }

  .ref-content {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .ref-info {
    flex: 1;
  }

  .ref-name {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 4px;
  }

  .ref-desc {
    font-size: 13px;
    color: var(--text-secondary);
    margin-bottom: 8px;
  }

  .ref-meta {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .ref-price {
    font-size: 18px;
    font-weight: 700;
    color: #f56c6c;
  }
}
</style>

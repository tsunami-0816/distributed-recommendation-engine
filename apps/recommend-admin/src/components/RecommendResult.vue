<template>
  <div class="recommend-result">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-wrap">
      <el-skeleton v-for="i in 8" :key="i" :rows="3" animated class="skeleton-card" />
    </div>

    <!-- 空状态 -->
    <el-empty
      v-else-if="!items || items.length === 0"
      description="暂无推荐结果，请尝试切换用户或场景"
    >
      <el-button type="primary" @click="$emit('refresh')">重新加载</el-button>
    </el-empty>

    <!-- 推荐结果 -->
    <template v-else>
      <!-- 结果元信息 -->
      <div class="result-meta">
        <el-space wrap>
          <el-tag type="info" effect="plain">
            场景: <strong>{{ scene }}</strong>
          </el-tag>
          <el-tag type="success" effect="plain">
            A/B组: <strong>{{ abGroup }}</strong>
          </el-tag>
          <el-tag type="warning" effect="plain">
            模型: <strong>{{ modelVersion }}</strong>
          </el-tag>
          <el-tag effect="plain">
            候选数: <strong>{{ candidateCount }}</strong>
          </el-tag>
          <el-tag v-if="totalMs" type="primary" effect="plain">
            耗时: <strong>{{ totalMs }}ms</strong>
          </el-tag>
        </el-space>
      </div>

      <!-- 各阶段耗时（可展开） -->
      <el-collapse v-if="duration" class="duration-collapse">
        <el-collapse-item title="各阶段耗时明细" name="duration">
          <el-descriptions :column="4" border size="small">
            <el-descriptions-item label="缓存">
              {{ duration.cacheMs }}ms
            </el-descriptions-item>
            <el-descriptions-item label="召回">
              {{ duration.recallMs }}ms
            </el-descriptions-item>
            <el-descriptions-item label="特征组装">
              {{ duration.featureMs }}ms
            </el-descriptions-item>
            <el-descriptions-item label="精排">
              {{ duration.rankMs }}ms
            </el-descriptions-item>
            <el-descriptions-item label="重排">
              {{ duration.rerankMs }}ms
            </el-descriptions-item>
            <el-descriptions-item label="总计">
              <strong>{{ duration.totalMs }}ms</strong>
            </el-descriptions-item>
          </el-descriptions>
        </el-collapse-item>
      </el-collapse>

      <!-- 物品卡片网格 -->
      <div class="items-grid">
        <ItemCard
          v-for="item in items"
          :key="item.itemId"
          :item="item.item"
          :rank="item.rank"
          :score="item.score"
          :recall-channel="item.recallChannel"
        />
      </div>
    </template>
  </div>
</template>

<script setup>
import ItemCard from './ItemCard.vue'

defineProps({
  items: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  scene: { type: String, default: '-' },
  abGroup: { type: String, default: '-' },
  modelVersion: { type: String, default: '-' },
  candidateCount: { type: Number, default: 0 },
  totalMs: { type: Number, default: 0 },
  duration: { type: Object, default: null }
})

defineEmits(['refresh'])
</script>

<style scoped lang="scss">
.recommend-result {
  width: 100%;
}

.loading-wrap {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;

  .skeleton-card {
    padding: 16px;
    background: #fff;
    border-radius: 8px;
  }
}

.result-meta {
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
}

.duration-collapse {
  margin-bottom: 16px;
  background: #fff;
  border-radius: 8px;
  padding: 0 16px;

  :deep(.el-collapse-item__header) {
    font-size: 13px;
    color: var(--text-secondary);
  }
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
</style>

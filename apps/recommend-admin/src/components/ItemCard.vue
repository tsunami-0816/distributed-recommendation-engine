<template>
  <el-card class="item-card" shadow="hover" @click="handleClick">
    <!-- 物品图片占位 -->
    <div class="item-image">
      <el-icon :size="48" color="#c0c4cc"><Goods /></el-icon>
    </div>

    <!-- 物品信息 -->
    <div class="item-info">
      <div class="item-name" :title="item.name">{{ item.name }}</div>
      <div class="item-desc" :title="item.description">{{ item.description }}</div>

      <!-- 价格与热度 -->
      <div class="item-meta">
        <span class="item-price">{{ formatPrice(item.price) }}</span>
        <el-tooltip content="热度分" placement="top">
          <span class="hot-score">
            <el-icon><TrendCharts /></el-icon>
            {{ item.hotScore?.toFixed(2) }}
          </span>
        </el-tooltip>
      </div>

      <!-- 召回通道标签 -->
      <div v-if="recallChannel && recallChannel.length" class="recall-tags">
        <el-tag
          v-for="channel in recallChannel"
          :key="channel"
          size="small"
          :class="`tag-${channel}`"
          effect="plain"
        >
          {{ RECALL_CHANNEL_MAP[channel] || channel }}
        </el-tag>
      </div>
    </div>

    <!-- 排名角标 -->
    <div v-if="rank" class="rank-badge" :class="rankClass">
      {{ rank }}
    </div>

    <!-- 推荐分数 -->
    <div v-if="score !== undefined" class="score-badge">
      {{ score.toFixed(4) }}
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { formatPrice, RECALL_CHANNEL_MAP } from '@/utils'
import { useRecommendStore } from '@/stores/recommend'

const props = defineProps({
  // 物品详情
  item: {
    type: Object,
    required: true
  },
  // 排名
  rank: {
    type: Number,
    default: null
  },
  // 推荐分数
  score: {
    type: Number,
    default: undefined
  },
  // 召回通道
  recallChannel: {
    type: Array,
    default: () => []
  }
})

const recommendStore = useRecommendStore()

// 排名样式（前3名特殊颜色）
const rankClass = computed(() => {
  if (props.rank === 1) return 'rank-gold'
  if (props.rank === 2) return 'rank-silver'
  if (props.rank === 3) return 'rank-bronze'
  return ''
})

// 点击物品：上报点击行为
async function handleClick() {
  await recommendStore.reportBehavior({
    itemId: props.item.itemId,
    actionType: 'click',
    extra: { position: props.rank, device: 'web' }
  })
  ElMessage.success(`已点击: ${props.item.name}`)
}
</script>

<style scoped lang="scss">
.item-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border-radius: 8px;
  overflow: hidden;

  &:hover {
    transform: translateY(-2px);
  }

  :deep(.el-card__body) {
    padding: 0;
  }
}

.item-image {
  height: 140px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-info {
  padding: 12px;
}

.item-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.item-desc {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 8px;
}

.item-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.item-price {
  font-size: 16px;
  font-weight: 700;
  color: #f56c6c;
}

.hot-score {
  font-size: 12px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 2px;
}

.recall-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.rank-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.rank-gold {
  background: linear-gradient(135deg, #ffd700, #ffaa00);
}

.rank-silver {
  background: linear-gradient(135deg, #c0c0c0, #a0a0a0);
}

.rank-bronze {
  background: linear-gradient(135deg, #cd7f32, #b87333);
}

.score-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(64, 158, 255, 0.9);
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  font-family: monospace;
}
</style>

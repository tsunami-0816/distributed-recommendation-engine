<template>
  <el-card class="stat-card" shadow="hover">
    <div class="stat-content">
      <div class="stat-icon" :style="{ background: iconBg }">
        <el-icon :size="24" :color="iconColor"><component :is="icon" /></el-icon>
      </div>
      <div class="stat-info">
        <div class="stat-label">{{ label }}</div>
        <div class="stat-value">
          {{ displayValue }}
          <span v-if="unit" class="stat-unit">{{ unit }}</span>
        </div>
        <div v-if="trend !== undefined" class="stat-trend" :class="trendClass">
          <el-icon><CaretTop v-if="trend >= 0" /><CaretBottom v-else /></el-icon>
          {{ Math.abs(trend).toFixed(1) }}%
          <span class="trend-label">较昨日</span>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { formatNumber, formatPercent } from '@/utils'

const props = defineProps({
  label: { type: String, required: true },
  value: { type: [Number, String], required: true },
  icon: { type: String, default: 'DataAnalysis' },
  iconColor: { type: String, default: '#409eff' },
  iconBg: { type: String, default: 'rgba(64, 158, 255, 0.1)' },
  unit: { type: String, default: '' },
  // 趋势百分比，正数上升，负数下降
  trend: { type: Number, default: undefined },
  // 值格式: number / percent / raw
  format: { type: String, default: 'number' }
})

const displayValue = computed(() => {
  if (props.format === 'percent') return formatPercent(props.value)
  if (props.format === 'number' && typeof props.value === 'number') return formatNumber(props.value)
  return props.value
})

const trendClass = computed(() => (props.trend >= 0 ? 'trend-up' : 'trend-down'))
</script>

<style scoped lang="scss">
.stat-card {
  border-radius: 8px;
  border: none;

  :deep(.el-card__body) {
    padding: 20px;
  }
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-unit {
  font-size: 13px;
  font-weight: 400;
  color: var(--text-secondary);
  margin-left: 2px;
}

.stat-trend {
  font-size: 12px;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 2px;

  &.trend-up {
    color: #67c23a;
  }

  &.trend-down {
    color: #f56c6c;
  }

  .trend-label {
    color: var(--text-secondary);
    margin-left: 4px;
  }
}
</style>

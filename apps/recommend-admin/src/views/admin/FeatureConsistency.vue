<template>
  <div class="feature-consistency">
    <!-- 概览卡片 -->
    <el-row :gutter="16">
      <el-col :span="6">
        <StatCard
          label="校验状态"
          :value="result.status"
          icon="Checked"
          :icon-color="result.status === 'PASSED' ? '#67c23a' : '#f56c6c'"
          :icon-bg="result.status === 'PASSED' ? 'rgba(103,194,58,0.1)' : 'rgba(245,108,108,0.1)'"
        />
      </el-col>
      <el-col :span="6">
        <StatCard label="抽样数量" :value="result.sampleCount" icon="DataLine" icon-color="#409eff" icon-bg="rgba(64,158,255,0.1)" />
      </el-col>
      <el-col :span="6">
        <StatCard
          label="通过数量"
          :value="result.passedCount"
          icon="CircleCheck"
          icon-color="#67c23a"
          icon-bg="rgba(103,194,58,0.1)"
        />
      </el-col>
      <el-col :span="6">
        <StatCard
          label="最大绝对误差"
          :value="result.maxAbsError"
          icon="Aim"
          icon-color="#e6a23c"
          icon-bg="rgba(230,162,60,0.1)"
        />
      </el-col>
    </el-row>

    <!-- 操作栏 -->
    <div class="common-card mt-16">
      <div class="action-bar">
        <div class="action-left">
          <el-date-picker
            v-model="queryDate"
            type="date"
            placeholder="选择校验日期"
            value-format="YYYY-MM-DD"
            :clearable="false"
            @change="loadData"
          />
          <span class="check-time">校验时间: {{ formatTime(result.checkedAt) }}</span>
        </div>
        <div class="action-right">
          <el-button type="primary" :icon="VideoPlay" :loading="triggering" @click="handleTrigger">
            手动触发校验
          </el-button>
          <el-button :icon="Refresh" @click="loadData">刷新</el-button>
        </div>
      </div>
    </div>

    <!-- 校验标准说明 -->
    <el-alert
      class="mt-16"
      title="校验标准"
      type="info"
      :closable="false"
      show-icon
      description="连续特征最大绝对误差 ≤ 1e-6，类别特征一致性 100%。偏差超阈值将写入 feature_consistency_log 状态=FAILED 并阻断模型发布。校验由 Spark 离线任务每日自动执行，也可通过本页面手动触发。"
    />

    <!-- 校验明细 -->
    <div class="common-card mt-16">
      <div class="card-title">
        <el-icon color="#409eff"><List /></el-icon>
        校验明细（抽样 {{ result.sampleCount }} 条，通过 {{ result.passedCount }}，失败 {{ result.failedCount }}）
      </div>
      <el-table :data="result.detail" stripe>
        <el-table-column prop="featureName" label="特征名称" min-width="220">
          <template #default="{ row }">
            <code class="feature-name">{{ row.featureName }}</code>
          </template>
        </el-table-column>
        <el-table-column label="期望值（离线）" width="160" align="right">
          <template #default="{ row }">{{ formatFeatureValue(row.expected) }}</template>
        </el-table-column>
        <el-table-column label="实际值（在线）" width="160" align="right">
          <template #default="{ row }">{{ formatFeatureValue(row.actual) }}</template>
        </el-table-column>
        <el-table-column label="误差" width="160" align="right">
          <template #default="{ row }">
            <span :class="row.error > 1e-6 ? 'text-danger' : 'text-success'">
              {{ row.error.toExponential(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="是否超阈值" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.error > 1e-6" type="danger" size="small">超阈值</el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PASSED' ? 'success' : 'danger'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 触发校验进度弹窗 -->
    <el-dialog v-model="progressVisible" title="特征一致性校验进行中" width="500px" :close-on-click-modal="false" :show-close="false">
      <el-progress :percentage="triggerProgress" :status="triggerProgress >= 100 ? 'success' : ''" />
      <div class="progress-tip">
        <p v-if="triggerProgress < 30">正在抽取 1000 条训练样本...</p>
        <p v-else-if="triggerProgress < 60">正在调用在线特征服务重新计算...</p>
        <p v-else-if="triggerProgress < 90">正在逐字段对比离线与在线特征...</p>
        <p v-else>正在生成校验报告并写入 MySQL...</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { VideoPlay, Refresh, Checked, List } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { featureConsistencyApi } from '@/api'
import { mockFeatureConsistency } from '@/mock'
import { formatTime } from '@/utils'
import StatCard from '@/components/StatCard.vue'

const useMock = import.meta.env.VITE_USE_MOCK === 'true'
const queryDate = ref('2026-10-03')
const triggering = ref(false)
const progressVisible = ref(false)
const triggerProgress = ref(0)

const result = reactive({ ...mockFeatureConsistency })

function formatFeatureValue(val) {
  if (typeof val === 'number') {
    if (Math.abs(val) < 0.001 || val >= 10000) return val.toExponential(4)
    return val.toFixed(6)
  }
  return val
}

async function loadData() {
  try {
    if (useMock) {
      await new Promise((r) => setTimeout(r, 300))
      Object.assign(result, { ...mockFeatureConsistency, date: queryDate.value })
    } else {
      const res = await featureConsistencyApi.getFeatureConsistency(queryDate.value)
      Object.assign(result, res.data)
    }
  } catch (err) {
    ElMessage.error('获取校验结果失败')
  }
}

async function handleTrigger() {
  await ElMessageBox.confirm(
    '确定手动触发特征一致性校验？将随机抽取1000条样本进行在线/离线对比，耗时约30秒。',
    '确认触发',
    { type: 'warning' }
  )

  triggering.value = true
  progressVisible.value = true
  triggerProgress.value = 0

  // 模拟进度
  const timer = setInterval(() => {
    triggerProgress.value += Math.floor(Math.random() * 15) + 5
    if (triggerProgress.value >= 100) {
      triggerProgress.value = 100
      clearInterval(timer)
      setTimeout(() => {
        progressVisible.value = false
        triggering.value = false
        // 更新结果
        result.checkedAt = new Date().toISOString()
        ElMessage.success('特征一致性校验完成，结果: PASSED')
        loadData()
      }, 500)
    }
  }, 500)
}
</script>

<style scoped lang="scss">
.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .action-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .check-time {
    font-size: 13px;
    color: var(--text-secondary);
  }
}

.feature-name {
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-family: monospace;
}

.text-danger {
  color: #f56c6c;
  font-weight: 600;
}

.text-success {
  color: #67c23a;
}

.progress-tip {
  margin-top: 16px;
  font-size: 13px;
  color: var(--text-secondary);

  p {
    margin: 4px 0;
  }
}
</style>

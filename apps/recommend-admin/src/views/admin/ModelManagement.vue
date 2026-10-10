<template>
  <div class="model-management">
    <div class="common-card action-bar">
      <el-form :inline="true" :model="queryForm" @submit.prevent>
        <el-form-item label="模型类型">
          <el-select v-model="queryForm.modelType" placeholder="全部类型" clearable style="width: 140px">
            <el-option label="DeepFM" value="deepfm" />
            <el-option label="XGBoost" value="xgboost" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option v-for="(val, key) in MODEL_STATUS_MAP" :key="key" :label="val.label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="ElMessage.success('查询完成')">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="common-card mt-16">
      <el-table :data="filteredModels" stripe v-loading="loading">
        <el-table-column label="模型类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.modelType === 'deepfm' ? 'primary' : 'success'" size="small">
              {{ row.modelType.toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="MODEL_STATUS_MAP[row.status]?.type" size="small">
              {{ MODEL_STATUS_MAP[row.status]?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="AUC" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.auc !== null" :class="row.auc >= 0.8 ? 'text-success' : 'text-warning'">
              {{ row.auc?.toFixed(4) }}
            </span>
            <span v-else class="text-secondary">-</span>
          </template>
        </el-table-column>
        <el-table-column label="LogLoss" width="100" align="right">
          <template #default="{ row }">{{ row.logLoss?.toFixed(4) || '-' }}</template>
        </el-table-column>
        <el-table-column label="特征维度" width="100" align="center">
          <template #default="{ row }">{{ row.featureDim }}</template>
        </el-table-column>
        <el-table-column label="特征一致性" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.featureConsistencyPassed" type="success" size="small">通过</el-tag>
            <el-tag v-else type="danger" size="small">未通过</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="训练时间" width="180">
          <template #default="{ row }">{{ formatTime(row.trainedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="handleViewMetrics(row)">指标详情</el-button>
            <el-button
              size="small"
              text
              type="success"
              :disabled="row.status === 'active' || !row.featureConsistencyPassed || row.status === 'training'"
              @click="handleActivate(row)"
            >
              激活
            </el-button>
            <el-button
              size="small"
              text
              type="warning"
              :disabled="row.status !== 'active'"
              @click="handleRollback(row)"
            >
              回滚
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 模型指标弹窗 -->
    <el-dialog v-model="metricsVisible" title="模型评估指标" width="600px">
      <div v-if="currentModel" class="metrics-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="模型类型">{{ currentModel.modelType.toUpperCase() }}</el-descriptions-item>
          <el-descriptions-item label="版本">{{ currentModel.version }}</el-descriptions-item>
          <el-descriptions-item label="AUC">
            <span :class="currentModel.auc >= 0.8 ? 'text-success' : 'text-warning'">
              {{ currentModel.auc?.toFixed(4) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="LogLoss">{{ currentModel.logLoss?.toFixed(4) }}</el-descriptions-item>
          <el-descriptions-item label="特征维度">{{ currentModel.featureDim }}</el-descriptions-item>
          <el-descriptions-item label="特征一致性">
            <el-tag :type="currentModel.featureConsistencyPassed ? 'success' : 'danger'" size="small">
              {{ currentModel.featureConsistencyPassed ? '通过' : '未通过' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="模型文件" :span="2">
            <code class="file-path">{{ currentModel.filePath }}</code>
          </el-descriptions-item>
          <el-descriptions-item label="训练时间" :span="2">{{ formatTime(currentModel.trainedAt) }}</el-descriptions-item>
        </el-descriptions>

        <el-alert
          class="mt-16"
          title="质量门禁"
          :type="currentModel.auc >= (currentModel.modelType === 'deepfm' ? 0.80 : 0.78) ? 'success' : 'warning'"
          :description="`${currentModel.modelType.toUpperCase()} 质量门禁要求 AUC ≥ ${currentModel.modelType === 'deepfm' ? '0.80' : '0.78'}，特征一致性必须通过。`"
          show-icon
          :closable="false"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { modelApi } from '@/api'
import { mockModels } from '@/mock'
import { formatTime, MODEL_STATUS_MAP } from '@/utils'

const loading = ref(false)
const useMock = import.meta.env.VITE_USE_MOCK === 'true'
const models = ref([...mockModels])

const queryForm = reactive({ modelType: '', status: '' })

const filteredModels = computed(() => {
  return models.value.filter((m) => {
    const typeMatch = !queryForm.modelType || m.modelType === queryForm.modelType
    const statusMatch = !queryForm.status || m.status === queryForm.status
    return typeMatch && statusMatch
  })
})

const metricsVisible = ref(false)
const currentModel = ref(null)

function handleViewMetrics(row) {
  currentModel.value = row
  metricsVisible.value = true
}

async function handleActivate(row) {
  await ElMessageBox.confirm(
    `确定激活 ${row.modelType.toUpperCase()} ${row.version}？激活前将校验特征一致性状态。`,
    '确认激活',
    { type: 'warning' }
  )
  if (!row.featureConsistencyPassed) {
    ElMessage.error('特征一致性校验未通过，无法激活该模型')
    return
  }
  // 取消同类型其他模型的激活状态
  models.value.forEach((m) => {
    if (m.modelType === row.modelType && m.status === 'active') m.status = 'inactive'
  })
  row.status = 'active'
  ElMessage.success(`模型 ${row.modelType.toUpperCase()} ${row.version} 已激活`)
}

async function handleRollback(row) {
  await ElMessageBox.confirm(`确定回滚 ${row.modelType.toUpperCase()} ${row.version}？`, '确认回滚', { type: 'warning' })
  row.status = 'rolled_back'
  ElMessage.success('模型已回滚')
}
</script>

<style scoped>
.action-bar {
  padding: 16px 20px;
}

.text-success {
  color: #67c23a;
  font-weight: 600;
}

.text-warning {
  color: #e6a23c;
  font-weight: 600;
}

.text-secondary {
  color: #909399;
}

.metrics-content {
  .file-path {
    background: #f5f7fa;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 12px;
    word-break: break-all;
  }

  .mt-16 {
    margin-top: 16px;
  }
}
</style>

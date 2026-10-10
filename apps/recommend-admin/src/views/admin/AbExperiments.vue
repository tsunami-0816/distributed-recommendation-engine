<template>
  <div class="ab-experiments">
    <!-- 操作栏 -->
    <div class="common-card action-bar">
      <el-form :inline="true" :model="queryForm" @submit.prevent>
        <el-form-item label="实验名称">
          <el-input v-model="queryForm.name" placeholder="搜索实验名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option v-for="(val, key) in EXPERIMENT_STATUS_MAP" :key="key" :label="val.label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item class="float-right">
          <el-button type="success" :icon="Plus" @click="handleCreate">新建实验</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 实验列表 -->
    <div class="common-card mt-16">
      <el-table :data="filteredExperiments" stripe v-loading="loading">
        <el-table-column prop="experimentId" label="实验ID" width="120" />
        <el-table-column prop="name" label="实验名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="EXPERIMENT_STATUS_MAP[row.status]?.type" size="small">
              {{ EXPERIMENT_STATUS_MAP[row.status]?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="流量" width="100" align="center">
          <template #default="{ row }">{{ row.trafficPercent }}%</template>
        </el-table-column>
        <el-table-column label="配置版本" width="100" align="center">
          <template #default="{ row }">v{{ row.configVersion }}</template>
        </el-table-column>
        <el-table-column label="开始时间" width="170">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="170">
          <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="handleViewReport(row)">实验报告</el-button>
            <el-button size="small" text type="primary" @click="handleEdit(row)" :disabled="row.status === 'ended'">编辑</el-button>
            <el-button v-if="row.status === 'draft'" size="small" text type="success" @click="handleStart(row)">启动</el-button>
            <el-button v-if="row.status === 'running'" size="small" text type="warning" @click="handlePause(row)">暂停</el-button>
            <el-button v-if="row.status === 'paused'" size="small" text type="success" @click="handleStart(row)">恢复</el-button>
            <el-button v-if="row.status === 'running' || row.status === 'paused'" size="small" text type="danger" @click="handleEnd(row)">结束</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑实验' : '新建实验'"
      width="700px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="实验名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入实验名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="流量比例" prop="trafficPercent">
          <el-slider v-model="formData.trafficPercent" :min="0" :max="100" :step="5" show-input />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="formData.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DDTHH:mm:ssZ"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="formData.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DDTHH:mm:ssZ"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="实验组配置">
          <div class="groups-config">
            <div v-for="(group, idx) in formData.groupsConfig" :key="idx" class="group-row">
              <el-input v-model="group.groupName" placeholder="组名" style="width: 140px" />
              <el-input-number v-model="group.percent" :min="0" :max="100" :step="5" placeholder="流量%" style="width: 120px" />
              <el-input v-model="group.description" placeholder="描述" style="flex: 1" />
              <el-button :icon="Delete" type="danger" text @click="removeGroup(idx)" :disabled="formData.groupsConfig.length <= 2" />
            </div>
            <el-button type="primary" plain :icon="Plus" size="small" @click="addGroup">添加实验组</el-button>
            <div class="group-tip">
              当前总流量: <strong :class="{ 'text-danger': totalGroupPercent !== 100 }">{{ totalGroupPercent }}%</strong>
              <span v-if="totalGroupPercent !== 100" class="text-danger">（需等于100%）</span>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 实验报告弹窗 -->
    <el-dialog v-model="reportVisible" title="实验报告" width="800px">
      <div v-if="currentReport" class="report-content">
        <el-descriptions :column="2" border class="mb-16">
          <el-descriptions-item label="实验名称">{{ currentReport.name }}</el-descriptions-item>
          <el-descriptions-item label="实验ID">{{ currentReport.experimentId }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="EXPERIMENT_STATUS_MAP[currentReport.status]?.type" size="small">
              {{ EXPERIMENT_STATUS_MAP[currentReport.status]?.label }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="配置版本">v{{ currentReport.configVersion }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="report-section-title">实验组指标对比</h4>
        <el-table :data="reportMetricsTable" stripe>
          <el-table-column prop="metric" label="指标" width="150" />
          <el-table-column prop="control" label="对照组" width="120" align="right" />
          <el-table-column prop="experiment" label="实验组" width="120" align="right" />
          <el-table-column label="提升率" width="120" align="right">
            <template #default="{ row }">
              <span :class="row.lift >= 0 ? 'text-success' : 'text-danger'">
                {{ row.lift >= 0 ? '+' : '' }}{{ row.lift.toFixed(2) }}%
              </span>
            </template>
          </el-table-column>
          <el-table-column label="P值" width="100" align="right">
            <template #default="{ row }">
              <el-tag :type="row.pValue < 0.05 ? 'success' : 'info'" size="small">{{ row.pValue }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="显著性" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.pValue < 0.05" type="success" size="small">显著</el-tag>
              <el-tag v-else type="info" size="small">不显著</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <el-alert
          v-if="currentReport.metrics"
          class="mt-16"
          title="结论建议"
          :type="reportConclusion.type"
          :description="reportConclusion.text"
          show-icon
          :closable="false"
        />
      </div>
      <el-empty v-else description="该实验暂无报告数据" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { Search, Refresh, Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { experimentApi } from '@/api'
import { mockExperiments } from '@/mock'
import { formatTime, EXPERIMENT_STATUS_MAP } from '@/utils'

const loading = ref(false)
const submitting = ref(false)
const useMock = import.meta.env.VITE_USE_MOCK === 'true'

const experiments = ref([...mockExperiments])

const queryForm = reactive({ name: '', status: '' })

const filteredExperiments = computed(() => {
  return experiments.value.filter((exp) => {
    const nameMatch = !queryForm.name || exp.name.includes(queryForm.name)
    const statusMatch = !queryForm.status || exp.status === queryForm.status
    return nameMatch && statusMatch
  })
})

// 弹窗
const dialogVisible = ref(false)
const reportVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const currentReport = ref(null)

const formData = reactive({
  experimentId: '',
  name: '',
  trafficPercent: 50,
  startTime: '',
  endTime: '',
  groupsConfig: [
    { groupName: 'control', percent: 50, description: '对照组' },
    { groupName: 'experiment', percent: 50, description: '实验组' }
  ]
})

const formRules = {
  name: [{ required: true, message: '请输入实验名称', trigger: 'blur' }],
  trafficPercent: [{ required: true, message: '请设置流量比例', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const totalGroupPercent = computed(() => formData.groupsConfig.reduce((sum, g) => sum + (g.percent || 0), 0))

// 报告指标表格
const reportMetricsTable = computed(() => {
  if (!currentReport.value?.metrics) return []
  return Object.entries(currentReport.value.metrics).map(([metric, vals]) => ({
    metric: metric.toUpperCase(),
    control: vals.control,
    experiment: vals.experiment,
    lift: vals.lift,
    pValue: vals.pValue
  }))
})

// 报告结论
const reportConclusion = computed(() => {
  if (!currentReport.value?.metrics) return { type: 'info', text: '暂无数据' }
  const ctr = currentReport.value.metrics.ctr
  if (ctr && ctr.pValue < 0.05 && ctr.lift > 0) {
    return { type: 'success', text: `实验组CTR显著提升${ctr.lift.toFixed(1)}%（P=${ctr.pValue}），建议全量发布实验组策略。` }
  }
  if (ctr && ctr.pValue < 0.05 && ctr.lift < 0) {
    return { type: 'danger', text: `实验组CTR显著下降${Math.abs(ctr.lift).toFixed(1)}%（P=${ctr.pValue}），建议终止实验。` }
  }
  return { type: 'warning', text: '当前指标差异不具有统计显著性（P≥0.05），建议继续观察或扩大样本量。' }
})

function handleSearch() {
  ElMessage.success('查询完成')
}

function handleReset() {
  queryForm.name = ''
  queryForm.status = ''
}

function handleCreate() {
  isEdit.value = false
  formData.experimentId = ''
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(formData, {
    experimentId: row.experimentId,
    name: row.name,
    trafficPercent: row.trafficPercent,
    startTime: row.startTime,
    endTime: row.endTime,
    groupsConfig: JSON.parse(JSON.stringify(row.groupsConfig))
  })
  dialogVisible.value = true
}

function addGroup() {
  formData.groupsConfig.push({ groupName: '', percent: 0, description: '' })
}

function removeGroup(idx) {
  formData.groupsConfig.splice(idx, 1)
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请完善表单信息')
    return
  }
  if (totalGroupPercent.value !== 100) {
    ElMessage.error('实验组流量总和必须等于100%')
    return
  }

  submitting.value = true
  try {
    if (useMock) {
      await new Promise((r) => setTimeout(r, 500))
      if (isEdit.value) {
        const idx = experiments.value.findIndex((e) => e.experimentId === formData.experimentId)
        if (idx > -1) {
          experiments.value[idx] = { ...experiments.value[idx], ...formData, configVersion: experiments.value[idx].configVersion + 1 }
        }
        ElMessage.success('实验已更新，configVersion已自增')
      } else {
        experiments.value.unshift({
          ...formData,
          experimentId: 'exp_' + String(experiments.value.length + 1).padStart(3, '0'),
          status: 'draft',
          configVersion: 1,
          metrics: null
        })
        ElMessage.success('实验创建成功')
      }
    } else {
      if (isEdit.value) {
        await experimentApi.updateExperiment(formData.experimentId, formData)
      } else {
        await experimentApi.createExperiment(formData)
      }
      ElMessage.success('操作成功')
    }
    dialogVisible.value = false
  } catch (err) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

function resetForm() {
  formData.name = ''
  formData.trafficPercent = 50
  formData.startTime = ''
  formData.endTime = ''
  formData.groupsConfig = [
    { groupName: 'control', percent: 50, description: '对照组' },
    { groupName: 'experiment', percent: 50, description: '实验组' }
  ]
}

async function handleStart(row) {
  await ElMessageBox.confirm(`确定启动实验「${row.name}」？`, '确认启动', { type: 'warning' })
  row.status = 'running'
  if (!row.startTime) row.startTime = new Date().toISOString()
  ElMessage.success('实验已启动')
}

async function handlePause(row) {
  await ElMessageBox.confirm(`确定暂停实验「${row.name}」？暂停后分流将返回对照组。`, '确认暂停', { type: 'warning' })
  row.status = 'paused'
  ElMessage.success('实验已暂停')
}

async function handleEnd(row) {
  await ElMessageBox.confirm(`确定结束实验「${row.name}」？结束后数据保留供分析。`, '确认结束', { type: 'warning' })
  row.status = 'ended'
  row.endTime = new Date().toISOString()
  ElMessage.success('实验已结束')
}

function handleViewReport(row) {
  currentReport.value = row
  reportVisible.value = true
}
</script>

<style scoped lang="scss">
.action-bar {
  padding: 16px 20px;

  .float-right {
    margin-left: auto;
  }
}

.groups-config {
  width: 100%;

  .group-row {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
  }

  .group-tip {
    margin-top: 8px;
    font-size: 13px;
    color: var(--text-secondary);
  }
}

.text-danger {
  color: #f56c6c;
}

.text-success {
  color: #67c23a;
}

.report-content {
  .report-section-title {
    font-size: 15px;
    font-weight: 600;
    margin: 16px 0 12px;
  }

  .mb-16 {
    margin-bottom: 16px;
  }

  .mt-16 {
    margin-top: 16px;
  }
}
</style>

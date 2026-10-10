<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <StatCard
          label="今日推荐请求"
          :value="stats.totalRequests"
          icon="DataAnalysis"
          icon-color="#409eff"
          icon-bg="rgba(64,158,255,0.1)"
          :trend="5.2"
        />
      </el-col>
      <el-col :span="6">
        <StatCard
          label="平均响应延迟"
          :value="stats.avgLatencyMs"
          unit="ms"
          icon="Timer"
          icon-color="#67c23a"
          icon-bg="rgba(103,194,58,0.1)"
          :trend="-8.3"
        />
      </el-col>
      <el-col :span="6">
        <StatCard
          label="P99 延迟"
          :value="stats.p99LatencyMs"
          unit="ms"
          icon="Odometer"
          icon-color="#e6a23c"
          icon-bg="rgba(230,162,60,0.1)"
          :trend="-3.1"
        />
      </el-col>
      <el-col :span="6">
        <StatCard
          label="缓存命中率"
          :value="stats.cacheHitRate"
          format="percent"
          icon="Coin"
          icon-color="#f56c6c"
          icon-bg="rgba(245,108,108,0.1)"
          :trend="2.8"
        />
      </el-col>
    </el-row>

    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <StatCard
          label="当前活跃用户"
          :value="stats.activeUsers"
          icon="User"
          icon-color="#909399"
          icon-bg="rgba(144,147,153,0.1)"
          :trend="4.1"
        />
      </el-col>
      <el-col :span="6">
        <StatCard
          label="推荐 QPS"
          :value="stats.recommendationQps"
          icon="Lightning"
          icon-color="#409eff"
          icon-bg="rgba(64,158,255,0.1)"
        />
      </el-col>
      <el-col :span="6">
        <StatCard
          label="模型降级率"
          :value="stats.modelDegradationRate"
          format="percent"
          icon="Warning"
          icon-color="#e6a23c"
          icon-bg="rgba(230,162,60,0.1)"
          :trend="-1.2"
        />
      </el-col>
      <el-col :span="6">
        <StatCard
          label="进行中实验"
          :value="stats.activeExperiments"
          icon="Histogram"
          icon-color="#67c23a"
          icon-bg="rgba(103,194,58,0.1)"
        />
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="chart-row">
      <!-- 请求量趋势 -->
      <el-col :span="16">
        <div class="common-card chart-card">
          <div class="card-title">
            <el-icon color="#409eff"><TrendCharts /></el-icon>
            近7天请求量与延迟趋势
          </div>
          <v-chart class="chart" :option="requestTrendOption" autoresize />
        </div>
      </el-col>

      <!-- 召回通道分布 -->
      <el-col :span="8">
        <div class="common-card chart-card">
          <div class="card-title">
            <el-icon color="#67c23a"><PieChart /></el-icon>
            召回通道分布
          </div>
          <v-chart class="chart" :option="recallPieOption" autoresize />
        </div>
      </el-col>
    </el-row>

    <!-- 系统状态 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="24">
        <div class="common-card">
          <div class="card-title">
            <el-icon color="#e6a23c"><Monitor /></el-icon>
            系统组件健康状态
          </div>
          <el-table :data="healthTable" stripe>
            <el-table-column prop="name" label="组件" width="150" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'UP' ? 'success' : 'danger'" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="latency" label="响应延迟" width="120" align="right" />
            <el-table-column prop="connections" label="连接数" width="120" align="right" />
            <el-table-column prop="description" label="说明" />
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import StatCard from '@/components/StatCard.vue'
import { mockDashboardStats, mockRequestTrend, mockRecallDistribution } from '@/mock'
import { getDashboardStats, getSystemHealth } from '@/api/admin/dashboard'

use([CanvasRenderer, LineChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const stats = ref({ ...mockDashboardStats })

const healthTable = ref([
  { name: 'MySQL', status: 'UP', latency: '2ms', connections: '12/50', description: '物品元数据/用户信息/实验配置' },
  { name: 'Redis', status: 'UP', latency: '1ms', connections: '8/100', description: '推荐结果缓存/热点数据/画像缓存' },
  { name: 'MongoDB', status: 'UP', latency: '3ms', connections: '5/20', description: '用户画像/行为明细' },
  { name: 'Kafka', status: 'UP', latency: '5ms', connections: '3/10', description: '行为事件/实时特征管道' },
  { name: 'Elasticsearch', status: 'UP', latency: '8ms', connections: '4/10', description: '文本检索/语义召回' },
  { name: '模型推理(gRPC)', status: 'UP', latency: '15ms', connections: '2/8', description: 'DeepFM/XGBoost ONNX推理' },
  { name: 'Flink', status: 'UP', latency: '-', connections: '-', description: '实时特征计算，Checkpoint正常' },
  { name: 'Spark', status: 'UP', latency: '-', connections: '-', description: '离线计算（本地模式，00:30启动）' }
])

// 请求量趋势图配置
const requestTrendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['请求量', '平均延迟(ms)'], bottom: 0 },
  grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true },
  xAxis: {
    type: 'category',
    data: mockRequestTrend.map((d) => d.date),
    boundaryGap: false
  },
  yAxis: [
    { type: 'value', name: '请求量', position: 'left' },
    { type: 'value', name: '延迟(ms)', position: 'right' }
  ],
  series: [
    {
      name: '请求量',
      type: 'line',
      smooth: true,
      data: mockRequestTrend.map((d) => d.requests),
      areaStyle: { opacity: 0.1 },
      itemStyle: { color: '#409eff' }
    },
    {
      name: '平均延迟(ms)',
      type: 'line',
      smooth: true,
      yAxisIndex: 1,
      data: mockRequestTrend.map((d) => d.latency),
      itemStyle: { color: '#67c23a' }
    }
  ]
}))

// 召回通道饼图配置
const recallPieOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c}% ({d}%)' },
  legend: { orient: 'vertical', right: '5%', top: 'center' },
  series: [
    {
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: mockRecallDistribution.map((d) => ({ name: d.name, value: d.value }))
    }
  ]
}))

onMounted(async () => {
  // P-05 修复：从后端管理接口获取实时统计数据，失败时降级到 mock
  try {
    const res = await getDashboardStats()
    if (res && res.code === 0 && res.data) {
      stats.value = { ...mockDashboardStats, ...res.data }
    }
  } catch (err) {
    console.warn('[Dashboard] 实时统计数据获取失败，使用 Mock 数据:', err.message)
  }

  // 从 Actuator /health 获取系统组件健康状态
  try {
    const health = await getSystemHealth()
    if (health && health.components) {
      const componentList = []
      Object.entries(health.components).forEach(([name, detail]) => {
        componentList.push({
          name,
          status: detail.status || 'UNKNOWN',
          latency: '-',
          connections: '-',
          description: detail.details ? JSON.stringify(detail.details) : ''
        })
      })
      if (componentList.length > 0) {
        healthTable.value = componentList
      }
    }
  } catch (err) {
    console.warn('[Dashboard] 健康状态获取失败，使用默认组件列表:', err.message)
  }
})
</script>

<style scoped lang="scss">
.dashboard {
  width: 100%;
}

.stat-row {
  margin-bottom: 16px;
}

.chart-row {
  margin-bottom: 16px;
}

.chart-card {
  padding: 20px;
}

.chart {
  height: 300px;
  width: 100%;
}
</style>

<template>
  <div class="cache-manage">
    <!-- 缓存概览 -->
    <el-row :gutter="16">
      <el-col :span="8">
        <StatCard label="缓存命中率" :value="0.68" format="percent" icon="Coin" icon-color="#409eff" icon-bg="rgba(64,158,255,0.1)" />
      </el-col>
      <el-col :span="8">
        <StatCard label="缓存Key总数" :value="125840" icon="Files" icon-color="#67c23a" icon-bg="rgba(103,194,58,0.1)" />
      </el-col>
      <el-col :span="8">
        <StatCard label="内存使用" value="856MB / 1GB" icon="Memory" icon-color="#e6a23c" icon-bg="rgba(230,162,60,0.1)" />
      </el-col>
    </el-row>

    <!-- 缓存刷新操作 -->
    <div class="common-card mt-16">
      <div class="card-title">
        <el-icon color="#f56c6c"><RefreshLeft /></el-icon>
        缓存刷新（支持按Key前缀批量失效 / 指定具体Key删除）
      </div>

      <el-form :model="refreshForm" :rules="refreshRules" ref="formRef" label-width="120px">
        <el-form-item label="刷新方式">
          <el-radio-group v-model="refreshForm.mode">
            <el-radio value="prefix">按Key前缀批量失效</el-radio>
            <el-radio value="key">指定具体Key删除</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="refreshForm.mode === 'prefix'" label="Key前缀" prop="prefix">
          <el-select v-model="refreshForm.prefix" placeholder="选择缓存Key前缀" style="width: 400px" filterable allow-create>
            <el-option
              v-for="item in cachePrefixOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
              <span>{{ item.label }}</span>
              <span style="float: right; color: #8492a6; font-size: 12px">{{ item.desc }}</span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item v-else label="具体Key" prop="key">
          <el-input v-model="refreshForm.key" placeholder="例如: rec:result:home:u_10001:control:deepfm_v1" style="width: 500px" />
        </el-form-item>

        <el-form-item>
          <el-button type="danger" :icon="Delete" :loading="refreshing" @click="handleRefresh">
            执行缓存刷新
          </el-button>
          <el-button :icon="Warning" @click="showWarning = true">注意事项</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 刷新历史 -->
    <div class="common-card mt-16">
      <div class="card-title">
        <el-icon color="#909399"><Clock /></el-icon>
        最近刷新记录
      </div>
      <el-table :data="refreshHistory" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="type" label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 'prefix' ? 'warning' : 'danger'" size="small">
              {{ row.type === 'prefix' ? '前缀批量' : '指定Key' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="target" label="目标" min-width="300" show-overflow-tooltip>
          <template #default="{ row }">
            <code class="target-key">{{ row.target }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="affectedKeys" label="影响Key数" width="120" align="right" />
        <el-table-column prop="operator" label="操作人" width="100" align="center" />
        <el-table-column prop="time" label="操作时间" width="170" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 注意事项弹窗 -->
    <el-dialog v-model="showWarning" title="缓存刷新注意事项" width="500px">
      <el-alert type="warning" :closable="false" show-icon title="刷新影响">
        <ul style="margin: 8px 0; padding-left: 20px; line-height: 1.8;">
          <li>批量前缀刷新会导致大量缓存同时失效，可能引发缓存雪崩</li>
          <li>推荐结果缓存失效后，首次请求将走完整召回+排序链路（P99 &lt; 200ms）</li>
          <li>用户画像长期缓存失效后，下次读取将回源 MongoDB</li>
          <li>建议在低峰期执行大批量缓存刷新</li>
          <li>短期画像缓存（TTL 5s）无需手动刷新，靠TTL自然过期</li>
        </ul>
      </el-alert>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { RefreshLeft, Delete, Warning, Clock } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cacheApi } from '@/api'
import StatCard from '@/components/StatCard.vue'

const refreshing = ref(false)
const showWarning = ref(false)
const formRef = ref(null)
const useMock = import.meta.env.VITE_USE_MOCK === 'true'

const refreshForm = reactive({
  mode: 'prefix',
  prefix: '',
  key: ''
})

const refreshRules = {
  prefix: [{ required: true, message: '请选择或输入Key前缀', trigger: 'change' }],
  key: [{ required: true, message: '请输入具体Key', trigger: 'blur' }]
}

const cachePrefixOptions = [
  { value: 'rec:result', label: 'rec:result', desc: '推荐结果缓存（分层TTL）' },
  { value: 'user:profile:long', label: 'user:profile:long', desc: '用户长期画像缓存（TTL 300s）' },
  { value: 'user:profile:short', label: 'user:profile:short', desc: '用户短期画像缓存（TTL 5s，不建议手动刷新）' },
  { value: 'ab:user', label: 'ab:user', desc: 'A/B分流缓存（TTL=实验结束时间）' },
  { value: 'ab:experiment', label: 'ab:experiment', desc: '实验配置缓存（TTL 300s）' },
  { value: 'item:info', label: 'item:info', desc: '物品信息缓存' },
  { value: 'item:sim', label: 'item:sim', desc: '物品相似度缓存（ItemCF用）' },
  { value: 'hot:items', label: 'hot:items', desc: '热门物品缓存' },
  { value: 'user:action', label: 'user:action', desc: '用户实时行为计数（Flink写入）' }
]

const refreshHistory = ref([
  { id: 1, type: 'prefix', target: 'rec:result', affectedKeys: 12580, operator: 'admin', time: '2026-10-03 10:30:00', status: 'success' },
  { id: 2, type: 'key', target: 'rec:result:home:u_10001:control:deepfm_v1', affectedKeys: 1, operator: 'admin', time: '2026-10-03 09:15:00', status: 'success' },
  { id: 3, type: 'prefix', target: 'user:profile:long', affectedKeys: 8432, operator: 'admin', time: '2026-10-02 22:00:00', status: 'success' },
  { id: 4, type: 'prefix', target: 'ab:user', affectedKeys: 5200, operator: 'admin', time: '2026-10-02 18:30:00', status: 'success' }
])

async function handleRefresh() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  const target = refreshForm.mode === 'prefix' ? refreshForm.prefix : refreshForm.key
  await ElMessageBox.confirm(
    `确定刷新缓存「${target}」？此操作不可撤销。`,
    '确认刷新',
    { type: 'warning' }
  )

  refreshing.value = true
  try {
    if (useMock) {
      await new Promise((r) => setTimeout(r, 800))
    } else {
      await cacheApi.refreshCache(refreshForm.mode === 'prefix' ? { prefix: target } : { key: target })
    }
    refreshHistory.value.unshift({
      id: refreshHistory.value.length + 1,
      type: refreshForm.mode,
      target,
      affectedKeys: refreshForm.mode === 'prefix' ? Math.floor(Math.random() * 5000) + 100 : 1,
      operator: 'admin',
      time: new Date().toLocaleString('zh-CN'),
      status: 'success'
    })
    ElMessage.success('缓存刷新成功')
  } catch (err) {
    if (err !== 'cancel') ElMessage.error('缓存刷新失败')
  } finally {
    refreshing.value = false
  }
}
</script>

<style scoped>
.target-key {
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-family: monospace;
}
</style>

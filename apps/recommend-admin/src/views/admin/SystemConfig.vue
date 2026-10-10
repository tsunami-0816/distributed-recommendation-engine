<template>
  <div class="system-config">
    <div class="common-card">
      <div class="card-title">
        <el-icon color="#409eff"><Setting /></el-icon>
        系统动态配置（支持热更新，更新后写入 Redis config:dynamic）
      </div>

      <el-table :data="configs" stripe v-loading="loading">
        <el-table-column prop="configKey" label="配置Key" width="320">
          <template #default="{ row }">
            <code class="config-key">{{ row.configKey }}</code>
          </template>
        </el-table-column>
        <el-table-column label="配置值" min-width="200">
          <template #default="{ row }">
            <el-input
              v-if="editingKey === row.configKey"
              v-model="row.configValue"
              size="small"
              @keyup.enter="handleSave(row)"
            />
            <span v-else class="config-value">{{ row.configValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
        <el-table-column label="更新时间" width="170">
          <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column prop="updatedBy" label="操作人" width="100" align="center" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="editingKey !== row.configKey" size="small" text type="primary" @click="handleEdit(row)">
              编辑
            </el-button>
            <template v-else>
              <el-button size="small" text type="success" @click="handleSave(row)">保存</el-button>
              <el-button size="small" text @click="handleCancel(row)">取消</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 配置说明 -->
    <div class="common-card mt-16">
      <div class="card-title">
        <el-icon color="#e6a23c"><InfoFilled /></el-icon>
        配置说明
      </div>
      <el-alert
        title="热更新机制"
        type="info"
        :closable="false"
        show-icon
        description="所有配置项更新后立即写入 Redis Hash（config:dynamic），应用通过 @RefreshScope 或动态监听实现运行时热更新，无需重启服务。关键配置如 model.rank.active 切换后，新请求将立即使用新模型。"
      />
      <el-divider />
      <div class="config-groups">
        <div class="config-group">
          <h4>推荐相关</h4>
          <ul>
            <li><code>model.rank.active</code> - 排序服务当前活跃模型（deepfm_v1 / deepfm_v2 等）</li>
            <li><code>recommend.rank.default-page-size</code> - 默认返回数量</li>
            <li><code>recommend.rank.max-page-size</code> - 最大返回数量</li>
          </ul>
        </div>
        <div class="config-group">
          <h4>缓存相关</h4>
          <ul>
            <li><code>recommend.cache.high-frequency-ttl</code> - 高频用户缓存TTL（秒）</li>
            <li><code>recommend.cache.normal-ttl</code> - 普通用户缓存TTL（秒）</li>
            <li><code>recommend.cache.fallback-ttl</code> - 兜底用户缓存TTL（秒）</li>
          </ul>
        </div>
        <div class="config-group">
          <h4>模型推理</h4>
          <ul>
            <li><code>recommend.model-inference.deepfm-timeout-ms</code> - DeepFM推理超时（毫秒）</li>
            <li><code>recommend.model-inference.xgboost-timeout-ms</code> - XGBoost推理超时（毫秒）</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Setting, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { configApi } from '@/api'
import { mockConfigs } from '@/mock'
import { formatTime } from '@/utils'

const loading = ref(false)
const useMock = import.meta.env.VITE_USE_MOCK === 'true'
const configs = ref(JSON.parse(JSON.stringify(mockConfigs)))
const editingKey = ref('')
const originalValues = ref({})

function handleEdit(row) {
  editingKey.value = row.configKey
  originalValues.value[row.configKey] = row.configValue
}

function handleCancel(row) {
  row.configValue = originalValues.value[row.configKey]
  editingKey.value = ''
}

async function handleSave(row) {
  if (!row.configValue.trim()) {
    ElMessage.warning('配置值不能为空')
    return
  }
  try {
    if (useMock) {
      await new Promise((r) => setTimeout(r, 300))
    } else {
      await configApi.updateConfig(row.configKey, { configValue: row.configValue })
    }
    row.updatedAt = new Date().toISOString()
    row.updatedBy = 'admin'
    editingKey.value = ''
    ElMessage.success(`配置 ${row.configKey} 已更新，热更新生效`)
  } catch (err) {
    ElMessage.error('配置更新失败')
  }
}
</script>

<style scoped lang="scss">
.config-key {
  background: #f0f2f5;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-family: monospace;
}

.config-value {
  font-weight: 500;
  color: var(--text-primary);
}

.config-groups {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;

  .config-group {
    h4 {
      font-size: 14px;
      margin-bottom: 8px;
      color: var(--text-primary);
    }

    ul {
      padding-left: 0;
      list-style: none;

      li {
        font-size: 12px;
        color: var(--text-secondary);
        margin-bottom: 6px;
        line-height: 1.6;

        code {
          background: #f0f2f5;
          padding: 1px 4px;
          border-radius: 3px;
          font-size: 11px;
          color: var(--primary-color);
        }
      }
    }
  }
}
</style>

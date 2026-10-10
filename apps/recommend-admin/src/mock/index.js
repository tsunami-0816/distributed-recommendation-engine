/**
 * Mock 数据中心
 * 当后端服务不可用时，前端可通过 VITE_USE_MOCK=true 切换到 Mock 模式
 * 数据结构严格对齐后端 DTO 定义
 */

// ===== 推荐接口 Mock =====
export const mockRecommendResponse = {
  code: 0,
  message: 'success',
  traceId: '66b8293aa5b44cb0b95675d852b1e5c',
  timestamp: Date.now(),
  data: {
    scene: 'home',
    abGroup: 'control',
    modelVersion: 'deepfm_v1',
    items: [
      {
        itemId: 'i_16094',
        rank: 1,
        score: 0.9523,
        recallChannel: ['itemcf', 'profile'],
        item: {
          itemId: 'i_16094',
          name: '无线蓝牙耳机 Pro',
          description: '主动降噪，30小时续航，蓝牙5.3',
          categoryId: 101,
          categoryPath: '电子产品/音频/耳机',
          price: 599.0,
          tags: ['新品', '热销'],
          status: 1,
          hotScore: 0.87
        }
      },
      {
        itemId: 'i_15716',
        rank: 2,
        score: 0.9187,
        recallChannel: ['tag'],
        item: {
          itemId: 'i_15716',
          name: '智能手表 Series 8',
          description: '健康监测，GPS定位，血氧检测',
          categoryId: 103,
          categoryPath: '电子产品/穿戴/手表',
          price: 1299.0,
          tags: ['好评', '限量'],
          status: 1,
          hotScore: 0.76
        }
      },
      {
        itemId: 'i_18230',
        rank: 3,
        score: 0.8856,
        recallChannel: ['hot'],
        item: {
          itemId: 'i_18230',
          name: '机械键盘 87键',
          description: '红轴，RGB背光，PBT键帽',
          categoryId: 102,
          categoryPath: '电子产品/外设/键盘',
          price: 349.0,
          tags: ['热销', '推荐'],
          status: 1,
          hotScore: 0.92
        }
      },
      {
        itemId: 'i_14521',
        rank: 4,
        score: 0.8534,
        recallChannel: ['itemcf', 'tag'],
        item: {
          itemId: 'i_14521',
          name: '4K 显示器 27寸',
          description: 'IPS面板，HDR400，Type-C 90W',
          categoryId: 104,
          categoryPath: '电子产品/显示/显示器',
          price: 1899.0,
          tags: ['新品', '品质'],
          status: 1,
          hotScore: 0.68
        }
      },
      {
        itemId: 'i_13987',
        rank: 5,
        score: 0.8211,
        recallChannel: ['profile'],
        item: {
          itemId: 'i_13987',
          name: '人体工学椅',
          description: '网布透气，腰托调节，静音轮',
          categoryId: 201,
          categoryPath: '家居/办公/椅子',
          price: 899.0,
          tags: ['热销', '舒适'],
          status: 1,
          hotScore: 0.71
        }
      }
    ],
    candidateCount: 250,
    duration: {
      cacheMs: 0,
      recallMs: 12,
      featureMs: 0,
      rankMs: 8,
      rerankMs: 0,
      totalMs: 23
    }
  }
}

// ===== 用户画像 Mock =====
export const mockUserProfile = {
  code: 0,
  message: 'success',
  traceId: 'd4e5f6a7b8c9',
  timestamp: Date.now(),
  data: {
    userId: 'u_10001',
    longTerm: {
      categoryPreferences: { '101': 0.85, '102': 0.62, '201': 0.45, '103': 0.38 },
      tagWeights: { 新品: 0.78, 热销: 0.65, 品质: 0.55, 好评: 0.42 },
      activityLevel: 0.72,
      activeHours: { '10': 0.1, '14': 0.15, '20': 0.25, '22': 0.2 },
      pricePreference: '中',
      registerDays: 180
    },
    shortTerm: {
      recentClickedItems: ['i_16094', 'i_15716', 'i_18230'],
      recentFavoritedItems: ['i_16094'],
      recentCategories: { '101': 3, '103': 1 },
      recentActionCount: 8
    },
    fusedTags: { 新品: 0.71, 热销: 0.63, 近期热门: 0.6 },
    version: 1727936400,
    updatedAt: Date.now()
  }
}

// ===== 热门物品 Mock =====
export const mockHotItems = {
  code: 0,
  message: 'success',
  traceId: 'e7f8a9b0c1d2',
  timestamp: Date.now(),
  data: [
    {
      itemId: 'i_18230',
      rank: 1,
      hotScore: 0.98,
      clickCount: 9800,
      favoriteCount: 2940,
      item: { itemId: 'i_18230', name: '机械键盘 87键', categoryId: 101, price: 349.0, status: 1, hotScore: 0.98 }
    },
    {
      itemId: 'i_16094',
      rank: 2,
      hotScore: 0.87,
      clickCount: 8700,
      favoriteCount: 2610,
      item: { itemId: 'i_16094', name: '无线蓝牙耳机 Pro', categoryId: 101, price: 599.0, status: 1, hotScore: 0.87 }
    },
    {
      itemId: 'i_15716',
      rank: 3,
      hotScore: 0.76,
      clickCount: 7600,
      favoriteCount: 2280,
      item: { itemId: 'i_15716', name: '智能手表 Series 8', categoryId: 101, price: 1299.0, status: 1, hotScore: 0.76 }
    },
    {
      itemId: 'i_14521',
      rank: 4,
      hotScore: 0.68,
      clickCount: 6800,
      favoriteCount: 2040,
      item: { itemId: 'i_14521', name: '4K 显示器 27寸', categoryId: 104, price: 1899.0, status: 1, hotScore: 0.68 }
    },
    {
      itemId: 'i_13987',
      rank: 5,
      hotScore: 0.62,
      clickCount: 6200,
      favoriteCount: 1860,
      item: { itemId: 'i_13987', name: '人体工学椅', categoryId: 201, price: 899.0, status: 1, hotScore: 0.62 }
    }
  ]
}

// ===== A/B 实验 Mock =====
export const mockExperiments = [
  {
    experimentId: 'exp_001',
    name: '首页推荐模型对比实验',
    status: 'running',
    trafficPercent: 50,
    configVersion: 3,
    startTime: '2026-09-28T00:00:00Z',
    endTime: '2026-10-15T00:00:00Z',
    groupsConfig: [
      { groupName: 'control', percent: 50, modelVersion: 'deepfm_v1', description: '对照组-当前模型' },
      { groupName: 'experiment', percent: 50, modelVersion: 'deepfm_v2', description: '实验组-新模型' }
    ],
    metrics: { ctr: { control: 0.125, experiment: 0.142, lift: 13.6, pValue: 0.012 }, cvr: { control: 0.032, experiment: 0.038, lift: 18.75, pValue: 0.028 } }
  },
  {
    experimentId: 'exp_002',
    name: '搜索场景召回通道优化',
    status: 'running',
    trafficPercent: 30,
    configVersion: 1,
    startTime: '2026-10-01T00:00:00Z',
    endTime: '2026-10-20T00:00:00Z',
    groupsConfig: [
      { groupName: 'control', percent: 70, description: '对照组-原召回策略' },
      { groupName: 'experiment', percent: 30, description: '实验组-新增语义召回' }
    ],
    metrics: { ctr: { control: 0.098, experiment: 0.115, lift: 17.3, pValue: 0.045 } }
  },
  {
    experimentId: 'exp_003',
    name: '重排策略长尾加权测试',
    status: 'paused',
    trafficPercent: 20,
    configVersion: 2,
    startTime: '2026-09-25T00:00:00Z',
    endTime: '2026-10-10T00:00:00Z',
    groupsConfig: [
      { groupName: 'control', percent: 80, description: '对照组' },
      { groupName: 'experiment', percent: 20, description: '实验组-长尾加权×1.1' }
    ],
    metrics: { ctr: { control: 0.11, experiment: 0.108, lift: -1.8, pValue: 0.32 } }
  },
  {
    experimentId: 'exp_004',
    name: '缓存TTL策略调优',
    status: 'draft',
    trafficPercent: 0,
    configVersion: 1,
    startTime: null,
    endTime: null,
    groupsConfig: [
      { groupName: 'control', percent: 50, description: '当前TTL策略' },
      { groupName: 'experiment', percent: 50, description: '优化后TTL策略' }
    ],
    metrics: null
  },
  {
    experimentId: 'exp_005',
    name: '行为采集延迟优化验证',
    status: 'ended',
    trafficPercent: 100,
    configVersion: 1,
    startTime: '2026-09-15T00:00:00Z',
    endTime: '2026-09-25T00:00:00Z',
    groupsConfig: [
      { groupName: 'control', percent: 50, description: '旧采集链路' },
      { groupName: 'experiment', percent: 50, description: '新采集链路' }
    ],
    metrics: { latencyP99: { control: 4500, experiment: 2800, lift: -37.8, pValue: 0.003 } }
  }
]

// ===== 模型管理 Mock =====
export const mockModels = [
  { modelType: 'deepfm', version: 'v2', filePath: '/models/active/deepfm_v2_20261002.onnx', auc: 0.812, logLoss: 0.423, featureDim: 32, featureConsistencyPassed: true, status: 'active', trainedAt: '2026-10-02T08:00:00Z' },
  { modelType: 'deepfm', version: 'v1', filePath: '/models/active/deepfm_v1_20260920.onnx', auc: 0.805, logLoss: 0.431, featureDim: 32, featureConsistencyPassed: true, status: 'inactive', trainedAt: '2026-09-20T10:00:00Z' },
  { modelType: 'xgboost', version: 'v3', filePath: '/models/active/xgboost_v3_20261001.onnx', auc: 0.791, logLoss: 0.445, featureDim: 32, featureConsistencyPassed: true, status: 'active', trainedAt: '2026-10-01T06:00:00Z' },
  { modelType: 'xgboost', version: 'v2', filePath: '/models/active/xgboost_v2_20260915.onnx', auc: 0.785, logLoss: 0.452, featureDim: 30, featureConsistencyPassed: true, status: 'inactive', trainedAt: '2026-09-15T08:00:00Z' },
  { modelType: 'deepfm', version: 'v3', filePath: '/models/active/deepfm_v3_20261003.onnx', auc: null, logLoss: null, featureDim: 36, featureConsistencyPassed: false, status: 'training', trainedAt: '2026-10-03T02:00:00Z' }
]

// ===== 系统配置 Mock =====
export const mockConfigs = [
  { configKey: 'model.rank.active', configValue: 'deepfm_v2', description: '排序服务当前活跃模型', updatedAt: '2026-10-02T09:30:00Z', updatedBy: 'admin' },
  { configKey: 'recommend.rank.default-page-size', configValue: '20', description: '默认返回数量', updatedAt: '2026-09-20T10:00:00Z', updatedBy: 'admin' },
  { configKey: 'recommend.rank.max-page-size', configValue: '50', description: '最大返回数量', updatedAt: '2026-09-20T10:00:00Z', updatedBy: 'admin' },
  { configKey: 'recommend.cache.high-frequency-ttl', configValue: '10', description: '高频用户缓存TTL（秒）', updatedAt: '2026-09-25T14:00:00Z', updatedBy: 'admin' },
  { configKey: 'recommend.cache.normal-ttl', configValue: '60', description: '普通用户缓存TTL（秒）', updatedAt: '2026-09-20T10:00:00Z', updatedBy: 'admin' },
  { configKey: 'recommend.cache.fallback-ttl', configValue: '120', description: '兜底用户缓存TTL（秒）', updatedAt: '2026-09-20T10:00:00Z', updatedBy: 'admin' },
  { configKey: 'recommend.model-inference.deepfm-timeout-ms', configValue: '80', description: 'DeepFM推理超时（毫秒）', updatedAt: '2026-09-20T10:00:00Z', updatedBy: 'admin' },
  { configKey: 'recommend.model-inference.xgboost-timeout-ms', configValue: '20', description: 'XGBoost推理超时（毫秒）', updatedAt: '2026-09-20T10:00:00Z', updatedBy: 'admin' },
  { configKey: 'ab.experiment.default-exp-id', configValue: 'exp_default', description: '默认A/B实验ID', updatedAt: '2026-09-20T10:00:00Z', updatedBy: 'admin' }
]

// ===== 特征一致性 Mock =====
export const mockFeatureConsistency = {
  date: '2026-10-03',
  sampleCount: 1000,
  passedCount: 998,
  failedCount: 2,
  maxAbsError: 8.5e-7,
  status: 'PASSED',
  checkedAt: '2026-10-03T03:15:00Z',
  detail: [
    { featureName: 'user_click_count_1h', expected: 5.0, actual: 5.0, error: 0, status: 'PASSED' },
    { featureName: 'item_hot_score', expected: 0.8723, actual: 0.8723000001, error: 1e-10, status: 'PASSED' },
    { featureName: 'user_category_pref_101', expected: 0.85, actual: 0.8499999, error: 1e-7, status: 'PASSED' },
    { featureName: 'item_price_norm', expected: 0.4231, actual: 0.42310085, error: 8.5e-7, status: 'PASSED' },
    { featureName: 'user_active_level', expected: 0.72, actual: 0.719999, error: 1e-6, status: 'FAILED' },
    { featureName: 'context_hour_norm', expected: 0.5833, actual: 0.5834, error: 1e-4, status: 'FAILED' }
  ]
}

// ===== 仪表盘统计 Mock =====
export const mockDashboardStats = {
  totalRequests: 1258430,
  avgLatencyMs: 18.5,
  p99LatencyMs: 85,
  cacheHitRate: 0.68,
  activeUsers: 8432,
  recommendationQps: 856,
  behaviorQps: 1820,
  modelDegradationRate: 0.003,
  activeExperiments: 2,
  activeModels: 2,
  systemHealth: 'UP'
}

// 近7天请求量趋势
export const mockRequestTrend = [
  { date: '09-27', requests: 980000, latency: 22.1 },
  { date: '09-28', requests: 1050000, latency: 21.5 },
  { date: '09-29', requests: 1120000, latency: 20.8 },
  { date: '09-30', requests: 1180000, latency: 19.6 },
  { date: '10-01', requests: 1200000, latency: 18.9 },
  { date: '10-02', requests: 1240000, latency: 18.2 },
  { date: '10-03', requests: 1258430, latency: 18.5 }
]

// 召回通道分布
export const mockRecallDistribution = [
  { name: '协同过滤', value: 35 },
  { name: '内容标签', value: 22 },
  { name: '用户画像', value: 18 },
  { name: '热门趋势', value: 15 },
  { name: 'ES文本', value: 6 },
  { name: 'ES语义', value: 4 }
]

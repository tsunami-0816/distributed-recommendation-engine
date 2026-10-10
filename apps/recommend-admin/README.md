# recommend-admin

分布式智能推荐引擎 - 前端管理台与推荐演示系统

## 技术栈

- **框架**: Vue 3 (Composition API) + Vite 5
- **UI 组件库**: Element Plus 2.7
- **状态管理**: Pinia 2
- **路由**: Vue Router 4
- **HTTP 客户端**: Axios
- **图表**: ECharts 5 + vue-echarts
- **工具**: dayjs

## 快速开始

### 环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0（或 pnpm / yarn）

### 安装依赖

```bash
cd recommend-admin
npm install
```

### 开发模式（Mock 数据，无需后端）

```bash
npm run dev
```

浏览器自动打开 `http://localhost:5174`，默认使用 Mock 数据。

### 开发模式（对接真实后端）

1. 启动后端 Spring Boot 服务（默认端口 8084）
2. 修改 `.env.development` 中 `VITE_USE_MOCK=false`
3. 运行 `npm run dev`

Vite 开发服务器会自动代理 `/api`、`/admin`、`/actuator` 到 `http://localhost:8084`。

### 生产构建

```bash
npm run build
```

构建产物输出到 `dist/` 目录。

### 预览生产构建

```bash
npm run preview
```

## 项目结构

```
recommend-admin/
├── public/                  # 静态资源
├── src/
│   ├── api/                 # API 封装层
│   │   ├── request.js       # Axios 实例（拦截器/统一响应处理）
│   │   ├── recommend.js     # 推荐接口
│   │   ├── behavior.js      # 行为采集接口
│   │   ├── userProfile.js   # 用户画像接口
│   │   ├── hotItems.js      # 热门物品接口
│   │   ├── admin/           # 管理后台 API（M11）
│   │   │   ├── experiment.js
│   │   │   ├── model.js
│   │   │   ├── config.js
│   │   │   ├── cache.js
│   │   │   └── featureConsistency.js
│   │   └── index.js         # API 统一导出
│   ├── components/          # 公共组件
│   │   ├── DemoLayout.vue   # 推荐演示前台布局
│   │   ├── AdminLayout.vue  # 管理后台布局
│   │   ├── ItemCard.vue     # 物品卡片
│   │   ├── RecommendResult.vue # 推荐结果展示
│   │   └── StatCard.vue     # 统计卡片
│   ├── mock/                # Mock 数据
│   │   └── index.js
│   ├── router/              # 路由配置
│   │   └── index.js
│   ├── stores/              # Pinia 状态管理
│   │   ├── app.js
│   │   └── recommend.js
│   ├── styles/              # 全局样式
│   │   └── index.scss
│   ├── utils/               # 工具函数
│   │   └── index.js
│   ├── views/               # 页面
│   │   ├── demo/            # 推荐演示前台（6个页面）
│   │   │   ├── HomeRecommend.vue
│   │   │   ├── GuessYouLike.vue
│   │   │   ├── RelatedRecommend.vue
│   │   │   ├── SearchRecommend.vue
│   │   │   ├── HotItems.vue
│   │   │   └── UserProfile.vue
│   │   ├── admin/           # 管理后台（6个页面）
│   │   │   ├── Dashboard.vue
│   │   │   ├── AbExperiments.vue
│   │   │   ├── ModelManagement.vue
│   │   │   ├── SystemConfig.vue
│   │   │   ├── CacheManage.vue
│   │   │   └── FeatureConsistency.vue
│   │   └── NotFound.vue
│   ├── App.vue
│   └── main.js
├── .env.development         # 开发环境变量
├── .env.production          # 生产环境变量
├── .gitignore
├── index.html
├── package.json
├── vite.config.js
└── README.md
```

## 功能模块

### 推荐演示前台 (`/demo`)

| 页面 | 路由 | 场景 | 说明 |
|-----|------|------|------|
| 首页推荐 | `/demo/home` | home | 4路召回（ItemCF/标签/画像/热门） |
| 猜你喜欢 | `/demo/guess` | guess | ItemCF为主 + 画像 + 标签 |
| 相关推荐 | `/demo/related` | related | 基于itemId的相似度推荐 |
| 搜索推荐 | `/demo/search` | search | ES文本 + 语义 RRF融合 |
| 热门榜单 | `/demo/hot` | - | 热度排行榜（click×0.7 + favorite×0.3） |
| 用户画像 | `/demo/profile` | - | 长期/短期画像 + 融合标签可视化 |

### 管理后台 (`/admin`)

| 页面 | 路由 | 对应后端模块 | 说明 |
|-----|------|------------|------|
| 系统概览 | `/admin/dashboard` | M18 监控 | 请求量/延迟/缓存命中率/组件健康 |
| A/B实验管理 | `/admin/experiments` | M10 + M11-T01 | 实验CRUD/启停/报告/统计显著性 |
| 模型管理 | `/admin/models` | M11-T02 | 模型版本/激活/回滚/质量门禁 |
| 系统配置 | `/admin/configs` | M11-T03 | 动态配置热更新 |
| 缓存管理 | `/admin/cache` | M11-T04 | 按前缀/指定Key刷新缓存 |
| 特征一致性 | `/admin/feature-consistency` | M11-T05 | 在线/离线特征对比校验 |

## 后端接口对接

### 统一响应格式

所有后端接口返回 `Result<T>` 结构：

```json
{
  "code": 0,
  "message": "success",
  "traceId": "xxx",
  "timestamp": 1727937000123,
  "data": { ... }
}
```

前端 Axios 拦截器自动处理：
- `code=0`：成功，返回完整响应
- `code=400001/400002/400003`：业务降级，不报错，返回数据供页面处理
- `code=200001/200003`：鉴权失败，跳转登录
- `code=600001`：限流，提示用户
- 其他：ElMessage 报错

### 核心接口

| 接口 | 方法 | 路径 | 说明 |
|-----|------|------|------|
| 推荐 | GET | `/api/v1/recommend` | 4种场景推荐 |
| 行为采集 | POST | `/api/v1/behavior` | 上报点击/收藏等行为 |
| 用户画像 | GET | `/api/v1/user/{userId}/profile` | 查询用户画像 |
| 热门物品 | GET | `/api/v1/hot/items` | 热门榜单 |
| 健康检查 | GET | `/actuator/health` | 系统健康状态 |
| 指标 | GET | `/actuator/prometheus` | Prometheus指标 |

## 开发规范

- 使用 Vue 3 Composition API + `<script setup>` 语法
- 组件命名使用 PascalCase，文件命名使用 PascalCase.vue
- API 调用统一封装在 `src/api/` 目录
- 全局状态使用 Pinia，页面局部状态使用 `ref`/`reactive`
- 样式使用 SCSS，全局变量定义在 `src/styles/index.scss`
- 时间格式化使用 `dayjs` 或 `src/utils/index.js` 中的工具函数

## License

MIT

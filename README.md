# 分布式智能推荐引擎

基于 Spring Boot + Vue3 的分布式智能推荐系统，包含推荐引擎后端服务与管理后台前端。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.x、Maven、Java 17 |
| 前端 | Vue 3、Vite、Node.js 16+ |
| 缓存 | Redis |
| 消息队列 | RabbitMQ |

## 目录结构

```
distributed-recommendation-engine/
├── apps/
│   ├── recommend-admin/          # 前端管理后台 (Vue3 + Vite)
│   └── recommendation-engine/    # 后端推荐引擎 (Spring Boot)
├── docs/
│   ├── 01_architecture/          # 架构设计、评审报告、基线说明
│   ├── 02_development/           # 任务拆解、前后端开发实现文档
│   ├── 03_operation/             # 操作指南、联调报告、交付复盘
│   └── 04_test/                  # 测试报告、Bug清单、回归测试
├── scripts/                      # 一键启动/停止脚本 (Windows)
├── .gitignore
└── README.md
```

## 快速启动

### 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 16+
- Redis、RabbitMQ（按需）

### 启动步骤

1. **环境检查**：双击 `scripts/01-check-env.bat`
2. **一键启动**：双击 `scripts/04-start-all.bat`
   - 后端服务：http://localhost:8084
   - 前端页面：http://localhost:5174
3. **停止服务**：双击 `scripts/05-stop-all.bat`

也可分别启动：
- 后端：`scripts/02-start-backend.bat`
- 前端：`scripts/03-start-frontend.bat`

## 文档导航

| 分类 | 文档 |
|------|------|
| 架构设计 | [技术架构设计文档](docs/01_architecture/技术架构设计文档.md)、[项目基线说明书](docs/01_architecture/项目基线说明书.md) |
| 开发规范 | [任务拆解与开发规范](docs/02_development/任务拆解与开发规范.md)、[前端开发实现文档](docs/02_development/前端开发实现文档.md)、[后端开发实现文档](docs/02_development/后端开发实现文档.md) |
| 运维操作 | [操作指南](docs/03_operation/操作指南.md)、[联调验证报告](docs/03_operation/联调验证报告.md)、[项目端口说明](docs/03_operation/项目端口.docx) |
| 测试验收 | [最终验收测试报告](docs/04_test/最终验收测试报告_v1.0.md)、[项目交付与复盘](docs/03_operation/项目交付包与复盘报告.md) |

## 端口说明

| 服务 | 端口 |
|------|------|
| 后端 Spring Boot | 8084 |
| 前端 Vite Dev Server | 5174 |
| Redis | 6379 |
| RabbitMQ | 5672 (管理界面 15672) |

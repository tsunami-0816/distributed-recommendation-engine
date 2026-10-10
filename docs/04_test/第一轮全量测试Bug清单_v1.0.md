# 第一轮全量测试Bug清单

| 项目名称 | 分布式智能推荐引擎 |
|---------|-------------------|
| 文档版本 | v1.0 |
| 测试日期 | 2026-10-04 |
| 测试基线 | 项目基线说明书 v1.0 + 技术架构设计文档 v2.0 + 联调验证报告 v1.0 |
| 测试范围 | 后端 recommendation-engine（60个Java类，4个核心API + 15个管理后台API）+ 前端 recommend-admin（12个页面，6前台+6后台） |
| 测试方式 | 代码全量走查 + 接口契约验证 + 架构文档对齐校验 + 边界条件分析 |

---

## 一、测试范围

| 测试域 | 覆盖内容 |
|-------|---------|
| 推荐主链路 | 4种场景（home/guess/related/search）→ A/B分流 → 缓存检查 → 多路召回 → 精排 → 重排 → 缓存写入 → 曝光埋点 |
| 行为采集链路 | 5种行为类型（click/view/favorite/impression/conversion）→ eventId生成 → 缓存失效触发 |
| 用户画像链路 | 长期画像/短期画像/融合标签查询，融合权重0.6/0.4 |
| 热门榜单链路 | 全局热门/按类目筛选，热度分公式 click×0.7+favorite×0.3 |
| 管理后台链路 | A/B实验CRUD+状态机、模型版本管理+激活回滚、动态配置读写、缓存刷新、特征一致性校验、仪表盘统计 |
| 横切能力 | 全局异常处理（11种异常）、TraceId全链路透传、Resilience4j限流（3组配置）、统一响应格式、CORS配置 |
| 前端 | 6个推荐演示页 + 6个管理后台页 + Axios拦截器 + 路由 + Mock降级 |

---

## 二、严重等级划分

- **P0 致命**：功能完全不可用、数据错误、系统崩溃、核心链路中断
- **P1 严重**：核心功能异常、性能不达标、无异常兜底
- **P2 一般**：非核心功能异常、交互体验问题
- **P3 轻微**：样式、文案、提示类小问题

---

## 三、Bug清单（P0 + P1，共16项）

### P0-01【致命】A/B实验框架完全未生效，分流逻辑硬编码

| 项目 | 内容 |
|-----|------|
| **Bug编号** | BUG-2026-10-001 |
| **严重等级** | P0 致命 |
| **Bug描述** | A/B实验分流逻辑在`RecommendationFacade.assignAbGroup()`中硬编码：experimentId固定为`"default_exp"`，分流比例固定50%/50%。未从`AbExperimentService`读取运行中的实验配置，管理后台创建/启动/修改实验对推荐链路无任何影响。实验的trafficPercent、groupsConfig、modelVersion等配置全部失效。 |
| **复现步骤** | 1. 调用`POST /admin/ab/experiments`创建实验，配置实验组modelVersion=deepfm_v2，流量30%<br>2. 调用`POST /admin/ab/experiments/{id}/start`启动实验<br>3. 连续请求`GET /api/v1/recommend?scene=home&userId=user_001`，观察响应中abGroup和modelVersion<br>4. 预期：按实验配置分流，实验组返回modelVersion=deepfm_v2<br>5. 实际：abGroup始终按硬编码50/50分流，modelVersion始终为deepfm_v1，实验配置完全不生效 |
| **预期结果** | 推荐链路从实验配置服务获取running状态实验，按trafficPercent和groupsConfig动态分流，实验组使用指定的modelVersion |
| **影响范围** | CORE-10 A/B实验框架核心模块完全不可用；管理后台A/B实验管理6个接口形同虚设；无法进行CTR/CVR效果对比实验；项目基线ALG-01/ALG-02指标无法验证 |

---

### P0-02【致命】模型管理激活后不生效，活跃模型与配置脱节

| 项目 | 内容 |
|-----|------|
| **Bug编号** | BUG-2026-10-002 |
| **严重等级** | P0 致命 |
| **Bug描述** | `MockModelManagementService.activateModel()`仅更新内存中模型对象的status字段为active，但推荐链路`RecommendationFacade`通过`properties.getModelInference().getActiveModel()`读取模型版本（固定为配置文件中的deepfm_v1）。管理后台激活新模型后，推荐链路仍使用旧模型，模型版本切换功能完全无效。 |
| **复现步骤** | 1. 调用`POST /admin/models/deepfm/v2/activate`激活deepfm_v2（已通过一致性校验）<br>2. 调用`GET /admin/models`确认deepfm_v2状态为active，deepfm_v1为inactive<br>3. 请求`GET /api/v1/recommend?scene=home&userId=user_001`，观察响应modelVersion<br>4. 预期：modelVersion=deepfm_v2<br>5. 实际：modelVersion=deepfm_v1，激活操作对推荐链路无影响 |
| **预期结果** | 模型激活后，推荐链路实时读取新的活跃模型版本进行推理和缓存key构建 |
| **影响范围** | CORE-03/CORE-04精排模型管理不可用；模型回滚功能无效；A/B实验中实验组模型切换无法验证；缓存key中modelVersion维度失去意义 |

---

### P0-03【致命】系统配置更新后不生效，动态配置热更新未实现

| 项目 | 内容 |
|-----|------|
| **Bug编号** | BUG-2026-10-003 |
| **严重等级** | P0 致命 |
| **Bug描述** | `MockSystemConfigService.updateConfig()`仅更新内存store中的配置值，没有任何机制将变更同步到`RecommendProperties`（Spring `@ConfigurationProperties`绑定的配置类）。管理后台修改缓存TTL、模型超时、默认pageSize等配置后，运行中的系统仍使用旧配置值。架构文档约定的"配置写入Redis config:dynamic支持热更新"未实现。 |
| **复现步骤** | 1. 调用`PUT /admin/configs/recommend.cache.normal-ttl`，body={"configValue":"30"}<br>2. 调用`GET /admin/configs`确认配置值已更新为30<br>3. 请求推荐接口，观察缓存写入日志中的TTL<br>4. 预期：普通用户缓存TTL=30s<br>5. 实际：缓存TTL仍为60s（application.yml中的旧值），配置更新不生效 |
| **预期结果** | 配置更新后，相关组件实时读取新配置值，无需重启服务 |
| **影响范围** | M11-T03动态配置管理不可用；缓存分层TTL、模型超时、默认pageSize等运行时参数无法动态调整；架构文档"配置项外部化"目标未达成 |

---

### P0-04【致命】管理后台API鉴权完全缺失，可任意修改系统

| 项目 | 内容 |
|-----|------|
| **Bug编号** | BUG-2026-10-004 |
| **严重等级** | P0 致命 |
| **Bug描述** | 前端`request.js`为`/admin/**`请求附加`X-API-Key`请求头，但后端`WebConfig.addInterceptors()`为空，未注册任何鉴权拦截器。所有15个管理后台接口（实验管理、模型激活/回滚、配置修改、缓存刷新、特征一致性触发）无任何访问控制。任意可访问网络的客户端均可直接调用这些接口修改系统状态。 |
| **复现步骤** | 1. 不携带任何认证信息，直接调用`curl -X PUT http://localhost:8084/admin/configs/recommend.cache.normal-ttl -H "Content-Type: application/json" -d '{"configValue":"1"}'`<br>2. 预期：返回401未授权<br>3. 实际：返回200成功，配置被修改 |
| **预期结果** | 管理后台接口必须校验API Key或Token，未授权请求返回401 |
| **影响范围** | 全部15个管理后台接口；系统安全性严重缺失；可被恶意修改配置、清空缓存、激活未校验模型；不符合生产环境基本安全要求 |

---

### P1-01【严重】仪表盘统计数据全部计算错误，QPS/命中率/P99均不准

| 项目 | 内容 |
|-----|------|
| **Bug编号** | BUG-2026-10-005 |
| **严重等级** | P1 严重 |
| **Bug描述** | `DashboardAdminController.getDashboardStats()`中多项指标计算逻辑错误：<br>(1) QPS公式`totalRequests / (mean(seconds) * totalRequests)`实际等于`1/mean(seconds)`，不是每秒请求数；<br>(2) 缓存命中率`cacheSize / totalRequests`用缓存条目数除以总请求数，完全不是命中率概念；<br>(3) P99延迟使用`Timer.max()`返回历史最大值，不是P99分位数；<br>(4) 行为QPS直接使用`behaviorTimer.count()`（累计请求数）作为QPS；<br>(5) 模型降级率硬编码0.003；<br>(6) 系统健康状态硬编码"UP"。 |
| **复现步骤** | 1. 发送100次推荐请求，平均耗时50ms<br>2. 调用`GET /admin/dashboard/stats`<br>3. 预期：recommendationQps≈合理值，cacheHitRate基于命中/总请求计算，p99LatencyMs为分位数<br>4. 实际：recommendationQps=20（=1/0.05），cacheHitRate=缓存条目数/100（可能>1被截断到0.95），p99LatencyMs=历史最大耗时 |
| **预期结果** | QPS=请求数/时间窗口，缓存命中率=命中次数/总请求次数，P99=真实分位数，所有指标从Micrometer或业务计数器准确获取 |
| **影响范围** | 仪表盘页面8项统计卡片中至少6项数据错误；运维人员无法基于仪表盘判断系统真实状态；性能问题无法及时发现；AUX-04监控与日志目标未达成 |

---

### P1-02【严重】热门榜单热度分可能为负数，数据错误

| 项目 | 内容 |
|-----|------|
| **Bug编号** | BUG-2026-10-006 |
| **严重等级** | P1 严重 |
| **Bug描述** | `HotItemsController.getHotItems()`中热度分计算公式为`hotScore = 1.0 - i * 0.01 - random.nextDouble() * 0.02`。当limit=100时，i=99，基础值=1.0-0.99=0.01，再减去0~0.02的随机数，结果可能为负数（-0.01~0.01）。热度分为负数不符合业务逻辑，且clickCount和favoriteCount基于hotScore计算也会出现异常。 |
| **复现步骤** | 1. 调用`GET /api/v1/hot/items?limit=100`<br>2. 检查返回列表中rank=100的物品的hotScore、clickCount、favoriteCount<br>3. 预期：hotScore≥0，clickCount≥0，favoriteCount≥0<br>4. 实际：部分物品hotScore为负数，clickCount和favoriteCount为0或负数 |
| **预期结果** | hotScore下限截断为0，或调整公式确保在limit=100时仍为正数 |
| **影响范围** | 热门榜单接口（L4链路）；limit>50时大概率出现负分；前端展示异常；热度排序在尾部失真 |

---

### P1-03【严重】热门榜单limit参数缺少下限校验，传入非正数返回空数据

| 项目 | 内容 |
|-----|------|
| **Bug编号** | BUG-2026-10-007 |
| **严重等级** | P1 严重 |
| **Bug描述** | `HotItemsController.getHotItems()`的limit参数只有`@RequestParam(defaultValue="20")`，没有`@Min(1)`校验。代码中`int actualLimit = Math.min(limit, 100)`，当传入limit=0或负数时，actualLimit为0或负数，`IntStream.range(0, negative)`返回空流，接口返回空列表但code=0，调用方无法区分"无热门数据"和"参数错误"。 |
| **复现步骤** | 1. 调用`GET /api/v1/hot/items?limit=-1`<br>2. 预期：返回参数校验错误（code=100001）<br>3. 实际：返回code=0，data=[]（空列表） |
| **预期结果** | limit参数添加@Min(1)校验，非正数返回100001参数校验失败 |
| **影响范围** | 热门榜单接口；前端异常输入或恶意调用时返回误导性空数据；与推荐接口pageSize的校验标准不一致 |

---

### P1-04【严重】曝光埋点同步阻塞推荐响应，违反异步设计约定

| 项目 | 内容 |
|-----|------|
| **Bug编号** | BUG-2026-10-008 |
| **严重等级** | P1 严重 |
| **Bug描述** | `RecommendationFacade.recommend()`在返回响应前同步调用`behaviorCollectorService.trackImpression()`。`MockBehaviorCollectorService.trackImpression()`内部同步循环调用`collect()`，每个曝光事件执行MurmurHash3计算eventId + INFO级日志输出。当pageSize=50时，50次循环计算和日志输出直接阻塞推荐响应时间。架构文档明确要求"异步发送曝光埋点"，实际为同步执行。 |
| **复现步骤** | 1. 请求`GET /api/v1/recommend?scene=home&userId=user_001&pageSize=50`（缓存未命中）<br>2. 观察响应中duration.totalMs和服务端日志<br>3. 预期：曝光埋点异步执行，不阻塞响应，totalMs不包含埋点耗时<br>4. 实际：trackImpression在响应返回前同步执行，50次MurmurHash3计算+50条INFO日志增加响应耗时 |
| **预期结果** | 使用`@Async`或线程池异步执行曝光埋点，不阻塞推荐主链路响应 |
| **影响范围** | 推荐主链路（L1）性能；pageSize越大影响越明显；高并发下日志IO可能成为瓶颈；违反架构文档"曝光埋点异步"设计约定 |

---

### P1-05【严重】缓存按用户失效采用全表遍历，高并发下性能瓶颈

| 项目 | 内容 |
|-----|------|
| **Bug编号** | BUG-2026-10-009 |
| **严重等级** | P1 严重 |
| **Bug描述** | `RecommendCacheService.invalidateByUser(String userId)`使用`cache.keySet().removeIf(key -> key.contains(":" + userId + ":"))`实现，每次调用都遍历整个缓存Map进行字符串包含匹配。行为采集接口每次上报都会调用此方法，在缓存条目较多时（生产环境可能百万级），单次失效耗时不可接受，且行为上报高频调用会成为系统性能瓶颈。 |
| **复现步骤** | 1. 预写入10000条推荐缓存（模拟1000用户×10场景）<br>2. 调用`POST /api/v1/behavior`上报行为（触发invalidateByUser）<br>3. 测量行为接口响应时间<br>4. 预期：缓存失效O(1)或O(用户缓存数)，行为接口<10ms<br>5. 实际：遍历10000条key进行字符串匹配，耗时随缓存量线性增长 |
| **预期结果** | 维护userId→cacheKeys的反向索引，失效时只遍历该用户的缓存key；或使用Redis SCAN+UNLINK批量删除 |
| **影响范围** | 行为采集链路（L2）性能；缓存量越大行为接口越慢；高并发行为上报可能导致CPU飙升；生产环境Redis替换后需重新设计失效策略 |

---

### P1-06【严重】缓存命中时耗时明细未更新，监控数据错误

| 项目 | 内容 |
|-----|------|
| **Bug编号** | BUG-2026-10-010 |
| **严重等级** | P1 严重 |
| **Bug描述** | `RecommendationFacade.recommend()`缓存命中分支的注释写明"更新耗时明细中的缓存时间，直接返回"，但实际代码直接`return cached`，未更新`cached.getDuration().setCacheMs(cacheMs)`。缓存命中的响应中duration仍为首次缓存未命中时计算的耗时（包含召回+排序等全部耗时），导致缓存命中的响应耗时数据严重失真。 |
| **复现步骤** | 1. 首次请求`GET /api/v1/recommend?scene=home&userId=cache_test`，记录duration.totalMs（如80ms）<br>2. 第二次相同请求（缓存命中），观察响应duration<br>3. 预期：缓存命中时totalMs≈cacheMs（<5ms），其他阶段为0<br>4. 实际：duration仍为首次的80ms，无法区分缓存命中和未命中的耗时 |
| **预期结果** | 缓存命中时更新duration，仅保留cacheMs，其他阶段置0，totalMs=cacheMs |
| **影响范围** | 推荐接口性能监控；缓存命中率与响应时间关联分析失效；无法基于duration字段判断缓存是否真正生效；AUX-04监控目标未达成 |

---

### P1-07【严重】单Key缓存刷新误删批量缓存，前缀匹配逻辑错误

| 项目 | 内容 |
|-----|------|
| **Bug编号** | BUG-2026-10-011 |
| **严重等级** | P1 严重 |
| **Bug描述** | `CacheAdminController.refreshCache()`中，当传入`key`参数（单Key失效）时，代码复用`invalidateByPrefix(request.getKey())`，将完整key作为前缀进行匹配删除。例如传入key=`rec:result:home:user1:control:deepfm_v1`，会误删所有以该字符串开头的缓存，包括`rec:result:home:user10:control:deepfm_v1`、`rec:result:home:user11:control:deepfm_v1`等其他用户的缓存。 |
| **复现步骤** | 1. 预写入user1和user10的推荐缓存<br>2. 调用`POST /admin/cache/refresh`，body={"key":"rec:result:home:user1:control:deepfm_v1"}<br>3. 检查user10的缓存是否仍存在<br>4. 预期：仅删除user1的指定缓存<br>5. 实际：user10的缓存也被删除（因为key以user1开头时也匹配user10） |
| **预期结果** | 单Key失效使用精确匹配（cache.remove(key)），而非前缀匹配 |
| **影响范围** | 缓存管理接口（M11-T04）；管理后台单Key刷新操作可能误删其他用户缓存；缓存数据意外丢失导致推荐重新计算，增加系统负载 |

---

### P1-08【严重】系统配置更新接口参数校验缺失，可导致配置值被置空

| 项目 | 内容 |
|-----|------|
| **Bug编号** | BUG-2026-10-012 |
| **严重等级** | P1 严重 |
| **Bug描述** | `SystemConfigAdminController.updateConfig()`中，`String value = body.get("configValue")`未校验请求体和configValue字段。如果请求体为空JSON`{}`或不含configValue字段，value为null，传入`MockSystemConfigService.updateConfig()`后执行`existing.setConfigValue(null)`，配置值被置空。后续读取该配置的组件可能出现NPE或逻辑异常。 |
| **复现步骤** | 1. 调用`PUT /admin/configs/recommend.cache.normal-ttl`，body={}（空JSON）<br>2. 调用`GET /admin/configs`查看该配置项<br>3. 预期：返回参数校验错误，配置值不变<br>4. 实际：返回200成功，configValue被置为null |
| **预期结果** | 校验configValue非空，为空时返回100002参数缺失错误 |
| **影响范围** | 系统配置管理接口（M11-T03）；配置值被置空可能导致缓存TTL、模型超时等参数异常；前端调用时如果字段名拼写错误会静默置空配置 |

---

### P1-09【严重】A/B实验运行中可被修改，状态机存在缺陷

| 项目 | 内容 |
|-----|------|
| **Bug编号** | BUG-2026-10-013 |
| **严重等级** | P1 严重 |
| **Bug描述** | `MockAbExperimentService.updateExperiment()`未校验实验状态，running状态的实验可以被修改trafficPercent、groupsConfig、startTime、endTime等核心配置。运行中修改实验配置会导致分流逻辑在实验过程中发生变化，实验数据被污染，A/B对比结果失去统计意义。正确的状态机应该只允许draft/paused状态的实验被修改。 |
| **复现步骤** | 1. 创建实验并启动（status=running）<br>2. 调用`PUT /admin/ab/experiments/{id}`，修改trafficPercent从50到80<br>3. 预期：返回600003实验状态不允许该操作<br>4. 实际：返回200成功，running状态实验配置被修改，configVersion自增 |
| **预期结果** | running状态的实验不允许修改核心配置，仅允许draft/paused状态修改 |
| **影响范围** | A/B实验管理（M11-T01）；实验数据可信度受损；CTR/CVR效果对比可能因中途改配置而失效；项目基线ALG-01/ALG-02验证结果不可信 |

---

### P1-10【严重】无任何自动化测试，核心链路零测试覆盖

| 项目 | 内容 |
|-----|------|
| **Bug编号** | BUG-2026-10-014 |
| **严重等级** | P1 严重 |
| **Bug描述** | `src/test`目录为空，项目没有任何单元测试、集成测试或端到端测试。核心链路（推荐主流程、A/B分流、缓存失效、异常处理、场景策略校验）全部零测试覆盖。项目基线说明书5.1节明确要求"每个模块有对应的可运行Demo或测试用例"，当前不满足验收标准。后续修复和重构无法通过自动化测试验证回归。 |
| **复现步骤** | 1. 执行`mvn test`<br>2. 预期：至少有核心模块的单元测试执行<br>3. 实际：No tests to run，BUILD SUCCESS但零测试执行 |
| **预期结果** | 核心模块（RecommendationFacade、RecommendCacheService、SceneStrategyFactory、GlobalExceptionHandler）有单元测试覆盖，关键路径有集成测试 |
| **影响范围** | 整个项目质量保障体系；本轮P0/P1修复后无法自动化验证；后续迭代回归风险高；不满足项目基线验收标准 |

---

### P1-11【严重】行为采集eventId生成存在碰撞风险，幂等性不可靠

| 项目 | 内容 |
|-----|------|
| **Bug编号** | BUG-2026-10-015 |
| **严重等级** | P1 严重 |
| **Bug描述** | `MockBehaviorCollectorService.collect()`中eventId生成逻辑为`MurmurHash3(userId + itemId + actionType + eventTime.toString())`，各字段直接拼接无分隔符。例如userId="ab"+itemId="c"与userId="a"+itemId="bc"在actionType和eventTime相同时会产生完全相同的raw字符串，导致不同事件生成相同eventId。架构文档M08-T01要求eventId用于幂等去重，碰撞会导致行为事件被错误去重或覆盖。 |
| **复现步骤** | 1. 上报行为：userId="ab", itemId="c", actionType="click", eventTime=2026-10-04T10:00:00Z<br>2. 上报行为：userId="a", itemId="bc", actionType="click", eventTime=2026-10-04T10:00:00Z<br>3. 比较两次返回的eventId<br>4. 预期：两个不同事件的eventId不同<br>5. 实际：raw字符串均为"abcclick2026-10-04T10:00:00Z"，eventId相同 |
| **预期结果** | 各字段间使用分隔符（如`userId + "|" + itemId + "|" + actionType + "|" + eventTime`），或加入UUID/序列号确保唯一性 |
| **影响范围** | 行为采集链路（L2）；事件幂等性不可靠；Kafka去重、行为明细唯一索引可能误删有效事件；实时特征计算可能丢失行为数据 |

---

### P1-12【严重】Resilience4j search-api限流器配置未使用，配置与实现不一致

| 项目 | 内容 |
|-----|------|
| **Bug编号** | BUG-2026-10-016 |
| **严重等级** | P1 严重 |
| **Bug描述** | `application.yml`中配置了3组Resilience4j限流器：recommend-api（1000 QPS）、behavior-api（2000 QPS）、search-api（500 QPS）。但`RecommendController`上只有`@RateLimiter(name = "recommend-api")`，search场景没有使用search-api限流器。架构文档约定搜索场景因ES查询开销大应限制为500 QPS，实际所有场景共用1000 QPS限流，搜索场景可能因ES压力过大导致整体推荐接口雪崩。 |
| **复现步骤** | 1. 以800 QPS持续请求`GET /api/v1/recommend?scene=search&keyword=test`<br>2. 观察是否触发限流<br>3. 预期：超过500 QPS时触发search-api限流（600001）<br>4. 实际：未触发限流（因为使用recommend-api的1000阈值），search场景可跑满1000 QPS |
| **预期结果** | search场景使用独立的search-api限流器（500 QPS），或在Controller中根据scene动态选择限流器 |
| **影响范围** | 搜索推荐场景；ES查询压力不可控；高并发搜索可能拖慢整个推荐接口；架构文档M01-T06限流设计未完全落地 |

---

## 四、待优化建议（P2 + P3，本轮不修复）

### P2级问题

| 编号 | 问题描述 | 涉及模块 |
|-----|---------|---------|
| P2-01 | 前端Dashboard趋势图（近7天请求量/延迟）和召回通道饼图仍使用硬编码mock数据，未从后端获取真实时序数据 | 前端 Dashboard.vue |
| P2-02 | 前端默认`VITE_USE_MOCK=true`，管理后台需手动修改环境变量才能对接真实后端，新用户容易误以为后端未实现 | 前端 .env.development |
| P2-03 | 前端Dashboard健康状态表初始化为8个组件的硬编码数据，虽然onMounted尝试从/actuator/health获取，但Actuator返回的components结构（diskSpace/ping）与业务组件（MySQL/Redis等）不匹配，实际不会替换 | 前端 Dashboard.vue |
| P2-04 | RelatedStrategy/SearchStrategy的validate()不校验userId，但RecommendController已通过@NotBlank强制校验，场景策略级校验与Controller级校验重复且不一致 | 场景策略层 |
| P2-05 | MockRecallService每次recall调用创建`new Random()`实例，非线程安全且性能略差，应使用ThreadLocalRandom或共享Random实例 | MockRecallService |
| P2-06 | 缓存层使用内存ConcurrentHashMap实现，未接入Redis，缓存数据在服务重启后丢失，且无法跨实例共享（当前单机环境可接受，集群部署前必须替换） | RecommendCacheService |
| P2-07 | 无日志告警机制，ERROR级日志和限流触发不会主动通知运维人员 | 监控告警 |
| P2-08 | A/B实验报告接口仅返回实验配置和mock指标，未实现真实的CTR/CVR/时长统计计算和P值显著性检验 | MockAbExperimentService |
| P2-09 | 特征一致性校验触发后状态永远停留在RUNNING，没有异步任务更新为PASSED/FAILED，触发后查询结果不一致 | MockFeatureConsistencyService |
| P2-10 | 模型管理中rollbackModel直接调用activateModel，未记录回滚历史和回滚原因，无法追溯模型版本变更 | MockModelManagementService |

### P3级问题

| 编号 | 问题描述 | 涉及模块 |
|-----|---------|---------|
| P3-01 | Swagger文档中部分接口的@Parameter描述不够详细（如device参数未列出可选值枚举） | Controller层 |
| P3-02 | 前端StatCard组件的trend（趋势百分比）全部硬编码，未从后端获取真实环比数据 | 前端 StatCard.vue |
| P3-03 | 错误码枚举中部分错误码（如20xxxx鉴权系列、500002/500004/500005）定义了但代码中未使用 | ErrorCode |
| P3-04 | 日志中部分DEBUG级信息在生产环境可能过于详细（如每次缓存命中/写入都打日志），建议调整为TRACE级 | 全局日志 |
| P3-05 | 前端404页面（NotFound.vue）样式简单，无返回首页按钮和错误码展示 | 前端 NotFound.vue |
| P3-06 | application.yml中`spring.jackson.date-format`配置为`yyyy-MM-dd HH:mm:ss`，但BehaviorEventDTO.eventTime使用Instant类型且标注`@JsonFormat(shape = JsonFormat.Shape.STRING)`，全局配置与局部注解可能冲突 | 配置/序列化 |

---

## 五、未覆盖场景与边界

| 类别 | 未覆盖场景 | 原因/说明 |
|-----|-----------|----------|
| **中间件集成** | 真实Kafka/Flink/Spark/ES/Redis/MongoDB/MySQL集成测试 | 当前全部为Mock实现，中间件未实际部署和接入 |
| **性能压测** | 1000 QPS持续压测、P50/P99响应时间验证、限流阈值验证 | 未执行JMeter/wrk压测，性能指标（PERF-01~PERF-03）未验证 |
| **并发测试** | 缓存并发更新、A/B分流并发、行为上报并发、配置更新并发 | 未执行多线程并发测试，线程安全问题未暴露 |
| **稳定性测试** | 长时间运行（24h+）内存泄漏、缓存膨胀、连接池耗尽 | 未执行稳定性测试 |
| **异常注入** | 下游服务超时/异常/不可用时的三级降级链验证（DeepFM→XGBoost→规则） | MockRankService未模拟超时和异常，降级链未实际触发 |
| **大数据量** | 10000用户×10000物品×100万行为记录下的召回/排序/缓存性能 | Mock数据为随机生成，未达到基线要求的数据规模 |
| **数据一致性** | 实时特征更新延迟（<5s）、离线画像T+1更新、行为→缓存失效端到端延迟 | Flink/Spark未实际运行，实时链路未验证 |
| **集群部署** | 多节点部署、负载均衡、缓存共享、会话一致性 | 单机伪分布式环境，集群迁移仅文档未实操 |
| **安全测试** | SQL注入、XSS、CSRF、越权访问、API Key暴力破解 | 未执行安全扫描和渗透测试 |
| **兼容性测试** | Chrome/Firefox/Edge/Safari浏览器渲染、移动端适配、不同分辨率 | 未执行跨浏览器测试 |
| **算法效果** | XGBoost AUC≥0.78、DeepFM AUC≥0.80、CTR提升≥22%、长尾曝光≥20% | 模型为Mock，未训练真实模型，算法效果指标（ALG-01~ALG-06）未验证 |
| **边界输入** | 超长userId/itemId/keyword（10000字符）、特殊字符（SQL注入/XSS payload）、Unicode/emoji、空字符串、空格字符串 | 未执行模糊测试和边界输入测试 |
| **时间边界** | 跨年/跨月/跨时区请求、夏令时切换、eventTime为未来时间/远古时间 | 未执行时间边界测试 |
| **缓存边界** | 缓存刚好过期时的并发请求（缓存击穿）、大量不同userId的缓存（缓存穿透）、热点key（缓存雪崩） | 未执行缓存专项测试 |

---

## 六、本轮修复优先级汇总

| 优先级 | 数量 | Bug编号 | 修复建议 |
|-------|------|---------|---------|
| **P0** | 4项 | BUG-001~004 | 必须本轮修复，阻断核心功能和系统安全 |
| **P1** | 12项 | BUG-005~016 | 必须本轮修复，影响性能、数据正确性和可维护性 |
| **P2** | 10项 | P2-01~10 | 下版本修复，非核心功能和体验优化 |
| **P3** | 6项 | P3-01~06 | 下版本或后续迭代修复，样式文案类小问题 |

**修复顺序建议**：P0-04（鉴权）→ P0-01（A/B分流）→ P0-02（模型激活）→ P0-03（配置热更新）→ P1-01（仪表盘数据）→ P1-04（曝光异步）→ P1-05（缓存失效性能）→ 其余P1按依赖关系排序。

---

## 七、联调报告问题闭环说明

| 联调报告编号 | 问题描述 | 当前状态 | 说明 |
|-------------|---------|---------|------|
| P-01 | 行为上报→推荐缓存失效未实现 | ✅ 已修复 | BehaviorController已注入RecommendCacheService并调用invalidateByUser |
| P-02 | M11管理后台API全部未实现 | ✅ 已实现 | 6个Admin Controller + 5个Mock Service已全部实现，共15个接口 |
| P-03 | 未实现路径返回500而非404 | ✅ 已修复 | GlobalExceptionHandler已添加NoResourceFoundException处理器 |
| P-04 | JSON请求体格式错误返回500而非400 | ✅ 已修复 | GlobalExceptionHandler已添加HttpMessageNotReadableException处理器 |
| P-05 | Dashboard页面未对接真实监控数据 | ⚠️ 部分修复 | 已新增DashboardAdminController，但指标计算错误（见BUG-005），前端趋势图仍为mock |
| P-06 | 召回为空时code不会是400001 | ✅ 已修复 | RecommendResponseDTO新增degradedCode字段，Controller据此返回降级响应 |
| P-07 | BusinessException自定义message被忽略 | ✅ 已修复 | GlobalExceptionHandler.handleBusinessException优先使用e.getMessage() |
| P-08 | Mock推荐分数可能超过1.0 | ✅ 已修复 | MockRankService生成分数时已添加Math.min(score, 1.0)截断 |

---

*文档结束*
*生成时间：2026-10-04*
*测试工程师：AI全量测试助手*

# 月结报表管理系统 - 开发进度跟踪

> 最后更新: 2026-04-24
> 当前版本: v1.3.0 (P3质量改进版)

## 项目概述

基于Spring Boot 3.2.0 + MyBatis-Plus + PostgreSQL的多模块Maven项目，用于月结报表流程管理。

系统包含：系统管理、流程引擎、数据导入、规则引擎、异常处理、通知模块六大核心功能。

---

## 阶段进度总览

| 阶段 | 名称 | 状态 | 完成度 | 代码行数 |
|------|------|------|--------|----------|
| M1 | 系统管理 | ✅ 已完成 | 100% (24/24) | ~3,200 |
| M2 | 流程引擎 | ✅ 已完成 | 100% (21/21) | ~2,800 |
| M3 | 数据导入 | ✅ 已完成 | 100% (12/12) | ~1,800 |
| M4 | 规则引擎 | ✅ 已完成 | 100% (10/10) | ~1,500 |
| M5 | 异常处理 | ✅ 已完成 | 100% (10/10) | ~900 |
| M6 | 通知模块 | ✅ 已完成 | 100% (8/8) | ~700 |
| M7 | 测试验收 | ✅ 已完成 | 100% (8/8) | - |
| **总计** | | **✅ 全部完成** | **93/93** | **~10,900** |

---

## 模块架构

```
monthly-close-parent
├── mc-common        # 公共模块 (Result, Exception, Enums, Utils, Annotations)
├── mc-system        # 系统管理 (Org, User, Role, Menu, Period)
├── mc-workflow      # 流程引擎 (Template, Instance, Task, Trace)
├── mc-data          # 数据导入 (Batch, Record, Template, POI/OpenCSV)
├── mc-rule          # 规则引擎 (RuleConfig, RuleGroup, Aviator)
├── mc-exception     # 异常处理 (BusinessExceptionRecord)
├── mc-notification  # 通知模块 (Template, Record, Mail)
├── mc-scheduler     # 调度模块 (预留)
└── mc-api           # API聚合模块 (聚合所有模块接口)
```

---

## M1 - 系统管理 ✅ 已完成

### 任务清单 (24/24 完成)

| 任务ID | 描述 | 状态 | 备注 |
|--------|------|------|------|
| M1-001 | 项目脚手架搭建 | ✅ | 多模块Maven结构 + parent pom |
| M1-002 | 公共模块封装 | ✅ | Result, 统一异常, 工具类, 通用注解 |
| M1-003 | 系统管理模块 | ✅ | SysOrg, SysUser, SysRole, SysMenu, SysPeriod |
| M1-004 | 数据库Schema设计 | ✅ | PostgreSQL 17张表 |
| M1-005 | 组织管理CRUD | ✅ | POST/GET/PUT/DELETE /system/org |
| M1-006 | 组织树查询 | ✅ | GET /system/org/tree |
| M1-007 | 子节点查询 | ✅ | GET /system/org/children/{parentId} |
| M1-008 | 用户管理CRUD | ✅ | POST/GET/PUT/DELETE /system/user |
| M1-009 | 用户登录 | ✅ | POST /system/auth/login |
| M1-010 | 用户登出 | ✅ | POST /system/auth/logout |
| M1-011 | 当前用户查询 | ✅ | GET /system/auth/current |
| M1-012 | 角色管理CRUD | ✅ | POST/GET/PUT/DELETE /system/role |
| M1-013 | 菜单管理CRUD | ✅ | POST/GET/PUT/DELETE /system/menu |
| M1-014 | 菜单树查询 | ✅ | GET /system/menu/tree |
| M1-015 | 用户菜单查询 | ✅ | GET /system/menu/user/{userId} |
| M1-016 | 期间管理CRUD | ✅ | POST/GET/PUT/DELETE /system/period |
| M1-017 | 期间切换 | ✅ | PUT /system/period/switch |
| M1-018 | 密码加密 | ✅ | BCryptPasswordEncoder |
| M1-019 | JWT Token | ✅ | 登录令牌生成与验证 |
| M1-020 | 审计注解 | ✅ | @Audit 记录操作日志 |
| M1-021 | 聚合查询优化 | ✅ | MyBatis-Plus Wrapper链式调用 |
| M1-022 | 数据权限 | ✅ | 组织级别数据隔离 |
| M1-023 | M1阶段测试 | ✅ | 接口验证通过 |
| M1-024 | M1阶段验收 | ✅ | 功能验收通过 |

### 产出物

- `mc-common`: 公共模块
  - `result/`: Result, ResultBuilder 统一响应封装
  - `exception/`: GlobalExceptionHandler, BusinessException
  - `enums/`: ErrorCode, StatusEnum
  - `annotation/`: @Audit 审计注解
  - `utils/`: JacksonUtils, DateUtils

- `mc-system`: 系统管理模块
  - `entity/`: SysOrg, SysUser, SysRole, SysMenu, SysPeriod + BaseEntity
  - `mapper/`: 对应MyBatis-Plus Mapper接口
  - `service/`: 完整业务逻辑 + 树形结构处理
  - `controller/`: REST API (含JWT认证)

- `mc-api`: API聚合模块
  - `config/`: SwaggerConfig, WebMvcConfig, MybatisPlusConfig

### 核心功能验证

```
✅ 登录 -> POST /system/auth/login (返回JWT token)
✅ 组织树 -> GET /system/org/tree (返回完整树形结构)
✅ 用户CRUD -> POST/GET/PUT/DELETE /system/user
✅ 角色分配 -> POST /system/user/role (分配用户角色)
✅ 菜单树 -> GET /system/menu/tree
✅ 期间查询 -> GET /system/period/list (当前期间标记is_current=1)
```

---

## M2 - 流程引擎 ✅ 已完成

### 任务清单 (21/21 完成)

| 任务ID | 描述 | 状态 | 备注 |
|--------|------|------|------|
| M2-001 | 流程模板管理 | ✅ | WfTemplate entity/mapper/service/controller |
| M2-002 | 流程实例管理 | ✅ | WfInstance entity/mapper/service |
| M2-003 | 版本管理 | ✅ | 发布时version递增，refCount引用计数 |
| M2-004 | 节点配置JSON | ✅ | NodeConfig, TemplateNodes DTO |
| M2-005 | 模板校验 | ✅ | 节点配置合法性校验 (startNodes/endNodes) |
| M2-006 | 任务实例管理 | ✅ | WfTask entity/mapper/service |
| M2-007 | 任务轨迹记录 | ✅ | WfTaskTrace entity (action/operator/time/comment) |
| M2-008 | SLA计算 | ✅ | SLA截止时间 = 开始时间 + 时长(小时) |
| M2-009 | 任务派发 | ✅ | 拓扑排序 + 依赖解析 |
| M2-010 | 创建模板 | ✅ | POST /workflow/template |
| M2-011 | 发布模板 | ✅ | POST /workflow/template/publish/{id} |
| M2-012 | 实例化流程 | ✅ | POST /workflow/instance/instantiate |
| M2-013 | 查询实例 | ✅ | GET /workflow/instance/{id} |
| M2-014 | 任务API | ✅ | 待办/进行中/已完成/轨迹 |
| M2-015 | 复核通过 | ✅ | PUT /workflow/task/approve/{id} |
| M2-016 | 复核退回 | ✅ | PUT /workflow/task/reject/{id} |
| M2-017 | 轨迹查询 | ✅ | GET /workflow/task/trace/{taskId} |
| M2-018 | SLA逾期查询 | ✅ | GET /workflow/task/overdue |
| M2-019 | 待办查询 | ✅ | GET /workflow/task/pending/{assigneeId} |
| M2-020 | M2阶段测试 | ✅ | 接口验证通过 |
| M2-021 | M2阶段验收 | ✅ | 功能验收通过 |

### 产出物

- `mc-workflow`: 流程引擎模块
  - `entity/`: WfTemplate, WfInstance, WfTask, WfTaskTrace, WorkflowBaseEntity
  - `dto/`: NodeConfig (nodeCode/name/type/assignee/slaHours/dependencies), TemplateNodes
  - `mapper/`: WfTemplateMapper, WfInstanceMapper, WfTaskMapper, WfTaskTraceMapper
  - `service/`: WfTemplateServiceImpl, WfInstanceServiceImpl, WfTaskServiceImpl
  - `controller/`: WorkflowController

### 核心功能验证

```
✅ 创建模板 -> POST /workflow/template (nodes: [startNode] -> [task1] -> [endNode])
✅ 发布模板 -> POST /workflow/template/publish/{id} (version: 1->2, status: DRAFT->PUBLISHED)
✅ 实例化流程 -> POST /workflow/instance/instantiate (templateId+periodId+unitId)
✅ 任务派发 -> 自动创建依赖任务，拓扑排序确定执行顺序
✅ 开始任务 -> PUT /workflow/task/start/{id}
✅ 复核通过 -> PUT /workflow/task/approve/{id} (记录轨迹，触发下游)
✅ 复核退回 -> PUT /workflow/task/reject/{id}
✅ 轨迹查询 -> GET /workflow/task/trace/{taskId}
✅ SLA逾期 -> GET /workflow/task/overdue
✅ 待办查询 -> GET /workflow/task/pending/{assigneeId}
```

---

## M3 - 数据导入 ✅ 已完成

### 任务清单 (12/12 完成)

| 任务ID | 描述 | 状态 | 备注 |
|--------|------|------|------|
| M3-001 | 导入批次管理 | ✅ | ImportBatch entity/mapper/service |
| M3-002 | 导入记录管理 | ✅ | ImportRecord entity/mapper |
| M3-003 | 导入模板管理 | ✅ | ImportTemplate entity (header_mappings JSON) |
| M3-004 | Excel解析 | ✅ | Apache POI (WorkbookFactory + Sheet遍历) |
| M3-005 | CSV解析 | ✅ | OpenCSV (CSVReaderBuilder) |
| M3-006 | 文件上传 | ✅ | MultipartFile + 本地存储 |
| M3-007 | 批次创建 | ✅ | POST /data/import/upload |
| M3-008 | 导入执行 | ✅ | POST /data/import/execute/{batchId} |
| M3-009 | 进度查询 | ✅ | GET /data/import/progress/{batchId} |
| M3-010 | 数据校验 | ✅ | POST /data/import/validate/{batchId} |
| M3-011 | 失败记录查询 | ✅ | GET /data/import/failures/{batchId} |
| M3-012 | M3阶段验收 | ✅ | 功能验收通过 |

### 产出物

- `mc-data`: 数据导入模块
  - `entity/`: ImportBatch, ImportRecord, ImportTemplate, DataBaseEntity
  - `dto/`: HeaderMapping (colIndex/fieldName/required/validator), ImportProgress
  - `mapper/`: ImportBatchMapper, ImportRecordMapper, ImportTemplateMapper
  - `service/`: ImportServiceImpl (POI + OpenCSV双解析器)
  - `controller/`: ImportController

### 核心功能验证

```
✅ 上传文件 -> POST /data/import/upload (返回batchId)
✅ 执行导入 -> POST /data/import/execute/{batchId}
✅ 进度查询 -> GET /data/import/progress/{batchId} (total/success/fail)
✅ 记录查询 -> GET /data/import/records/{batchId}
✅ 失败记录 -> GET /data/import/failures/{batchId}
✅ 数据校验 -> POST /data/import/validate/{batchId}
✅ 取消导入 -> POST /data/import/cancel/{batchId}
```

---

## M4 - 规则引擎 ✅ 已完成

### 任务清单 (10/10 完成)

| 任务ID | 描述 | 状态 | 备注 |
|--------|------|------|------|
| M4-001 | 规则配置管理 | ✅ | RuleConfig entity/mapper/service |
| M4-002 | 规则组管理 | ✅ | RuleGroup entity (rule_ids JSON数组) |
| M4-003 | 规则执行日志 | ✅ | RuleExecuteLog entity (input/output/error/duration) |
| M4-004 | Aviator集成 | ✅ | AviatorEvaluatorInstance 编译执行表达式 |
| M4-005 | 单规则执行 | ✅ | POST /rule/execute |
| M4-006 | 批量规则执行 | ✅ | POST /rule/execute/batch |
| M4-007 | 规则组执行 | ✅ | POST /rule/execute/group/{groupCode} |
| M4-008 | 规则发布 | ✅ | POST /rule/publish/{id} (status: DRAFT->PUBLISHED) |
| M4-009 | 表达式校验 | ✅ | POST /rule/validate (编译测试表达式) |
| M4-010 | M4阶段验收 | ✅ | 功能验收通过 |

### 产出物

- `mc-rule`: 规则引擎模块
  - `entity/`: RuleConfig, RuleGroup, RuleExecuteLog, RuleBaseEntity
  - `dto/`: RuleExecuteRequest, RuleExecuteResult
  - `mapper/`: RuleConfigMapper, RuleGroupMapper, RuleExecuteLogMapper
  - `service/`: RuleEngineServiceImpl (Aviator集成，支持变量注入)
  - `controller/`: RuleController

### 核心功能验证

```
✅ 创建规则 -> POST /rule (expression: "amount > 0")
✅ 发布规则 -> POST /rule/publish/{id}
✅ 执行规则 -> POST /rule/execute (amount=100 -> result: true)
✅ 执行规则 -> POST /rule/execute (amount=-50 -> result: false)
✅ 规则列表 -> GET /rule/list
✅ 表达式校验 -> POST /rule/validate (无效表达式返回编译错误)
```

---

## M5 - 异常处理 ✅ 已完成

### 任务清单 (10/10 完成)

| 任务ID | 描述 | 状态 | 备注 |
|--------|------|------|------|
| M5-001 | 异常记录管理 | ✅ | BusinessExceptionRecord entity/mapper/service |
| M5-002 | 异常记录API | ✅ | POST /exception (记录新异常) |
| M5-003 | 待处理查询 | ✅ | GET /exception/pending |
| M5-004 | 异常详情查询 | ✅ | GET /exception/{id} |
| M5-005 | 异常处理 | ✅ | POST /exception/handle/{id} |
| M5-006 | 异常忽略 | ✅ | POST /exception/ignore/{id} |
| M5-007 | 异常统计 | ✅ | GET /exception/statistics |
| M5-008 | 批量处理 | ✅ | POST /exception/batch/handle |
| M5-009 | 异常表创建 | ✅ | business_exception 表 |
| M5-010 | M5阶段验收 | ✅ | 功能验收通过 |

### 产出物

- `mc-exception`: 异常处理模块
  - `entity/`: BusinessExceptionRecord, ExceptionBaseEntity
  - `mapper/`: BusinessExceptionRecordMapper
  - `service/`: ExceptionServiceImpl
  - `controller/`: ExceptionController

### 核心功能验证

```
✅ 记录异常 -> POST /exception (返回记录ID)
✅ 待处理查询 -> GET /exception/pending?periodId=&unitId=
✅ 异常详情 -> GET /exception/{id}
✅ 处理异常 -> POST /exception/handle/{id}?handlerId=&handlerName=&comment=
✅ 忽略异常 -> POST /exception/ignore/{id}?reason=
✅ 异常统计 -> GET /exception/statistics?periodId=&unitId= (TOTAL/PENDING/RESOLVED/IGNORED)
✅ 批量处理 -> POST /exception/batch/handle
```

---

## M6 - 通知模块 ✅ 已完成

### 任务清单 (8/8 完成)

| 任务ID | 描述 | 状态 | 备注 |
|--------|------|------|------|
| M6-001 | 通知模板管理 | ✅ | NotificationTemplate entity/mapper/service |
| M6-002 | 通知记录管理 | ✅ | NotificationRecord entity/mapper |
| M6-003 | 邮件发送 | ✅ | Spring Mail (JavaMailSender) |
| M6-004 | WebSocket通知 | ✅ | 日志记录模式 (预留WebSocket扩展) |
| M6-005 | 发送API | ✅ | POST /notification/send |
| M6-006 | 模板管理API | ✅ | POST/GET/PUT/DELETE /notification/template |
| M6-007 | 重试机制 | ✅ | POST /notification/retry/{id} |
| M6-008 | M6阶段验收 | ✅ | 功能验收通过 |

### 产出物

- `mc-notification`: 通知模块
  - `entity/`: NotificationTemplate, NotificationRecord, NotificationBaseEntity
  - `dto/`: NotificationSendRequest
  - `mapper/`: NotificationTemplateMapper, NotificationRecordMapper
  - `service/`: NotificationServiceImpl (Mail + WebSocket双通道)
  - `controller/`: NotificationController

### 核心功能验证

```
✅ 发送通知 -> POST /notification/send (channel: EMAIL/WEB_SOCKET)
✅ 通知记录 -> GET /notification/record/list?status=&channel=
✅ 通知统计 -> GET /notification/statistics (TOTAL/SENT/FAILED/PENDING)
✅ 创建模板 -> POST /notification/template
✅ 查询模板 -> GET /notification/template/{code}
✅ 重试发送 -> POST /notification/retry/{id}
```

---

## M7 - 测试验收 ✅ 已完成

### 任务清单 (8/8 完成)

| 任务ID | 描述 | 状态 | 备注 |
|--------|------|------|------|
| M7-001 | M1系统管理测试 | ✅ | 组织树/用户/角色/菜单/期间 |
| M7-002 | M2流程引擎测试 | ✅ | 模板发布/实例化/任务/轨迹 |
| M7-003 | M3数据导入测试 | ✅ | 上传/解析/批次/记录/校验 |
| M7-004 | M4规则引擎测试 | ✅ | Aviator表达式执行 (true/false) |
| M7-005 | M5异常处理测试 | ✅ | 记录/处理/忽略/统计/批量 |
| M7-006 | M6通知模块测试 | ✅ | 发送/模板/记录/统计/重试 |
| M7-007 | 集成测试 | ✅ | 全流程API串联验证 |
| M7-008 | 文档同步 | ✅ | PROGRESS.md全面更新 |

### 验收结果

| 模块 | 测试接口 | 结果 |
|------|----------|------|
| M1 系统管理 | GET /system/org/tree | ✅ PASS |
| M2 流程引擎 | POST /workflow/template/publish/{id} | ✅ PASS |
| M3 数据导入 | POST /data/import/upload + /execute | ✅ PASS |
| M4 规则引擎 | POST /rule/execute (amount>0 → true, amount=-50 → false) | ✅ PASS |
| M5 异常处理 | POST /exception + GET /exception/pending + POST /exception/handle | ✅ PASS |
| M6 通知模块 | POST /notification/send + GET /notification/statistics | ✅ PASS |

---

## 数据库表清单

| 序号 | 表名 | 所属模块 | 说明 |
|------|------|----------|------|
| 1 | sys_org | M1 | 组织树表 |
| 2 | sys_user | M1 | 用户表 |
| 3 | sys_role | M1 | 角色表 |
| 4 | sys_menu | M1 | 菜单表 |
| 5 | sys_user_role | M1 | 用户角色关系 |
| 6 | sys_role_menu | M1 | 角色菜单关系 |
| 7 | sys_period | M1 | 会计期间表 |
| 8 | wf_template | M2 | 流程模板表 |
| 9 | wf_instance | M2 | 流程实例表 |
| 10 | wf_task | M2 | 任务实例表 |
| 11 | wf_task_trace | M2 | 任务轨迹表 |
| 12 | import_batch | M3 | 导入批次表 |
| 13 | import_record | M3 | 导入记录表 |
| 14 | import_template | M3 | 导入模板表 |
| 15 | rule_config | M4 | 规则配置表 |
| 16 | rule_group | M4 | 规则组表 |
| 17 | rule_execute_log | M4 | 规则执行日志表 |
| 18 | business_exception | M5 | 业务异常记录表 |
| 19 | notification_template | M6 | 通知模板表 |
| 20 | notification_record | M6 | 通知记录表 |

---

## API清单

| 模块 | 接口路径 | 方法 | 说明 |
|------|----------|------|------|
| system | /system/auth/login | POST | 用户登录 |
| system | /system/auth/logout | POST | 用户登出 |
| system | /system/auth/current | GET | 当前用户信息 |
| system | /system/org | POST/GET/PUT/DELETE | 组织CRUD |
| system | /system/org/tree | GET | 组织树 |
| system | /system/org/children/{parentId} | GET | 子节点查询 |
| system | /system/user | POST/GET/PUT/DELETE | 用户CRUD |
| system | /system/role | POST/GET/PUT/DELETE | 角色CRUD |
| system | /system/menu | POST/GET/PUT/DELETE | 菜单CRUD |
| system | /system/menu/tree | GET | 菜单树 |
| system | /system/menu/user/{userId} | GET | 用户菜单 |
| system | /system/period | POST/GET/PUT/DELETE | 期间CRUD |
| workflow | /workflow/template | POST/GET | 模板CRUD |
| workflow | /workflow/template/publish/{id} | POST | 发布模板 |
| workflow | /workflow/instance | POST/GET | 实例CRUD |
| workflow | /workflow/instance/instantiate | POST | 实例化流程 |
| workflow | /workflow/task | GET | 任务查询 |
| workflow | /workflow/task/start/{id} | PUT | 开始任务 |
| workflow | /workflow/task/approve/{id} | PUT | 复核通过 |
| workflow | /workflow/task/reject/{id} | PUT | 复核退回 |
| workflow | /workflow/task/trace/{taskId} | GET | 任务轨迹 |
| workflow | /workflow/task/pending/{assigneeId} | GET | 待办查询 |
| workflow | /workflow/task/overdue | GET | SLA逾期查询 |
| data | /data/import/upload | POST | 上传文件 |
| data | /data/import/execute/{batchId} | POST | 执行导入 |
| data | /data/import/progress/{batchId} | GET | 进度查询 |
| data | /data/import/records/{batchId} | GET | 记录查询 |
| data | /data/import/failures/{batchId} | GET | 失败记录 |
| data | /data/import/validate/{batchId} | POST | 数据校验 |
| data | /data/import/cancel/{batchId} | POST | 取消导入 |
| rule | /rule | POST/GET/PUT/DELETE | 规则CRUD |
| rule | /rule/list | GET | 规则列表 |
| rule | /rule/publish/{id} | POST | 发布规则 |
| rule | /rule/execute | POST | 执行规则 |
| rule | /rule/execute/batch | POST | 批量执行 |
| rule | /rule/execute/group/{groupCode} | POST | 规则组执行 |
| rule | /rule/validate | POST | 表达式校验 |
| exception | /exception | POST | 记录异常 |
| exception | /exception/pending | GET | 待处理查询 |
| exception | /exception/{id} | GET | 异常详情 |
| exception | /exception/handle/{id} | POST | 处理异常 |
| exception | /exception/ignore/{id} | POST | 忽略异常 |
| exception | /exception/statistics | GET | 异常统计 |
| exception | /exception/batch/handle | POST | 批量处理 |
| notification | /notification/send | POST | 发送通知 |
| notification | /notification/template | POST/GET | 模板管理 |
| notification | /notification/template/{code} | GET/DELETE | 模板查询/删除 |
| notification | /notification/template/list | GET | 模板列表 |
| notification | /notification/record/list | GET | 通知记录 |
| notification | /notification/retry/{id} | POST | 重试发送 |
| notification | /notification/statistics | GET | 通知统计 |

---

## 技术栈

| 组件 | 版本 | 用途 |
|------|------|------|
| JDK | 17 | Java运行时 |
| Spring Boot | 3.2.0 | 框架 |
| Maven | 3.9+ | 项目构建 |
| MyBatis-Plus | 3.5.5 | ORM框架 |
| PostgreSQL | 15 | 关系数据库 |
| Docker | 25+ | 容器化 |
| AviatorScript | 5.3.3 | 规则表达式引擎 |
| Apache POI | 5.2.5 | Excel解析 |
| OpenCSV | 5.9 | CSV解析 |
| Lombok | 1.18.36 | 简化代码 |
| JJWT | 0.12.3 | JWT令牌 |
| SpringDoc OpenAPI | 2.3.0 | API文档 |
| Hutool | 5.8.23 | 工具集 |

---

## 构建与启动

### 前置条件

- JDK 17+
- Maven 3.9+
- PostgreSQL 15+ (Docker方式)
- Docker (可选，用于数据库)

### 数据库初始化

```bash
# 启动PostgreSQL
docker run -d --name mc-postgres \
  -e POSTGRES_DB=monthly_close \
  -e POSTGRES_USER=mcuser \
  -e POSTGRES_PASSWORD=mcpassword \
  -p 5432:5432 \
  postgres:15

# 执行初始化脚本
docker exec mc-postgres psql -U mcuser -d monthly_close -f /path/to/init_schema.sql
```

### 构建项目

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17

mvn clean install -DskipTests
```

### 启动服务

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17

java -jar mc-api/target/mc-api-1.0.0-SNAPSHOT.jar --server.port=8080
```

或使用Maven插件启动：

```bash
cd mc-api
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
mvn spring-boot:run
```

---

## 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| API根地址 | http://localhost:8080 | Spring Boot API |
| Swagger UI | http://localhost:8080/swagger-ui.html | API文档页面 |
| API Docs | http://localhost:8080/v3/api-docs | OpenAPI JSON |
| Health Check | http://localhost:8080/actuator/health | 健康检查 |

---

## 数据库连接

| 配置项 | 值 |
|--------|-----|
| Host | localhost:5432 |
| Database | monthly_close |
| Username | mcuser |
| Password | mcpassword |
| Driver | org.postgresql.Driver |
| URL | jdbc:postgresql://localhost:5432/monthly_close |

---

## 已知问题与限制

1. **通知模块**: WebSocket通知为日志记录模式，生产环境需接入真实WebSocket通道
2. **邮件发送**: 需要配置邮件SMTP服务器，当前为跳过模式
3. **调度模块**: mc-scheduler为空模块预留，任务调度功能待开发
4. **数据权限**: 当前实现为组织级别隔离，字段级权限待完善
5. **JWT Secret**: 生产环境需使用独立配置的密钥（已在JwtUtil中实现但建议外置）

---

## 安全修复记录 (v1.1.0)

### P0 立即修复 ✅ 已完成

| # | 问题 | 修复方案 | 验证结果 |
|---|------|----------|----------|
| P0-1 | 文件上传未保存内容 | 使用`file.transferTo()`保存到`/tmp/UUID_filename` | ✅ 文件正确落地 |
| P0-2 | Aviator表达式无缓存 | `ConcurrentHashMap`缓存编译结果，变更时清除 | ✅ 编译结果复用 |
| P0-3 | 批量导入逐条插入 | 分批`saveBatch()`，BATCH_SIZE=1000 | ✅ 批量INSERT |
| P0-4 | CSV注入防护 | `sanitizeField()`对`=+-@`前缀加单引号 | ✅ 公式注入防护 |

### P1 紧急修复 ✅ 已完成

| # | 问题 | 修复方案 | 验证结果 |
|---|------|----------|----------|
| P1-1 | 所有接口无认证 | JwtAuthFilter全局过滤器，`/login`等路径放行 | ✅ 无token→401 |
| P1-2 | 硬编码JWT密钥 | JwtUtil从`System.getProperty("jwt.secret")`读取，支持环境变量`JWT_SECRET`覆盖 | ✅ 已支持外置 |
| P1-3 | 任务审批无归属验证 | `verifyAssignee()`校验当前用户==assigneeId | ✅ 已校验 |
| P1-4 | 路径穿越漏洞 | `FilenameUtils.getName()`过滤`..`，UUID文件名 | ✅ 路径清洁 |
| P1-5 | 密码重置无验证 | `resetPassword`接口增加`oldPassword`参数校验 | ✅ 需原密码 |
| P1-6 | 错误信息泄露 | GlobalExceptionHandler通用异常返回固定文案 | ✅ 不泄露堆栈 |
| P1-7 | CSV Injection | sanitizeField前缀处理 | ✅ 已防护 |
| P1-8 | Aviator沙箱 | `ALLOWED_CLASS_SET`空集合=拒绝所有外部类访问 | ✅ 已加固 |
| P1-9 | 规则组N+1查询 | `Map<Long,RuleConfig>`一次加载所有规则 | ✅ 内存比对 |

### P2 短期内修复 ✅ 已完成

| # | 问题 | 修复方案 | 验证结果 |
|---|------|----------|----------|
| P2-1 | 审计日志未持久化 | AuditAspect切面已记录，TODO持久化服务 | ⚠️ 待P3实现 |
| P2-2 | 异常统计内存爆炸 | `SQL COUNT`替代全量加载 | ✅ SQL统计 |
| P2-3 | 通知统计内存爆炸 | `SQL COUNT`替代全量加载 | ✅ SQL统计 |
| P2-4 | 批量处理N+1 | `listByIds`+`updateBatchById`替代循环 | ✅ 批量更新 |
| P2-5 | flushBatch批量插入 | `insertBatchForMySQL`原生批量SQL+降级逐条处理 | ✅ 批量INSERT |
| P2-6 | validateData批量更新 | 分批`updateById`减少网络往返 | ✅ 分批更新 |

### P3 代码质量改进 ✅ 已完成

| # | 问题 | 修复方案 | 验证结果 |
|---|------|----------|----------|
| P3-1 | 魔法字符串枚举化 | 新建`BatchStatus`/`RecordStatus`枚举，覆盖ImportServiceImpl中所有魔法字符串 | ✅ 已替换 |
| P3-2 | ObjectMapper统一注入 | `RuleEngineServiceImpl`改用`@Autowired ObjectMapper` | ✅ 已完成 |
| P3-3 | buildTree公共提取 | 新建`TreeUtil`工具类，提供通用树形构建方法 | ✅ 已创建工具类 |
| P3-4 | 审计日志持久化 | 新建`AuditLog`实体+`AuditLogMapper`+`IAuditLogService`，`AuditAspect`取消TODO改用真实服务 | ✅ 已实现 |
| P3-5 | 拓扑排序环检测 | 待后续流程模块增强 | 🔲 待处理 |

### 验证测试摘要

```bash
# JWT认证
curl localhost:18080/system/auth/current
# → 401 缺少认证Token

# 异常统计（SQL COUNT）
curl localhost:18080/exception/statistics -H "Authorization: Bearer $TOKEN"
# → {"TOTAL":1,"RESOLVED":1,"PENDING":0,"IGNORED":0}

# 密码重置（需原密码）
curl -X POST "localhost:18080/system/user/resetPassword/1?oldPassword=admin123&newPassword=admin123" -H "Authorization: Bearer $TOKEN"
# → {"code":0,"data":true}
```

---

## 下一步规划

- [x] P0 安全漏洞修复（文件上传/Aviator缓存/批量N+1）
- [x] P1 安全漏洞修复（JWT认证/任务归属/路径穿越/错误泄露/JWT密钥外置/Aviator沙箱）
- [x] P2 性能与逻辑修复（SQL统计/批量更新/flushBatch/validateData批量化）
- [x] P3 代码质量改进（枚举化/buildTree公共方法/审计日志持久化）
- [ ] 拓扑排序环检测（流程模块增强）
- [ ] mc-scheduler 调度模块开发
- [ ] WebSocket真实通道接入
- [ ] 邮件SMTP生产配置
- [ ] 前端页面开发
- [ ] 性能测试与优化
- [ ] 监控与告警接入

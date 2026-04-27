# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

月结报表管理系统 - 基于 Spring Boot 3.2.0 + MyBatis-Plus + PostgreSQL + Vue 3 的多模块 Maven 项目，用于月结报表流程管理。

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.9+
- Node.js 18+
- PostgreSQL 15+ (通过 docker-compose 启动)
- JAVA_HOME 必须指向 OpenJDK 17

### 启动后端

```bash
# 启动数据库
cd /Users/lisongjian/Project/projectX/monthly-close-system
docker-compose up -d

# 初始化数据库 (首次)
docker exec mc-postgres psql -U mcuser -d monthly_close -f /path/to/sql/init_schema.sql

# 构建
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
mvn clean package -DskipTests

# 启动 (端口 18080)
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
java -jar mc-api/target/mc-api-1.0.0-SNAPSHOT.jar --server.port=18080
```

### 启动前端

```bash
cd mc-frontend
npm install
npm run dev   # 访问 http://localhost:3000
```

默认登录: `admin` / `admin123`

## 模块架构

```
monthly-close-parent (pom.xml - 父模块)
├── mc-common        # 公共模块: Result, Exception, Enums, Utils, Annotations, Audit, Aspect
├── mc-system        # 系统管理: Org, User, Role, Menu, Period
├── mc-workflow      # 流程引擎: Template, Instance, Task, Trace
├── mc-data          # 数据导入: Batch, Record (Apache POI + OpenCSV)
├── mc-rule          # 规则引擎: RuleConfig, Aviator 表达式执行
├── mc-exception     # 异常处理: BusinessExceptionRecord
├── mc-notification  # 通知模块: Template, Record, Email
├── mc-scheduler     # 调度模块: @Scheduled SLA 逾期检测
└── mc-api           # API 聚合模块: 启动类, JwtAuthFilter, SwaggerConfig
```

### 前端架构 (Vue 3)

```
mc-frontend/src/
├── api/          # axios API 封装 (system.js, workflow.js, data.js)
├── stores/       # Pinia 状态管理 (auth.js)
├── router/       # Vue Router 路由配置
├── utils/        # 工具: request.js (axios 封装, baseURL 指向 localhost:18080)
└── views/        # Vue 页面组件
    ├── system/   # 组织/用户/角色/期间管理
    ├── workflow/ # 模板/实例/任务管理
    ├── data/     # 数据导入
    ├── rule/     # 规则配置
    ├── exception/# 异常处理
    └── notification/ # 通知管理
```

## 常用命令

```bash
# 编译整个项目
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
mvn clean package -DskipTests

# 只构建 API 模块 (依赖模块会自动构建)
mvn clean package -pl mc-api -am -DskipTests

# 运行单模块
mvn spring-boot:run -pl mc-api

# 前端开发
cd mc-frontend && npm run dev

# 前端构建生产版本
npm run build
```

## 核心架构设计

### JWT 认证 (JwtAuthFilter)
- 位置: `mc-api/src/main/java/com/mc/api/config/JwtAuthFilter.java`
- 所有请求经过此过滤器，放行路径: `/system/auth/login`, `/swagger-ui`, `/v3/api-docs`, `/actuator`
- CORS 头在此过滤器中直接设置 (Access-Control-Allow-Origin: *)
- Token 通过 `Authorization: Bearer <token>` 头传递
- 用户信息注入 request attribute: `userId`, `username`

### 审计日志 (AuditAspect)
- 位置: `mc-common/src/main/java/com/mc/common/aspect/AuditAspect.java`
- 基于 `@Audit` 注解，记录 action/bizId/operatorId 等
- 持久化到 `audit_log` 表 via `AuditLogService`

### 规则引擎 (RuleEngineServiceImpl)
- 位置: `mc-rule/src/main/java/com/mc/rule/service/RuleEngineServiceImpl.java`
- 使用 Aviator 5.3.3 执行表达式
- Aviator 配置: `ALLOWED_CLASS_SET` 空集合 = 拒绝所有外部类访问
- 表达式编译结果使用 `ConcurrentHashMap` 缓存

### 流程引擎 (WfTemplateServiceImpl)
- 位置: `mc-workflow/src/main/java/com/mc/workflow/service/WfTemplateServiceImpl.java`
- 节点拓扑排序派发任务，JSON 配置存储在 `node_config` 字段
- 任务状态机: PENDING → IN_PROGRESS → COMPLETED/REJECTED/OVERDUE
- SLA 计算: 开始时间 + SLA小时数

### 数据导入 (ImportServiceImpl)
- 位置: `mc-data/src/main/java/com/mc/data/service/ImportServiceImpl.java`
- Apache POI 解析 Excel (.xlsx/.xls)，OpenCSV 解析 CSV
- 批量插入: `insertBatchForMySQL` 原生批量 SQL，降级逐条处理
- CSV 注入防护: sanitizeField 对 `=+-@` 前缀加单引号

## API 设计约定

- 统一响应格式: `Result<T>` (code: 0=成功, message, data, timestamp, requestId)
- 分页响应: `PageResult<T>` (records, total, page, size)
- 所有删除操作使用 HTTP DELETE
- 文件上传使用 multipart/form-data

## 关键文件位置

| 功能 | 文件路径 |
|------|----------|
| 启动类 | `mc-api/src/main/java/com/mc/api/McApiApplication.java` |
| JWT 密钥 | `mc-common/src/main/java/com/mc/common/util/JwtUtil.java` (支持 -Djwt.secret 或 JWT_SECRET env) |
| 全局异常 | `mc-common/src/main/java/com/mc/common/exception/GlobalExceptionHandler.java` |
| 数据库配置 | `mc-api/src/main/resources/application.yml` |
| 前端 API 封装 | `mc-frontend/src/utils/request.js` |
| 前端路由 | `mc-frontend/src/router/index.js` |
| 前端 Pinia Store | `mc-frontend/src/stores/auth.js` |

## 开发注意事项

1. **JWT 密钥**: 默认使用硬编码密钥，生产环境必须通过 `-Djwt.secret=xxx` 或 `JWT_SECRET` 环境变量覆盖

2. **数据库迁移**: 首次启动需执行 `sql/init_schema.sql`，新增表需同步更新

3. **前端 API 代理**: Vite dev server 直接调用 `http://localhost:18080` (绕过代理)，生产构建使用 `/api` 前缀通过后端路由

4. **CORS 配置**: CORS 头在 `JwtAuthFilter` 中设置，OPTIONS 请求直接返回 200 不走后续过滤链

5. **mc-scheduler**: 使用 `@Scheduled(cron = "0 0/5 * * * ?")` 每 5 分钟扫描 SLA 逾期任务

6. **枚举类**: 所有状态字段使用枚举 (BatchStatus, RecordStatus, TaskStatus, PeriodStatus 等)，禁止魔法字符串

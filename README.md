# Monthly Close System / 月结报表系统

**月度结算报表管理系统** — 基于 Spring Boot 3.2.0 + MyBatis-Plus + PostgreSQL + Vue 3 的多模块 Maven 项目，用于月结报表流程管理。

## 技术栈

**后端**
- Java 17 + Spring Boot 3.2.0
- MyBatis-Plus
- PostgreSQL 15+
- JWT 认证
- Aviator 规则引擎
- Apache POI + OpenCSV (数据导入)

**前端**
- Vue 3 + Composition API
- Element Plus UI
- Pinia 状态管理
- Vite
- SCSS + CSS Variables (三套主题)

## 模块架构

```
monthly-close-parent/
├── mc-common        # 公共模块: Result, Exception, Enums, Utils
├── mc-system        # 系统管理: Org, User, Role, Menu, Period
├── mc-workflow      # 流程引擎: Template, Instance, Task, Trace
├── mc-data          # 数据导入: Batch, Record
├── mc-rule          # 规则引擎: RuleConfig, Aviator 表达式
├── mc-exception     # 异常处理
├── mc-notification  # 通知模块: Template, Record, Email
├── mc-scheduler     # 调度模块: SLA 逾期检测
└── mc-api           # API 聚合模块
```

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.9+
- Node.js 18+
- PostgreSQL 15+

### 启动后端

```bash
# 启动数据库
cd monthly-close-system
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

**默认登录**: `admin` / `admin123`

## 功能模块

| 模块 | 功能 |
|------|------|
| 系统管理 | 组织、用户、角色、期间管理 |
| 流程引擎 | 模板管理、实例派发、任务审批、SLA监控 |
| 数据导入 | Excel/CSV 批量导入、模板配置 |
| 规则配置 | Aviator 表达式规则、规则组管理 |
| 异常处理 | 异常记录、状态跟踪、处理记录 |
| 通知管理 | 通知模板、发送记录、Email通知 |

## 主题切换

支持三套主题一键切换：
- **White** — 白色简约风
- **Dark** — 深蓝科技风
- **Glass** — 毛玻璃苹果风

## API 设计

- 统一响应格式: `Result<T>` (code: 0=成功, message, data, timestamp)
- 分页响应: `PageResult<T>` (records, total, page, size)
- JWT 认证: `Authorization: Bearer <token>`
# 月结报表管理系统 - 操作手册

> 文档版本: 1.0.0
> 更新日期: 2026-04-24
> 适用版本: monthly-close-system 1.0.0

---

## 目录

1. [系统概述](#1-系统概述)
2. [快速开始](#2-快速开始)
3. [系统管理模块](#3-系统管理模块)
4. [流程引擎模块](#4-流程引擎模块)
5. [数据导入模块](#5-数据导入模块)
6. [规则引擎模块](#6-规则引擎模块)
7. [异常处理模块](#7-异常处理模块)
8. [通知模块](#8-通知模块)
9. [常见问题](#9-常见问题)

---

## 1. 系统概述

### 1.1 系统简介

月结报表管理系统是一套基于 Spring Boot 3.2.0 + MyBatis-Plus + PostgreSQL 的企业级月结流程管理平台，支持多组织架构、流程自动化、数据导入校验、规则引擎和异常处理。

### 1.2 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        客户端层                              │
│   Swagger UI  │  Postman/curl  │  前端应用                   │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP/REST
┌──────────────────────────▼──────────────────────────────────┐
│                        API层 (mc-api)                       │
│              Swagger文档 / 统一响应 / 审计日志                 │
└──────┬──────────┬──────────┬──────────┬──────────┬─────────┘
       │          │          │          │          │
┌──────▼──┐ ┌────▼───┐ ┌────▼────┐ ┌───▼─────┐ ┌▼────────┐
│ mc-system│ │mc-workflow│ │ mc-data │ │ mc-rule │ │mc-exception│
│ 系统管理 │ │ 流程引擎 │ │ 数据导入 │ │规则引擎 │ │ 异常处理 │
└─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘
                                                            │
                                                    ┌───────▼───────┐
                                                    │ mc-notification│
                                                    │   通知模块     │
                                                    └───────────────┘
```

### 1.3 功能模块

| 模块 | 功能说明 |
|------|----------|
| 系统管理 | 组织架构、用户、角色、菜单、会计期间管理 |
| 流程引擎 | 流程模板设计、实例化、任务派发、轨迹追踪 |
| 数据导入 | Excel/CSV 文件解析、批次管理、数据校验 |
| 规则引擎 | AviatorScript 表达式执行、规则组管理 |
| 异常处理 | 业务异常记录、处理、统计 |
| 通知模块 | 邮件/WebSocket 通知、模板管理 |

---

## 2. 快速开始

### 2.1 环境要求

| 组件 | 版本要求 |
|------|----------|
| JDK | 17+ |
| Maven | 3.9+ |
| PostgreSQL | 15+ |
| Docker | 25+ (可选) |

### 2.2 启动服务

```bash
# 1. 初始化数据库
docker exec mc-postgres psql -U mcuser -d monthly_close -f /path/to/init_schema.sql

# 2. 构建项目
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
mvn clean install -DskipTests

# 3. 启动服务
java -jar mc-api/target/mc-api-1.0.0-SNAPSHOT.jar --server.port=8080
```

### 2.3 访问地址

| 服务 | 地址 |
|------|------|
| API 根地址 | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API 文档 | http://localhost:8080/v3/api-docs |
| 健康检查 | http://localhost:8080/actuator/health |

### 2.4 默认账户

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 系统管理员 |

---

## 3. 系统管理模块

### 3.1 组织架构管理

#### 3.1.1 查看组织树

获取完整的组织架构树形结构。

**请求示例：**

```bash
curl -X GET "http://localhost:8080/system/org/tree"
```

**响应示例：**

```json
{
  "code": 0,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "code": "HQ",
      "name": "总公司",
      "type": "UNIT",
      "path": "/HQ",
      "level": 1,
      "children": [
        {
          "id": 2,
          "code": "BJ",
          "name": "北京分公司",
          "type": "UNIT",
          "path": "/HQ/BJ",
          "level": 2,
          "children": []
        }
      ]
    }
  ]
}
```

#### 3.1.2 新增组织

**请求示例：**

```bash
curl -X POST "http://localhost:8080/system/org" \
  -H "Content-Type: application/json" \
  -d '{
    "parentId": 1,
    "code": "GZ",
    "name": "广州分公司",
    "type": "UNIT",
    "path": "/HQ/GZ",
    "level": 2,
    "sort": 3
  }'
```

#### 3.1.3 修改组织

**请求示例：**

```bash
curl -X PUT "http://localhost:8080/system/org" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 4,
    "code": "GZ",
    "name": "广州分公司（新版）",
    "sort": 4
  }'
```

#### 3.1.4 删除组织

**请求示例：**

```bash
curl -X DELETE "http://localhost:8080/system/org/4"
```

> **注意**：删除组织前，请确保该组织下没有子组织和用户。

#### 3.1.5 查询子节点

**请求示例：**

```bash
curl -X GET "http://localhost:8080/system/org/children/1"
```

---

### 3.2 用户管理

#### 3.2.1 分页查询用户

**请求示例：**

```bash
curl -X GET "http://localhost:8080/system/user/page?pageNum=1&pageSize=10&username=admin"
```

**响应示例：**

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "total": 1,
    "size": 10,
    "current": 1,
    "records": [
      {
        "id": 1,
        "username": "admin",
        "realName": "系统管理员",
        "orgId": 1,
        "status": 1
      }
    ]
  }
}
```

#### 3.2.2 新增用户

**请求示例：**

```bash
curl -X POST "http://localhost:8080/system/user" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "zhangsan",
    "password": "password123",
    "realName": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "orgId": 2
  }'
```

#### 3.2.3 修改用户

**请求示例：**

```bash
curl -X PUT "http://localhost:8080/system/user" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 2,
    "realName": "张三（已修改）",
    "phone": "13900139000"
  }'
```

#### 3.2.4 重置密码

**请求示例：**

```bash
curl -X POST "http://localhost:8080/system/user/resetPassword/2?newPassword=newPassword123"
```

#### 3.2.5 修改密码

**请求示例：**

```bash
curl -X POST "http://localhost:8080/system/user/changePassword?id=2&oldPassword=old123&newPassword=new123"
```

---

### 3.3 角色管理

#### 3.3.1 角色列表

**请求示例：**

```bash
curl -X GET "http://localhost:8080/system/role/page"
```

#### 3.3.2 新增角色

**请求示例：**

```bash
curl -X POST "http://localhost:8080/system/role" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "FINANCE",
    "name": "财务主管",
    "type": "BUSINESS",
    "sort": 3
  }'
```

#### 3.3.3 修改角色

**请求示例：**

```bash
curl -X PUT "http://localhost:8080/system/role" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 3,
    "name": "财务经理",
    "remark": "负责财务管理"
  }'
```

#### 3.3.4 删除角色

**请求示例：**

```bash
curl -X DELETE "http://localhost:8080/system/role/3"
```

---

### 3.4 菜单管理

#### 3.4.1 菜单树查询

**请求示例：**

```bash
curl -X GET "http://localhost:8080/system/menu/tree"
```

**响应示例：**

```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "code": "system",
      "name": "系统管理",
      "type": "CATALOG",
      "path": "/system",
      "children": [
        {
          "id": 2,
          "code": "org",
          "name": "组织管理",
          "type": "MENU",
          "path": "/system/org"
        },
        {
          "id": 3,
          "code": "user",
          "name": "用户管理",
          "type": "MENU",
          "path": "/system/user"
        }
      ]
    }
  ]
}
```

#### 3.4.2 用户菜单查询

**请求示例：**

```bash
curl -X GET "http://localhost:8080/system/menu/user/1"
```

#### 3.4.3 新增菜单

**请求示例：**

```bash
curl -X POST "http://localhost:8080/system/menu" \
  -H "Content-Type: application/json" \
  -d '{
    "parentId": 1,
    "code": "period",
    "name": "期间管理",
    "type": "MENU",
    "path": "/system/period",
    "sort": 4
  }'
```

---

### 3.5 会计期间管理

#### 3.5.1 期间列表

**请求示例：**

```bash
curl -X GET "http://localhost:8080/system/period/list"
```

**响应示例：**

```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "code": "2026-01",
      "name": "2026年1月",
      "startDate": "2026-01-01",
      "endDate": "2026-01-31",
      "year": 2026,
      "month": 1,
      "status": "CLOSED",
      "isCurrent": 0
    },
    {
      "id": 4,
      "code": "2026-04",
      "name": "2026年4月",
      "startDate": "2026-04-01",
      "endDate": "2026-04-30",
      "year": 2026,
      "month": 4,
      "status": "ACTIVE",
      "isCurrent": 1
    }
  ]
}
```

#### 3.5.2 新增期间

**请求示例：**

```bash
curl -X POST "http://localhost:8080/system/period" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "2026-05",
    "name": "2026年5月",
    "startDate": "2026-05-01",
    "endDate": "2026-05-31",
    "year": 2026,
    "month": 5,
    "status": "ACTIVE"
  }'
```

#### 3.5.3 切换当前期间

**请求示例：**

```bash
curl -X PUT "http://localhost:8080/system/period/switch/5"
```

> **说明**：同一时间只能有一个 `isCurrent=1` 的期间。

---

## 4. 流程引擎模块

### 4.1 流程模板管理

#### 4.1.1 创建流程模板

流程模板包含节点配置，定义流程的节点和依赖关系。

**节点配置说明：**

| 字段 | 说明 |
|------|------|
| code | 节点编码（唯一） |
| name | 节点名称 |
| type | 节点类型：ONE_TIME_TASK / PERIODIC_TASK / IMPORT_CHECK_TASK / CLOSE_CONFIRM_TASK |
| assigneeId | 责任人ID |
| assigneeName | 责任人名称 |
| slaHours | SLA时长（小时） |
| preNodeCodes | 前置节点编码（逗号分隔） |
| sort | 显示顺序 |

**请求示例：**

```bash
curl -X POST "http://localhost:8080/workflow/template" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "MONTHLY_CLOSE_V1",
    "name": "月度关账流程v1",
    "nodes": [
      {
        "code": "start",
        "name": "开始",
        "type": "ONE_TIME_TASK",
        "sort": 1,
        "preNodeCodes": ""
      },
      {
        "code": "data_import",
        "name": "数据导入",
        "type": "IMPORT_CHECK_TASK",
        "assigneeId": 1,
        "assigneeName": "张三",
        "slaHours": 24,
        "sort": 2,
        "preNodeCodes": "start"
      },
      {
        "code": "rule_check",
        "name": "规则校验",
        "type": "ONE_TIME_TASK",
        "assigneeId": 2,
        "assigneeName": "李四",
        "slaHours": 8,
        "sort": 3,
        "preNodeCodes": "data_import"
      },
      {
        "code": "confirm",
        "name": "关账确认",
        "type": "CLOSE_CONFIRM_TASK",
        "assigneeId": 1,
        "assigneeName": "张三",
        "slaHours": 4,
        "sort": 4,
        "preNodeCodes": "rule_check"
      },
      {
        "code": "end",
        "name": "结束",
        "type": "ONE_TIME_TASK",
        "sort": 5,
        "preNodeCodes": "confirm"
      }
    ]
  }'
```

**流程图示：**

```
  ┌───────┐    ┌────────────┐    ┌──────────┐    ┌──────────┐    ┌───────┐
  │ start │───▶│data_import│───▶│rule_check│───▶│ confirm  │───▶│  end  │
  └───────┘    └────────────┘    └──────────┘    └──────────┘    └───────┘
                  24h SLA            8h SLA           4h SLA
```

#### 4.1.2 查询模板列表

**请求示例：**

```bash
curl -X GET "http://localhost:8080/workflow/template/list"
```

#### 4.1.3 获取模板详情

**请求示例：**

```bash
curl -X GET "http://localhost:8080/workflow/template/1"
```

#### 4.1.4 发布模板

发布后模板状态从 DRAFT 变为 PUBLISHED，版本号递增。

**请求示例：**

```bash
curl -X POST "http://localhost:8080/workflow/template/publish/1?releaseNote=首次发布"
```

#### 4.1.5 更新模板

> **注意**：已发布的模板不能直接修改，需要先归档再创建新版本。

**请求示例：**

```bash
curl -X PUT "http://localhost:8080/workflow/template" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "name": "月度关账流程v1.1"
  }'
```

#### 4.1.6 归档模板

**请求示例：**

```bash
curl -X POST "http://localhost:8080/workflow/template/archive/1"
```

---

### 4.2 流程实例管理

#### 4.2.1 实例化流程

根据模板创建流程实例，自动派发首批任务。

**请求示例：**

```bash
curl -X POST "http://localhost:8080/workflow/instance/instantiate?templateId=1&periodId=4&unitId=2"
```

**响应示例：**

```json
{
  "code": 0,
  "message": "操作成功",
  "data": 2047551932905598978
}
```

**实例化流程图示：**

```
实例化时：
1. 根据 templateId 加载流程模板
2. 根据 periodId/unitId 绑定业务上下文
3. 拓扑排序确定节点执行顺序
4. 创建首批任务（无前置节点的 start 节点）
```

#### 4.2.2 查询期间实例

**请求示例：**

```bash
curl -X GET "http://localhost:8080/workflow/instance/period/4"
```

#### 4.2.3 查询单位实例

**请求示例：**

```bash
curl -X GET "http://localhost:8080/workflow/instance/unit/2"
```

#### 4.2.4 取消实例

**请求示例：**

```bash
curl -X POST "http://localhost:8080/workflow/instance/cancel/1"
```

---

### 4.3 任务管理

#### 4.3.1 查询待办任务

**请求示例：**

```bash
curl -X GET "http://localhost:8080/workflow/task/pending/1"
```

**响应示例：**

```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "instanceId": 1,
      "nodeCode": "data_import",
      "nodeName": "数据导入",
      "status": "PENDING",
      "assigneeId": 1,
      "assigneeName": "张三",
      "slaDeadline": "2026-04-25T13:00:00"
    }
  ]
}
```

#### 4.3.2 开始任务

**请求示例：**

```bash
curl -X PUT "http://localhost:8080/workflow/task/start/1"
```

#### 4.3.3 复核通过

**请求示例：**

```bash
curl -X PUT "http://localhost:8080/workflow/task/approve/1?comment=数据导入完成"
```

> **说明**：复核通过后，系统自动根据拓扑排序创建下游任务。

#### 4.3.4 复核退回

**请求示例：**

```bash
curl -X PUT "http://localhost:8080/workflow/task/reject/1?comment=数据格式有误，请重新导入"
```

#### 4.3.5 查询任务轨迹

**请求示例：**

```bash
curl -X GET "http://localhost:8080/workflow/task/trace/1"
```

**响应示例：**

```json
{
  "code": 0,
  "data": [
    {
      "taskId": 1,
      "action": "CREATE",
      "operatorName": "系统",
      "operateTime": "2026-04-24T10:00:00",
      "comment": "任务已创建"
    },
    {
      "taskId": 1,
      "action": "START",
      "operatorName": "张三",
      "operateTime": "2026-04-24T11:00:00",
      "comment": "开始处理"
    },
    {
      "taskId": 1,
      "action": "APPROVE",
      "operatorName": "张三",
      "operateTime": "2026-04-24T12:00:00",
      "comment": "数据导入完成"
    }
  ]
}
```

#### 4.3.6 查询SLA逾期任务

**请求示例：**

```bash
curl -X GET "http://localhost:8080/workflow/task/overdue"
```

---

## 5. 数据导入模块

### 5.1 文件上传

#### 5.1.1 上传Excel文件

**请求示例：**

```bash
curl -X POST "http://localhost:8080/data/import/upload" \
  -F "file=@/path/to/data.xlsx" \
  -F "importType=EXCEL" \
  -F "userId=1" \
  -F "userName=系统管理员"
```

**响应示例：**

```json
{
  "code": 0,
  "message": "操作成功",
  "data": 2047551932905598978
}
```

> **返回的 batchId 用于后续执行导入和查询进度**

#### 5.1.2 上传CSV文件

```bash
curl -X POST "http://localhost:8080/data/import/upload" \
  -F "file=@/path/to/data.csv" \
  -F "importType=CSV"
```

---

### 5.2 执行导入

上传文件后，需要执行导入操作解析文件内容。

**请求示例：**

```bash
curl -X POST "http://localhost:8080/data/import/execute/2047551932905598978"
```

**响应示例：**

```json
{
  "code": 0,
  "message": "操作成功",
  "data": true
}
```

> **说明**：导入为异步执行，可通过进度查询接口跟踪状态。

---

### 5.3 查询导入进度

**请求示例：**

```bash
curl -X GET "http://localhost:8080/data/import/progress/2047551932905598978"
```

**响应示例：**

```json
{
  "code": 0,
  "data": {
    "batchId": 2047551932905598978,
    "batchNo": "BATCH20260424100001",
    "status": "PROCESSING",
    "totalRecords": 1000,
    "processedRecords": 650,
    "successRecords": 640,
    "failRecords": 10,
    "progressPercent": 65,
    "currentRow": 650,
    "startTime": "2026-04-24T10:00:00",
    "estimatedRemainingSeconds": 120
  }
}
```

---

### 5.4 查看导入记录

#### 5.4.1 查询所有记录

```bash
curl -X GET "http://localhost:8080/data/import/records/2047551932905598978"
```

#### 5.4.2 查询失败记录

```bash
curl -X GET "http://localhost:8080/data/import/failures/2047551932905598978"
```

---

### 5.5 数据校验

对已导入的数据执行规则校验。

**请求示例：**

```bash
curl -X POST "http://localhost:8080/data/import/validate/2047551932905598978"
```

---

### 5.6 取消导入

**请求示例：**

```bash
curl -X POST "http://localhost:8080/data/import/cancel/2047551932905598978"
```

---

## 6. 规则引擎模块

### 6.1 创建规则

规则使用 AviatorScript 表达式语言。

**请求示例：**

```bash
curl -X POST "http://localhost:8080/rule" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "BALANCE_CHECK",
    "name": "余额校验规则",
    "ruleType": "VALIDATE",
    "category": "BALANCE",
    "expression": "amount > 0",
    "description": "校验余额必须大于0",
    "errorMessage": "余额必须大于0",
    "priority": 1
  }'
```

**Aviator 常用表达式示例：**

| 表达式 | 说明 |
|--------|------|
| `amount > 0` | 金额大于0 |
| `amount >= 1000 && amount <= 1000000` | 金额在范围内 |
| `period >= 1 && period <= 12` | 期间在有效范围 |
| `str.startsWith(accountCode, "1001")` | 科目代码前缀匹配 |
| `amount > 0 && str.len(name) > 0` | 组合条件 |

#### 6.1.1 发布规则

**请求示例：**

```bash
curl -X POST "http://localhost:8080/rule/publish/1"
```

> **说明**：只有 PUBLISHED 状态的规则才能被执行。

---

### 6.2 查询规则列表

**请求示例：**

```bash
curl -X GET "http://localhost:8080/rule/list"
```

**响应示例：**

```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "code": "BALANCE_CHECK",
      "name": "余额校验规则",
      "ruleType": "VALIDATE",
      "category": "BALANCE",
      "status": "PUBLISHED",
      "expression": "amount > 0",
      "priority": 1
    }
  ]
}
```

---

### 6.3 执行规则

#### 6.3.1 单规则执行

**请求示例 - 通过校验（amount=100）：**

```bash
curl -X POST "http://localhost:8080/rule/execute" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleCode": "BALANCE_CHECK",
    "params": {
      "amount": 100
    }
  }'
```

**响应示例：**

```json
{
  "code": 0,
  "data": {
    "success": true,
    "ruleCode": "BALANCE_CHECK",
    "result": true,
    "executeDuration": 5
  }
}
```

**请求示例 - 未通过校验（amount=-50）：**

```bash
curl -X POST "http://localhost:8080/rule/execute" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleCode": "BALANCE_CHECK",
    "params": {
      "amount": -50
    }
  }'
```

**响应示例：**

```json
{
  "code": 0,
  "data": {
    "success": true,
    "ruleCode": "BALANCE_CHECK",
    "result": false,
    "errorMsg": "余额必须大于0",
    "executeDuration": 3
  }
}
```

#### 6.3.2 批量执行规则

**请求示例：**

```bash
curl -X POST "http://localhost:8080/rule/execute/batch" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "ruleCode": "BALANCE_CHECK",
      "params": {"amount": 100}
    },
    {
      "ruleCode": "BALANCE_CHECK",
      "params": {"amount": -50}
    }
  ]'
```

---

### 6.4 表达式校验

在创建规则前，校验表达式语法是否正确。

**请求示例：**

```bash
curl -X POST "http://localhost:8080/rule/validate?expression=amount%20%3E%200"
```

**响应示例 - 有效表达式：**

```json
{
  "code": 0,
  "data": true
}
```

**响应示例 - 无效表达式：**

```json
{
  "code": 0,
  "data": false
}
```

---

## 7. 异常处理模块

### 7.1 记录异常

当业务流程中发现异常时，可以记录异常信息。

**请求示例：**

```bash
curl -X POST "http://localhost:8080/exception" \
  -H "Content-Type: application/json" \
  -d '{
    "exceptionCode": "E001",
    "exceptionType": "DATA_MISMATCH",
    "severity": "HIGH",
    "module": "DATA_IMPORT",
    "businessId": "IMPORT_001",
    "periodId": 4,
    "unitId": 2,
    "message": "导入金额与台账不一致",
    "stackTrace": "java.lang.RuntimeException: validation failed"
  }'
```

**响应示例：**

```json
{
  "code": 0,
  "message": "操作成功",
  "data": 2047551932905598978
}
```

**异常类型说明：**

| 类型 | 说明 |
|------|------|
| DATA_MISMATCH | 数据不一致 |
| VALIDATION_FAILED | 校验失败 |
| PROCESS_TIMEOUT | 处理超时 |
| SYSTEM_ERROR | 系统错误 |

**严重级别：**

| 级别 | 说明 |
|------|------|
| HIGH | 高优先级，需立即处理 |
| MEDIUM | 中优先级，24小时内处理 |
| LOW | 低优先级，可延后处理 |

---

### 7.2 查询待处理异常

**请求示例：**

```bash
curl -X GET "http://localhost:18080/exception/pending?periodId=4&unitId=2"
```

**响应示例：**

```json
{
  "code": 0,
  "data": [
    {
      "id": 2047551932905598978,
      "exceptionCode": "E001",
      "exceptionType": "DATA_MISMATCH",
      "severity": "HIGH",
      "module": "DATA_IMPORT",
      "businessId": "IMPORT_001",
      "status": "PENDING"
    }
  ]
}
```

---

### 7.3 处理异常

**请求示例：**

```bash
curl -X POST "http://localhost:8080/exception/handle/2047551932905598978?handlerId=1&handlerName=admin&comment=已修正数据"
```

---

### 7.4 忽略异常

**请求示例：**

```bash
curl -X POST "http://localhost:8080/exception/ignore/2047551932905598978?reason=数据已手动修正，无需处理"
```

---

### 7.5 异常统计

**请求示例：**

```bash
curl -X GET "http://localhost:8080/exception/statistics?periodId=4&unitId=2"
```

**响应示例：**

```json
{
  "code": 0,
  "data": {
    "TOTAL": 10,
    "PENDING": 3,
    "RESOLVED": 6,
    "IGNORED": 1
  }
}
```

---

### 7.6 批量处理异常

**请求示例：**

```bash
curl -X POST "http://localhost:8080/exception/batch/handle" \
  -H "Content-Type: application/json" \
  -d [1, 2, 3] \
  -d "handlerId=1" \
  -d "handlerName=admin"
```

---

## 8. 通知模块

### 8.1 发送通知

#### 8.1.1 发送 WebSocket 通知

**请求示例：**

```bash
curl -X POST "http://localhost:8080/notification/send" \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "WEB_SOCKET",
    "recipient": "admin",
    "subject": "月度关账提醒",
    "content": "2026年4月期间需要在本周五前完成关账操作。"
  }'
```

#### 8.1.2 发送邮件通知

**请求示例：**

```bash
curl -X POST "http://localhost:8080/notification/send" \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "EMAIL",
    "recipient": "user@example.com",
    "subject": "月度关账提醒",
    "content": "请及时处理待办任务。"
  }'
```

> **注意**：邮件发送需要配置 SMTP 服务器，当前环境可能跳过。

---

### 8.2 通知模板管理

#### 8.2.1 创建模板

**请求示例：**

```bash
curl -X POST "http://localhost:8080/notification/template" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "TEMPLATE_EMAIL_REMINDER",
    "name": "邮件提醒模板",
    "templateType": "REMINDER",
    "channel": "EMAIL",
    "subject": "月度关账提醒 - {{period}}",
    "content": "尊敬的 {{user}}，请在 {{deadline}} 前完成 {{period}} 的关账操作。"
  }'
```

#### 8.2.2 查询模板列表

**请求示例：**

```bash
curl -X GET "http://localhost:8080/notification/template/list?templateType=REMINDER&channel=EMAIL"
```

#### 8.2.3 获取模板详情

**请求示例：**

```bash
curl -X GET "http://localhost:8080/notification/template/TEMPLATE_EMAIL_REMINDER"
```

---

### 8.3 通知记录查询

**请求示例：**

```bash
curl -X GET "http://localhost:8080/notification/record/list?status=SENT&channel=WEB_SOCKET"
```

**响应示例：**

```json
{
  "code": 0,
  "data": [
    {
      "id": 2047553267675729921,
      "channel": "WEB_SOCKET",
      "recipient": "admin",
      "subject": "月度关账提醒",
      "content": "2026年4月期间需要在本周五前完成关账操作。",
      "status": "SENT",
      "sendTime": "2026-04-24T13:48:35"
    }
  ]
}
```

---

### 8.4 通知统计

**请求示例：**

```bash
curl -X GET "http://localhost:8080/notification/statistics"
```

**响应示例：**

```json
{
  "code": 0,
  "data": {
    "TOTAL": 5,
    "SENT": 4,
    "FAILED": 0,
    "PENDING": 1
  }
}
```

---

### 8.5 重试发送

**请求示例：**

```bash
curl -X POST "http://localhost:8080/notification/retry/1"
```

---

## 9. 常见问题

### 9.1 如何获取帮助？

- Swagger UI：http://localhost:8080/swagger-ui.html
- API 文档：http://localhost:8080/v3/api-docs

### 9.2 流程实例化失败？

1. 确认模板已发布（status=PUBLISHED）
2. 确认期间和单位存在
3. 检查模板节点配置是否合法（有 start 和 end 节点）

### 9.3 规则执行返回 null？

确认传入的参数名与表达式中使用的变量名一致。

### 9.4 导入文件失败？

1. 确认文件格式正确（Excel .xlsx 或 CSV）
2. 确认文件大小未超过限制
3. 检查文件编码（CSV 推荐 UTF-8）

### 9.5 忘记密码怎么办？

联系系统管理员使用重置密码功能。

---

## 附录

### A. API 统一响应格式

```json
{
  "code": 0,           // 0=成功，非0=失败
  "message": "操作成功",
  "data": {},          // 返回数据
  "timestamp": 1713945600000,
  "requestId": null,
  "success": true
}
```

### B. 分页响应格式

```json
{
  "total": 100,        // 总记录数
  "size": 10,          // 每页大小
  "current": 1,        // 当前页
  "records": []        // 记录列表
}
```

### C. 状态码说明

| 状态码 | 说明 |
|--------|------|
| 0 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或认证失败 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

# 月结报表管理系统 - 功能测试标准操作规程（SOP）

> 文档版本：v1.0
> 更新日期：2026-04-25
> 测试环境：本地开发环境 / SIT环境

---

## 1. 文档目的与范围

本SOP旨在为测试团队提供月结报表管理系统全功能模块的标准测试流程，确保所有已开发功能（系统管理、流程引擎、数据导入、规则引擎、异常处理、通知模块）均经过充分验证。

**测试前提：**
- 后端服务已启动（端口18080）
- 前端开发服务已启动（端口3000）
- PostgreSQL数据库已初始化（init_schema.sql + audit_log.sql已执行）
- 测试账号：admin / admin123（系统管理员）

---

## 2. 环境准备与启动

### 2.1 后端服务启动

```bash
# 编译项目
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
cd /path/to/monthly-close-system
mvn clean package -DskipTests

# 启动后端（端口18080）
java -jar mc-api/target/mc-api-1.0.0-SNAPSHOT.jar --server.port=18080
```

### 2.2 前端服务启动

```bash
cd mc-frontend
npm install
npm run dev   # 访问 http://localhost:3000
```

### 2.3 数据库初始化

```bash
# 启动PostgreSQL
docker run -d --name mc-postgres \
  -e POSTGRES_DB=monthly_close \
  -e POSTGRES_USER=mcuser \
  -e POSTGRES_PASSWORD=mcpassword \
  -p 5432:5432 \
  postgres:15

# 执行初始化脚本
docker exec mc-postgres psql -U mcuser -d monthly_close -f sql/init_schema.sql
docker exec mc-postgres psql -U mcuser -d monthly_close -f sql/audit_log.sql
```

---

## 3. 接口测试基础

### 3.1 获取访问令牌

所有受保护接口需要在请求头中携带JWT Token：

```bash
# 登录获取Token
curl -X POST http://localhost:18080/system/auth/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin123"

# 响应示例
# {"code":0,"message":"操作成功","data":{"realName":"系统管理员","userId":1,"token":"eyJhbGc..."}}

# 设置Token变量（后续测试使用）
TOKEN="eyJhbGc..."
```

### 3.2 统一响应格式

所有API统一响应格式为：

```json
{
  "code": 0,           // 0=成功，其他=失败
  "message": "操作成功",
  "data": { ... },     // 业务数据
  "timestamp": 1777123000,
  "requestId": null,
  "success": true
}
```

### 3.3 CORS说明

后端已启用CORS，Origin为`http://localhost:3000`的浏览器请求可直接访问。

---

## 4. M1 - 系统管理模块测试

### 4.1 认证模块

#### TC-M1-001：用户登录成功

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /system/auth/login，参数username=admin，password=admin123 | 返回code=0，data含token字段 |
| 2 | 使用返回的token，GET /system/auth/current | 返回当前用户信息（username=admin, realName=系统管理员） |

```bash
# 步骤1
curl -X POST http://localhost:18080/system/auth/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin123"

# 步骤2（使用步骤1返回的token）
curl http://localhost:18080/system/auth/current \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M1-002：用户登录失败 - 密码错误

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /system/auth/login，password错误 | 返回code≠0，提示用户名或密码错误 |

```bash
curl -X POST http://localhost:18080/system/auth/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=wrongpassword"
```

#### TC-M1-003：未认证访问受保护接口

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /system/auth/current（无Authorization头） | 返回401，提示"缺少认证Token" |

```bash
curl http://localhost:18080/system/auth/current
```

#### TC-M1-004：无效Token访问

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /system/auth/current，使用伪造Token | 返回401，提示"Token无效或已过期" |

```bash
curl http://localhost:18080/system/auth/current \
  -H "Authorization: Bearer invalid.token.here"
```

---

### 4.2 组织管理模块

#### TC-M1-005：组织树查询

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /system/org/tree | 返回完整树形结构，code=0 |

```bash
curl http://localhost:18080/system/org/tree \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M1-006：新增组织

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /system/org，body: {"orgName":"测试部门","orgCode":"TEST-DEPT","parentId":1,"status":1,"sortOrder":0} | 返回code=0，data=true |
| 2 | GET /system/org/tree，确认新组织出现 | 树中包含"测试部门" |

```bash
curl -X POST http://localhost:18080/system/org \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"orgName":"测试部门","orgCode":"TEST-DEPT","parentId":1,"status":1,"sortOrder":0}'
```

#### TC-M1-007：编辑组织

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | PUT /system/org，body: {"id":X,"orgName":"测试部门-已修改","orgCode":"TEST-DEPT"} | 返回code=0 |
| 2 | GET /system/org/tree | 组织名称已更新 |

#### TC-M1-008：删除组织

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | DELETE /system/org/{id}（叶子节点，无子组织） | 返回code=0 |
| 2 | GET /system/org/tree | 该组织已不存在 |

#### TC-M1-009：子节点查询

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /system/org/children/1 | 返回parentId=1的直接子节点列表 |

```bash
curl http://localhost:18080/system/org/children/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

### 4.3 用户管理模块

#### TC-M1-010：用户分页列表

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /system/user/page?pageNum=1&pageSize=10 | 返回分页用户列表，含total总数 |

```bash
curl "http://localhost:18080/system/user/page?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M1-011：新增用户

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /system/user，body: {"username":"testuser","realName":"测试用户","orgId":1,"password":"test123","status":1} | 返回code=0 |
| 2 | 使用新账号登录 | 登录成功，返回有效token |

```bash
curl -X POST http://localhost:18080/system/user \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","realName":"测试用户","orgId":1,"password":"test123","status":1}'
```

#### TC-M1-012：编辑用户

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | PUT /system/user，body: {"id":X,"realName":"测试用户-已修改"} | 返回code=0 |
| 2 | GET /system/user/{id} | realName已更新 |

#### TC-M1-013：重置密码

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /system/user/resetPassword/{userId}?oldPassword=xxx&newPassword=yyy | 返回code=0 |
| 2 | 使用新密码登录 | 登录成功 |

```bash
curl -X POST "http://localhost:18080/system/user/resetPassword/2?oldPassword=test123&newPassword=admin123" \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M1-014：删除用户

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | DELETE /system/user/{id} | 返回code=0 |
| 2 | GET /system/user/{id} | 返回null或空 |

---

### 4.4 角色管理模块

#### TC-M1-015：角色列表

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /system/role/page | 返回角色列表 |

```bash
curl http://localhost:18080/system/role/page \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M1-016：新增角色

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /system/role，body: {"roleName":"测试角色","roleCode":"TEST-ROLE","status":1} | 返回code=0 |

```bash
curl -X POST http://localhost:18080/system/role \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"roleName":"测试角色","roleCode":"TEST-ROLE","status":1}'
```

#### TC-M1-017：编辑角色

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | PUT /system/role，body: {"id":X,"roleName":"测试角色-已修改"} | 返回code=0 |

#### TC-M1-018：删除角色

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | DELETE /system/role/{id} | 返回code=0 |

---

### 4.5 期间管理模块

#### TC-M1-019：期间列表

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /system/period/list | 返回所有期间列表 |

```bash
curl http://localhost:18080/system/period/list \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M1-020：新增期间

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /system/period，body: {"code":"2026-05","year":2026,"month":5,"startDate":"2026-05-01","endDate":"2026-05-31","status":"ACTIVE","isCurrent":0} | 返回code=0 |
| 2 | GET /system/period/list | 列表中包含新的期间 |

```bash
curl -X POST http://localhost:18080/system/period \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"code":"2026-05","year":2026,"month":5,"startDate":"2026-05-01","endDate":"2026-05-31","status":"ACTIVE","isCurrent":0}'
```

#### TC-M1-021：编辑期间

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | PUT /system/period，body: {"id":X,"code":"2026-05","year":2026,"month":5} | 返回code=0 |

#### TC-M1-022：关闭期间

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /system/period/close/{id} | 返回code=0 |
| 2 | GET /system/period/{id} | status变为CLOSED |

```bash
curl -X POST http://localhost:18080/system/period/close/5 \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M1-023：获取当前期间

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /system/period/current | 返回isCurrent=1的期间 |

```bash
curl http://localhost:18080/system/period/current \
  -H "Authorization: Bearer $TOKEN"
```

---

## 5. M2 - 流程引擎模块测试

### 5.1 流程模板

#### TC-M2-001：创建流程模板

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /workflow/template，body含节点配置JSON | 返回code=0，模板状态=DRAFT |

节点配置示例：
```json
{
  "name": "测试流程",
  "code": "TEST-FLOW",
  "description": "测试流程模板",
  "nodeConfig": "[{\"nodeCode\":\"start\",\"nodeName\":\"开始\",\"type\":\"START\"},{\"nodeCode\":\"task1\",\"nodeName\":\"录入任务\",\"type\":\"TASK\",\"slaHours\":24,\"dependencies\":[\"start\"]},{\"nodeCode\":\"end\",\"nodeName\":\"结束\",\"type\":\"END\",\"dependencies\":[\"task1\"]}]"
}
```

```bash
curl -X POST http://localhost:18080/workflow/template \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"测试流程","code":"TEST-FLOW","description":"测试流程模板","nodeConfig":"[{\"nodeCode\":\"start\",\"nodeName\":\"开始\",\"type\":\"START\"},{\"nodeCode\":\"task1\",\"nodeName\":\"录入任务\",\"type\":\"TASK\",\"slaHours\":24,\"dependencies\":[\"start\"]},{\"nodeCode\":\"end\",\"nodeName\":\"结束\",\"type\":\"END\",\"dependencies\":[\"task1\"]}]"}'
```

#### TC-M2-002：发布流程模板

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /workflow/template/publish/{id} | 返回code=0，模板状态由DRAFT变为PUBLISHED，version从1变为2 |

```bash
curl -X POST http://localhost:18080/workflow/template/publish/1 \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M2-003：查询模板列表

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /workflow/template/list | 返回所有模板列表，含DRAFT和PUBLISHED状态 |
| 2 | GET /workflow/template/list?status=PUBLISHED | 仅返回已发布模板 |

```bash
curl http://localhost:18080/workflow/template/list \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M2-004：查询模板详情

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /workflow/template/{id} | 返回模板完整信息，含nodeConfig JSON |

```bash
curl http://localhost:18080/workflow/template/1 \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M2-005：编辑流程模板

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | PUT /workflow/template，body: {"id":X,"name":"已修改名称"} | 返回code=0 |
| 2 | GET /workflow/template/{id} | 名称已更新（需注意：已发布模板通常不允许编辑） |

---

### 5.2 流程实例

#### TC-M2-006：实例化流程

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /workflow/instance/instantiate?templateId=X&periodId=Y&unitId=Z | 返回code=0，data为新实例ID |
| 2 | GET /workflow/instance/list | 列表中包含新实例，status=RUNNING |

```bash
curl -X POST "http://localhost:18080/workflow/instance/instantiate?templateId=1&periodId=4&unitId=1" \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M2-007：查询期间实例

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /workflow/instance/period/{periodId} | 返回该期间的所有实例 |

```bash
curl http://localhost:18080/workflow/instance/period/4 \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M2-008：取消流程实例

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /workflow/instance/cancel/{id} | 返回code=0 |
| 2 | GET /workflow/instance/list | 该实例status变为CANCELLED |

---

### 5.3 任务管理

#### TC-M2-009：任务列表查询

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /workflow/task/list | 返回所有任务列表 |

```bash
curl http://localhost:18080/workflow/task/list \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M2-010：待办任务查询

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /workflow/task/pending/{assigneeId} | 返回该处理人的待办任务（PENDING状态） |

```bash
curl http://localhost:18080/workflow/task/pending/1 \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M2-011：开始任务

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | PUT /workflow/task/start/{taskId} | 返回code=0，任务状态由PENDING变为IN_PROGRESS，startTime被设置 |
| 2 | GET /workflow/task/trace/{taskId} | 轨迹中包含"开始任务"记录 |

```bash
curl -X PUT http://localhost:18080/workflow/task/start/2047541232464347137 \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M2-012：复核通过任务

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | PUT /workflow/task/approve/{taskId}?comment=测试通过 | 返回code=0，任务状态变为COMPLETED，endTime被设置 |
| 2 | GET /workflow/task/trace/{taskId} | 轨迹中包含"复核通过"记录 |
| 3 | 下游任务（如有）自动从PENDING变为IN_PROGRESS | 下游任务的preTaskIds引用当前任务 |

```bash
curl -X PUT "http://localhost:18080/workflow/task/approve/2047541232464347137?comment=测试通过" \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M2-013：复核退回任务

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | PUT /workflow/task/reject/{taskId}?comment=数据有问题 | 返回code=0，任务状态变为REJECTED |
| 2 | GET /workflow/task/trace/{taskId} | 轨迹中包含"复核退回"记录和退回原因 |

```bash
curl -X PUT "http://localhost:18080/workflow/task/reject/2047541232464347137?comment=数据有问题" \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M2-014：任务轨迹查询

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /workflow/task/trace/{taskId} | 返回该任务的所有操作轨迹（按时间正序） |

```bash
curl http://localhost:18080/workflow/task/trace/2047541232464347137 \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M2-015：SLA逾期查询

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /workflow/task/overdue | 返回所有已逾期任务列表（当前时间>SLA截止时间且状态为PENDING/IN_PROGRESS） |

```bash
curl http://localhost:18080/workflow/task/overdue \
  -H "Authorization: Bearer $TOKEN"
```

---

## 6. M3 - 数据导入模块测试

### 6.1 文件上传

#### TC-M3-001：上传Excel文件

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /data/import/upload，multipart file | 返回code=0，data为batchId（Long型） |

```bash
curl -X POST http://localhost:18080/data/import/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/test.xlsx" \
  -F "importType=EXCEL"
```

#### TC-M3-002：上传CSV文件

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /data/import/upload，multipart file，importType=CSV | 返回code=0，data为batchId |

```bash
curl -X POST http://localhost:18080/data/import/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/test.csv" \
  -F "importType=CSV"
```

#### TC-M3-003：非法文件名防护

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 上传文件名为`../../../etc/passwd`的文件 | 返回失败，提示"文件名非法" |

### 6.2 导入执行

#### TC-M3-004：执行导入

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /data/import/execute/{batchId} | 返回code=0，启动异步导入 |
| 2 | GET /data/import/progress/{batchId}（轮询） | 进度不断更新，最终status=COMPLETED/PARTIAL_SUCCESS/FAILED |

```bash
curl -X POST http://localhost:18080/data/import/execute/1 \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M3-005：导入进度查询

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /data/import/progress/{batchId} | 返回{total, success, fail, status} |

```bash
curl http://localhost:18080/data/import/progress/1 \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M3-006：导入记录查询

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /data/import/records/{batchId} | 返回该批次所有导入记录列表 |

```bash
curl http://localhost:18080/data/import/records/1 \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M3-007：失败记录查询

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /data/import/failures/{batchId} | 仅返回status=FAIL的记录，含错误信息 |

```bash
curl http://localhost:18080/data/import/failures/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 6.3 数据校验

#### TC-M3-008：数据校验

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /data/import/validate/{batchId} | 返回code=0，执行数据校验（不影响已导入数据） |

```bash
curl -X POST http://localhost:18080/data/import/validate/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 6.4 取消导入

#### TC-M3-009：取消导入

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /data/import/cancel/{batchId} | 返回code=0，批次状态变为CANCELLED |

```bash
curl -X POST http://localhost:18080/data/import/cancel/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

## 7. M4 - 规则引擎模块测试

### 7.1 规则配置

#### TC-M4-001：创建规则

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /rule，body: {"ruleCode":"R001","ruleName":"金额正数","expression":"amount > 0","category":"VALIDATION"} | 返回code=0，规则状态=DRAFT |

```bash
curl -X POST http://localhost:18080/rule \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"ruleCode":"R001","ruleName":"金额正数","expression":"amount > 0","category":"VALIDATION"}'
```

#### TC-M4-002：发布规则

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /rule/publish/{id} | 返回code=0，规则状态由DRAFT变为PUBLISHED |

```bash
curl -X POST http://localhost:18080/rule/publish/1 \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M4-003：规则列表查询

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /rule/list | 返回所有规则 |
| 2 | GET /rule/list?status=PUBLISHED | 仅返回已发布规则 |

```bash
curl http://localhost:18080/rule/list \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M4-004：编辑规则

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | PUT /rule，body: {"id":X,"ruleName":"已修改"} | 返回code=0 |

#### TC-M4-005：删除规则

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | DELETE /rule/{id} | 返回code=0 |

### 7.2 规则执行

#### TC-M4-006：执行规则 - 条件为真

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /rule/execute，body: {"ruleCode":"R001","params":{"amount":100}} | 返回code=0，result=true |

```bash
curl -X POST http://localhost:18080/rule/execute \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"ruleCode":"R001","params":{"amount":100}}'
```

#### TC-M4-007：执行规则 - 条件为假

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /rule/execute，body: {"ruleCode":"R001","params":{"amount":-50}} | 返回code=0，result=false |

```bash
curl -X POST http://localhost:18080/rule/execute \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"ruleCode":"R001","params":{"amount":-50}}'
```

#### TC-M4-008：批量执行规则

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /rule/execute/batch，body为规则请求数组 | 返回code=0，data为结果数组 |

```bash
curl -X POST http://localhost:18080/rule/execute/batch \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '[{"ruleCode":"R001","params":{"amount":100}},{"ruleCode":"R001","params":{"amount":-50}}]'
```

#### TC-M4-009：表达式校验 - 合法表达式

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /rule/validate?expression=amount%20%3E%200 | 返回code=0 |

```bash
curl -X POST "http://localhost:18080/rule/validate?expression=amount > 0" \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M4-010：表达式校验 - 非法表达式

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /rule/validate?expression=amount%20%3E%09 | 返回code≠0，提示编译错误 |

---

## 8. M5 - 异常处理模块测试

### 8.1 异常记录

#### TC-M5-001：记录新异常

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /exception，body含exceptionCode等字段 | 返回code=0，data为新异常记录ID |

```bash
curl -X POST http://localhost:18080/exception \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"exceptionCode":"E001","exceptionType":"DATA_ERROR","severity":"HIGH","module":"M3","businessId":"123","message":"数据校验失败"}'
```

### 8.2 异常查询

#### TC-M5-002：待处理异常查询

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /exception/pending | 返回所有PENDING状态的异常记录 |

```bash
curl http://localhost:18080/exception/pending \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M5-003：异常详情查询

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /exception/{id} | 返回该异常的完整信息含堆栈 |

```bash
curl http://localhost:18080/exception/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 8.3 异常处理

#### TC-M5-004：处理异常

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /exception/handle/{id}?handlerId=1&handlerName=admin&comment=已处理 | 返回code=0，状态变为RESOLVED |

```bash
curl -X POST "http://localhost:18080/exception/handle/1?handlerId=1&handlerName=admin&comment=已修复" \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M5-005：忽略异常

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /exception/ignore/{id}?reason=重复数据无需处理 | 返回code=0，状态变为IGNORED |

```bash
curl -X POST "http://localhost:18080/exception/ignore/1?reason=重复数据无需处理" \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M5-006：异常统计

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /exception/statistics | 返回{TOTAL, PENDING, RESOLVED, IGNORED}各计数值 |

```bash
curl http://localhost:18080/exception/statistics \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M5-007：批量处理异常

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /exception/batch/handle?handlerId=1&handlerName=admin，body: [id1, id2, id3] | 返回code=0，所有指定异常状态变为RESOLVED |

```bash
curl -X POST "http://localhost:18080/exception/batch/handle?handlerId=1&handlerName=admin" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '[1,2,3]'
```

---

## 9. M6 - 通知模块测试

### 9.1 发送通知

#### TC-M6-001：发送邮件通知

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /notification/send，body: {"channel":"EMAIL","recipient":"test@example.com","title":"测试标题","content":"测试内容"} | 返回code=0，发送成功（邮件实际发送取决于SMTP配置） |

```bash
curl -X POST http://localhost:18080/notification/send \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"channel":"EMAIL","recipient":"test@example.com","title":"测试标题","content":"测试内容"}'
```

#### TC-M6-002：发送WebSocket通知

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /notification/send，body: {"channel":"WEB_SOCKET","recipient":"admin","title":"通知标题","content":"通知内容"} | 返回code=0 |

### 9.2 通知模板

#### TC-M6-003：创建通知模板

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /notification/template，body: {"templateCode":"T001","templateName":"测试模板","channel":"EMAIL","templateType":"SYSTEM","status":"ACTIVE"} | 返回code=0 |

```bash
curl -X POST http://localhost:18080/notification/template \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"templateCode":"T001","templateName":"测试模板","channel":"EMAIL","templateType":"SYSTEM","status":"ACTIVE"}'
```

#### TC-M6-004：模板列表查询

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /notification/template/list | 返回所有模板 |

```bash
curl http://localhost:18080/notification/template/list \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M6-005：查询单个模板

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /notification/template/{code} | 返回模板详情 |

```bash
curl http://localhost:18080/notification/template/T001 \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M6-006：更新模板

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | PUT /notification/template，body: {"id":X,"templateName":"已修改"} | 返回code=0 |

#### TC-M6-007：删除模板

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | DELETE /notification/template/{id} | 返回code=0 |

### 9.3 通知记录

#### TC-M6-008：通知记录列表

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /notification/record/list | 返回所有通知记录列表 |

```bash
curl http://localhost:18080/notification/record/list \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M6-009：按状态筛选通知记录

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /notification/record/list?status=SENT | 仅返回已发送的记录 |

```bash
curl "http://localhost:18080/notification/record/list?status=SENT" \
  -H "Authorization: Bearer $TOKEN"
```

### 9.4 重试与统计

#### TC-M6-010：重试发送失败通知

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | POST /notification/retry/{id} | 返回code=0，重新尝试发送 |

```bash
curl -X POST http://localhost:18080/notification/retry/1 \
  -H "Authorization: Bearer $TOKEN"
```

#### TC-M6-011：通知统计

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | GET /notification/statistics | 返回{TOTAL, SENT, FAILED, PENDING}统计 |

```bash
curl http://localhost:18080/notification/statistics \
  -H "Authorization: Bearer $TOKEN"
```

---

## 10. M7 - 前端UI测试

### 10.1 登录与首页

#### TC-M7-001：登录页面

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 打开 http://localhost:3000 | 跳转到登录页 |
| 2 | 输入admin/admin123，点击登录 | 登录成功，跳转到首页（Dashboard） |
| 3 | 点击右侧用户名，下拉菜单显示"退出登录" | 菜单正常展开 |

### 10.2 首页Dashboard

#### TC-M7-002：首页数据展示

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | Dashboard显示4个统计卡片 | 用户数/运行实例/待办任务/异常数 |
| 2 | Dashboard显示快捷操作按钮 | 发起流程/数据导入/处理任务/查看异常 |
| 3 | 点击"发起流程"按钮 | 跳转到/exception（注意：应为/rule/instantiate，当前实现可能跳转异常） |

### 10.3 系统管理模块

#### TC-M7-003：组织管理页面

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 进入"系统管理 > 组织管理" | 显示组织树 |
| 2 | 点击"新增组织" | 弹出对话框，可填写组织信息 |
| 3 | 点击"编辑"按钮 | 弹出编辑对话框 |
| 4 | 点击"删除"按钮 | 确认后删除成功 |

#### TC-M7-004：用户管理页面

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 进入"系统管理 > 用户管理" | 显示用户分页列表 |
| 2 | 点击"新增用户" | 弹出对话框，可填写用户信息 |
| 3 | 点击"编辑" | 弹出编辑对话框 |
| 4 | 点击"重置密码" | 确认后密码重置 |
| 5 | 分页切换 | 列表正确切换 |

#### TC-M7-005：角色管理页面

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 进入"系统管理 > 角色管理" | 显示角色列表 |
| 2 | 新增/编辑/删除角色 | 操作成功，列表刷新 |

#### TC-M7-006：期间管理页面

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 进入"系统管理 > 期间管理" | 显示期间列表 |
| 2 | 新增期间 | 列表中显示新期间 |
| 3 | 点击"关闭"按钮 | 状态变为"关闭" |

### 10.4 流程引擎模块

#### TC-M7-007：流程模板页面

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 进入"流程引擎 > 流程模板" | 显示模板列表 |
| 2 | 点击"新增模板"，填写节点配置 | 模板创建成功，状态=DRAFT |
| 3 | 点击"发布" | 状态变为PUBLISHED |
| 4 | 点击"查看详情" | 显示节点配置JSON |

#### TC-M7-008：流程实例页面

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 进入"流程引擎 > 流程实例" | 显示实例列表 |
| 2 | 点击"发起流程"，选择模板/期间/单位 | 实例创建成功 |
| 3 | 点击"取消"（运行中实例） | 确认后实例取消 |

#### TC-M7-009：任务管理页面

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 进入"流程引擎 > 任务管理" | 显示所有任务列表 |
| 2 | 按状态筛选 | 列表正确筛选 |
| 3 | 点击"开始" | 任务状态变更 |
| 4 | 点击"通过"或"退回" | 操作成功，轨迹增加 |
| 5 | 点击"轨迹" | 显示任务操作轨迹 |

### 10.5 数据导入模块

#### TC-M7-010：数据导入页面

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 进入"数据导入" | 页面正常加载 |
| 2 | 选择Excel文件，点击上传 | 上传成功，获得batchId |
| 3 | 点击"执行导入" | 进度条动态更新 |
| 4 | 导入完成 | 显示成功/失败记录数 |

### 10.6 规则配置模块

#### TC-M7-011：规则配置页面

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 进入"规则配置" | 显示规则列表 |
| 2 | 新增规则 | 规则创建成功，状态=DRAFT |
| 3 | 点击"发布" | 状态变为PUBLISHED |
| 4 | 点击"测试"，填写参数{"amount":100} | 显示测试结果true |

### 10.7 异常处理模块

#### TC-M7-012：异常处理页面

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 进入"异常处理" | 显示统计卡片和待处理列表 |
| 2 | 点击"处理" | 输入备注后，状态变为"已处理" |
| 3 | 点击"忽略" | 输入原因后，状态变为"已忽略" |
| 4 | 点击"详情" | 显示异常详细信息含堆栈 |

### 10.8 通知管理模块

#### TC-M7-013：通知管理页面

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 进入"通知管理" | 显示统计卡片和通知记录列表 |
| 2 | 切换到"模板管理" | 显示模板列表 |
| 3 | 新增/编辑/删除模板 | 操作成功 |
| 4 | 点击"发送通知"，填写信息 | 发送成功 |

---

## 11. 安全测试

### 11.1 认证与授权

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 无Token访问 | 请求任意受保护接口无Authorization头 | 返回401 |
| 伪造Token | 使用假Token访问 | 返回401 |
| 过期Token | 使用过期/无效Token访问 | 返回401 |

### 11.2 SQL注入防护

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 组织名称SQL注入 | 新增组织orgName=`' OR '1'='1` | 应被安全处理，不报错或正常存储 |
| 用户名注入 | 用户名含SQL片段 | 应被安全处理 |

### 11.3 CSV公式注入防护

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| CSV文件含公式 | 上传含`=CMD\|'ls`的CSV | 后端sanitize处理，添加前缀单引号 |

### 11.4 文件上传安全

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 路径穿越文件名 | 上传文件名为`../../etc/passwd` | 返回失败，提示"文件名非法" |

### 11.5 Aviator表达式沙箱

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 尝试访问外部类 | 规则表达式`System.exit(1)` | 返回编译错误，拒绝执行 |

---

## 12. 性能与边界测试

### 12.1 批量操作

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 批量导入1000条 | 上传含1000条记录的Excel | 正常完成，进度准确 |
| 批量处理100个异常 | 传入100个ID的批量处理 | 全部处理成功 |

### 12.2 边界值测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 空用户名登录 | username="" | 返回参数校验失败 |
| 期间月份超范围 | month=13 | 应被后端校验拒绝 |
| 规则表达式除零 | expression=`a/0` | Aviator执行返回错误，不崩溃 |

---

## 13. 附录

### 13.1 接口访问速查

| 功能 | 接口 | 方法 |
|------|------|------|
| 登录 | /system/auth/login | POST |
| 获取当前用户 | /system/auth/current | GET |
| 组织树 | /system/org/tree | GET |
| 用户分页 | /system/user/page | GET |
| 角色列表 | /system/role/page | GET |
| 期间列表 | /system/period/list | GET |
| 模板列表 | /workflow/template/list | GET |
| 实例列表 | /workflow/instance/list | GET |
| 任务列表 | /workflow/task/list | GET |
| 待办任务 | /workflow/task/pending/{assigneeId} | GET |
| 逾期任务 | /workflow/task/overdue | GET |
| 任务轨迹 | /workflow/task/trace/{taskId} | GET |
| 导入上传 | /data/import/upload | POST |
| 导入执行 | /data/import/execute/{batchId} | POST |
| 导入进度 | /data/import/progress/{batchId} | GET |
| 规则列表 | /rule/list | GET |
| 规则执行 | /rule/execute | POST |
| 表达式校验 | /rule/validate | POST |
| 待处理异常 | /exception/pending | GET |
| 异常统计 | /exception/statistics | GET |
| 通知发送 | /notification/send | POST |
| 通知模板列表 | /notification/template/list | GET |
| 通知记录 | /notification/record/list | GET |
| 通知统计 | /notification/statistics | GET |

### 13.2 测试数据准备

```sql
-- 确保有测试期间
INSERT INTO sys_period (code, name, year, month, start_date, end_date, status, is_current)
VALUES ('2026-05', '2026年5月', 2026, 5, '2026-05-01', '2026-05-31', 'ACTIVE', 0);

-- 确保有测试组织
INSERT INTO sys_org (org_name, org_code, parent_id, status, sort_order)
VALUES ('测试单位', 'TEST-UNIT', 1, 1, 0);

-- 确保有测试用户
INSERT INTO sys_user (username, password, real_name, org_id, status)
VALUES ('testuser', '$2a$10$...', '测试用户', 2, 1);
```

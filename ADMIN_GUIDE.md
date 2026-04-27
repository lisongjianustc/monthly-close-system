# 月结报表管理系统 - 系统管理手册

> 文档版本: 1.0.0
> 更新日期: 2026-04-24
> 适用对象: 系统管理员 / 运维人员

---

## 目录

1. [管理员职责与账号](#1-管理员职责与账号)
2. [系统配置管理](#2-系统配置管理)
3. [组织架构管理](#3-组织架构管理)
4. [用户与权限管理](#4-用户与权限管理)
5. [菜单与角色管理](#5-菜单与角色管理)
6. [会计期间管理](#6-会计期间管理)
7. [流程模板管理](#7-流程模板管理)
8. [规则引擎管理](#8-规则引擎管理)
9. [数据导入模板配置](#9-数据导入模板配置)
10. [通知配置](#10-通知配置)
11. [系统监控与维护](#11-系统监控与维护)
12. [备份与恢复](#12-备份与恢复)
13. [安全建议](#13-安全建议)

---

## 1. 管理员职责与账号

### 1.1 管理员职责

系统管理员承担以下职责：

| 职责 | 说明 |
|------|------|
| 用户管理 | 创建、修改、禁用用户账号 |
| 角色分配 | 分配用户角色和权限 |
| 组织维护 | 维护组织架构 |
| 期间管理 | 管理会计期间状态 |
| 流程配置 | 配置和管理流程模板 |
| 规则配置 | 配置业务规则 |
| 系统监控 | 监控系统运行状态 |

### 1.2 默认管理员账号

| 用户名 | 密码 | 说明 |
|--------|------|------|
| admin | admin123 | 系统管理员，首次登录后请修改密码 |

### 1.3 首次登录检查清单

- [ ] 使用 admin 账号登录
- [ ] 修改 admin 默认密码
- [ ] 创建新的管理员账号（不共享使用 admin）
- [ ] 配置组织架构
- [ ] 创建业务用户账号
- [ ] 配置角色和权限
- [ ] 设置会计期间

---

## 2. 系统配置管理

### 2.1 配置文件位置

```
mc-api/src/main/resources/application.yml
```

### 2.2 主要配置项

```yaml
server:
  port: 8080                      # 服务端口

spring:
  application:
    name: monthly-close-system    # 应用名称

  datasource:
    url: jdbc:postgresql://localhost:5432/monthly_close
    username: mcuser
    password: mcpassword
    driver-class-name: org.postgresql.Driver

  data:
    redis:
      host: localhost
      port: 6379

mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.mc.**.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

### 2.3 配置变更流程

1. **开发环境**：直接修改 `application.yml`
2. **测试环境**：通过环境变量覆盖
3. **生产环境**：使用 `application-prod.yml` 并通过环境变量配置敏感信息

### 2.4 环境变量配置

| 变量名 | 说明 | 示例 |
|--------|------|------|
| JAVA_HOME | JDK路径 | /opt/homebrew/opt/openjdk@17 |
| SPRING_PROFILES_ACTIVE | 激活配置文件 | dev / test / prod |
| SERVER_PORT | 服务端口 | 8080 |
| SPRING_DATASOURCE_URL | 数据库URL | jdbc:postgresql://host:5432/db |
| SPRING_DATASOURCE_USERNAME | 数据库用户名 | mcuser |
| SPRING_DATASOURCE_PASSWORD | 数据库密码 | mcpassword |

---

## 3. 组织架构管理

### 3.1 组织类型

| 类型 | 说明 | 层级 |
|------|------|------|
| UNIT | 单位/分公司 | 顶层或中间层 |
| DEPT | 部门 | 叶子节点或中间层 |

### 3.2 组织层级设计建议

```
总公司 (UNIT, level=1)
├── 北京分公司 (UNIT, level=2)
│   ├── 财务部 (DEPT, level=3)
│   ├── 市场部 (DEPT, level=3)
│   └── 技术部 (DEPT, level=3)
├── 上海分公司 (UNIT, level=2)
│   ├── 财务部 (DEPT, level=3)
│   └── 运营部 (DEPT, level=3)
└── 广州分公司 (UNIT, level=2)
    └── 财务部 (DEPT, level=3)
```

### 3.3 组织管理注意事项

> **重要**：
> 1. `path` 字段必须保持唯一，且格式为 `/父path/当前code`
> 2. 删除组织前，需确保该组织下无子组织和用户
> 3. 建议使用稳定的编码（code），避免频繁修改

### 3.4 批量组织导入

如需批量导入组织，可通过数据导入模块上传组织架构 Excel 文件，格式如下：

| code | name | type | parent_code | sort |
|------|------|------|-------------|------|
| HQ | 总公司 | UNIT | | 1 |
| BJ | 北京分公司 | UNIT | HQ | 1 |
| SH | 上海分公司 | UNIT | HQ | 2 |

---

## 4. 用户与权限管理

### 4.1 用户状态

| 状态值 | 说明 |
|--------|------|
| 1 | 正常 |
| 0 | 禁用 |

### 4.2 创建用户流程

```
1. 创建用户 -> 设置基本信息（用户名、密码、姓名）
2. 分配组织 -> 关联所属组织
3. 分配角色 -> 关联角色（可多个）
4. 配置菜单权限 -> 通过角色自动获得
```

### 4.3 密码策略

| 策略 | 当前实现 |
|------|----------|
| 最小长度 | 6位 |
| 加密方式 | BCrypt |
| 重置密码 | 管理员可强制重置 |

### 4.4 用户禁用与恢复

- **禁用**：设置 `status = 0`，用户无法登录
- **恢复**：设置 `status = 1`，用户可重新登录

### 4.5 批量用户导入

通过数据导入模块上传用户 Excel 文件：

| username | realName | password | phone | email | org_code | role_codes |
|----------|----------|----------|-------|-------|----------|------------|
| zhangsan | 张三 | password123 | 13800138000 | zhangsan@example.com | BJ | USER |
| lisi | 李四 | password123 | 13900139000 | lisi@example.com | SH | FINANCE,USER |

---

## 5. 菜单与角色管理

### 5.1 菜单类型

| 类型 | 说明 | 用途 |
|------|------|------|
| CATALOG | 目录 | 一级菜单分组 |
| MENU | 菜单 | 具体功能页面 |
| BUTTON | 按钮 | 页面内操作按钮 |
| LINK | 链接 | 外部链接 |

### 5.2 角色类型

| 类型 | 说明 |
|------|------|
| SYSTEM | 系统角色 |
| BUSINESS | 业务角色 |

### 5.3 预定义角色

| 角色编码 | 角色名称 | 类型 | 说明 |
|----------|----------|------|------|
| ADMIN | 系统管理员 | SYSTEM | 拥有所有权限 |
| USER | 普通用户 | BUSINESS | 基础业务权限 |

### 5.4 创建自定义角色

1. **创建角色**
   ```
   POST /system/role
   {
     "code": "FINANCE",
     "name": "财务主管",
     "type": "BUSINESS",
     "sort": 10
   }
   ```

2. **分配菜单权限**
   - 通过直接关联 sys_role_menu 表
   - 或通过管理界面（预留）

### 5.5 权限继承

- 菜单权限通过角色分配
- 一个用户可拥有多个角色，权限取并集

---

## 6. 会计期间管理

### 6.1 期间状态

| 状态 | 说明 |
|------|------|
| DRAFT | 草稿 |
| ACTIVE | 开启（当前期间） |
| CLOSED | 已关闭 |
| ARCHIVED | 已归档 |

### 6.2 期间管理规则

1. **同一时间只能有一个 ACTIVE 期间**
2. **切换当前期间**：调用 `PUT /system/period/switch/{id}`
3. **关闭期间**：设置 `status = CLOSED`，期间不可再修改

### 6.3 期间初始化

每年1月初，需要创建新的年度期间：

```bash
# 示例：创建2026年12个月份
for month in {1..12}; do
  start_date=$(date -j -f "%Y-%m-%d" "2026-${month}-01" +"%Y-%m-%d")
  end_date=$(date -v+1m -j -f "%Y-%m-%d" "${start_date}" +"%Y-%m-%d")
  end_date=$(date -v-1d -j -f "%Y-%m-%d" "${end_date}" +"%Y-%m-%d")

  curl -X POST "http://localhost:8080/system/period" \
    -H "Content-Type: application/json" \
    -d "{
      \"code\": \"2026-$(printf '%02d' ${month})\",
      \"name\": \"2026年${month}月\",
      \"startDate\": \"${start_date}\",
      \"endDate\": \"${end_date}\",
      \"year\": 2026,
      \"month\": ${month},
      \"status\": \"DRAFT\"
    }"
done
```

### 6.4 月结流程

```
1. 开启新期间 (DRAFT -> ACTIVE)
2. 数据导入
3. 规则校验
4. 关账确认
5. 关闭期间 (ACTIVE -> CLOSED)
```

---

## 7. 流程模板管理

### 7.1 模板状态

| 状态 | 说明 |
|------|------|
| DRAFT | 草稿（可编辑） |
| PUBLISHED | 已发布（不可编辑） |
| DEPRECATED | 已停用 |

### 7.2 模板版本管理

- 每次发布，版本号递增（1 -> 2 -> 3...）
- 已发布模板不可修改，如需变更需归档后创建新版本
- `refCount` 记录被实例化次数

### 7.3 节点类型说明

| 节点类型 | 说明 | 适用场景 |
|----------|------|----------|
| ONE_TIME_TASK | 一次性任务 | 普通业务流程节点 |
| PERIODIC_TASK | 周期任务 | 定期重复的任务 |
| IMPORT_CHECK_TASK | 导入校验任务 | 数据导入检查节点 |
| CLOSE_CONFIRM_TASK | 关账确认任务 | 最终确认节点 |

### 7.4 创建流程模板最佳实践

**Step 1: 设计流程图**

```
开始 -> 数据导入 -> 规则校验 -> 异常处理 -> 关账确认 -> 结束
```

**Step 2: 定义节点配置**

```json
{
  "nodes": [
    {"code": "start", "name": "开始", "type": "ONE_TIME_TASK", "sort": 1},
    {"code": "import", "name": "数据导入", "type": "IMPORT_CHECK_TASK", "assigneeId": 1, "slaHours": 24, "sort": 2, "preNodeCodes": "start"},
    {"code": "check", "name": "规则校验", "type": "ONE_TIME_TASK", "assigneeId": 2, "slaHours": 8, "sort": 3, "preNodeCodes": "import"},
    {"code": "exception", "name": "异常处理", "type": "ONE_TIME_TASK", "assigneeId": 1, "slaHours": 4, "sort": 4, "preNodeCodes": "check"},
    {"code": "confirm", "name": "关账确认", "type": "CLOSE_CONFIRM_TASK", "assigneeId": 1, "slaHours": 2, "sort": 5, "preNodeCodes": "exception"},
    {"code": "end", "name": "结束", "type": "ONE_TIME_TASK", "sort": 6, "preNodeCodes": "confirm"}
  ]
}
```

**Step 3: 拓扑排序验证**

- 系统自动验证节点依赖是否形成闭环
- 必须有唯一的 start 节点（无前置节点）
- 必须有唯一的 end 节点（无后置节点）

### 7.5 模板发布流程

```
1. 创建模板 (DRAFT)
2. 审核模板配置
3. 发布模板 (DRAFT -> PUBLISHED)
4. 如需修改：归档旧模板 (PUBLISHED -> DEPRECATED)
5. 创建新版本模板
```

---

## 8. 规则引擎管理

### 8.1 规则类型

| 类型 | 说明 |
|------|------|
| VALIDATE | 校验规则 |
| CALCULATE | 计算规则 |
| FILTER | 过滤规则 |

### 8.2 规则分类

| 分类 | 说明 |
|------|------|
| BALANCE | 余额校验 |
| PERIOD | 期间校验 |
| AMOUNT | 金额校验 |
| ACCOUNT | 科目校验 |

### 8.3 Aviator 表达式参考

#### 8.3.1 基础运算符

| 表达式 | 说明 |
|--------|------|
| `a > b` | 大于 |
| `a >= b` | 大于等于 |
| `a < b` | 小于 |
| `a <= b` | 小于等于 |
| `a == b` | 等于 |
| `a != b` | 不等于 |
| `a && b` | 逻辑与 |
| `a \|\| b` | 逻辑或 |
| `!a` | 逻辑非 |

#### 8.3.2 数学函数

| 函数 | 说明 | 示例 |
|------|------|------|
| `math.abs(a)` | 绝对值 | `math.abs(amount) > 0` |
| `math.max(a, b)` | 最大值 | `math.max(a, b) > 100` |
| `math.min(a, b)` | 最小值 | `math.min(a, b) < 0` |
| `math.round(a)` | 四舍五入 | `math.round(amount) == amount` |

#### 8.3.3 字符串函数

| 函数 | 说明 | 示例 |
|------|------|------|
| `str.length(s)` | 字符串长度 | `str.length(name) > 0` |
| `str.contains(s, sub)` | 包含子串 | `str.contains(code, "1001")` |
| `str.startsWith(s, prefix)` | 开头匹配 | `str.startsWith(code, "1")` |
| `str.endsWith(s, suffix)` | 结尾匹配 | `str.endsWith(code, "00")` |

#### 8.3.4 集合函数

| 函数 | 说明 | 示例 |
|------|------|------|
| `list.contains(l, item)` | 包含元素 | `list.contains(periods, period)` |
| `list.size(l)` | 集合大小 | `list.size(items) > 0` |

### 8.4 常用规则表达式示例

| 业务场景 | 表达式 |
|----------|--------|
| 余额大于0 | `amount > 0` |
| 金额在范围内 | `amount >= 1000 && amount <= 1000000` |
| 期间有效 | `period >= 1 && period <= 12` |
| 科目代码前缀 | `str.startsWith(accountCode, "1001")` |
| 非空字符串 | `str.length(name) > 0` |
| 日期有效 | `date.after(startDate, endDate) == false` |

### 8.5 规则管理流程

```
1. 创建规则 (DRAFT)
   - 定义表达式
   - 设置错误提示
2. 测试规则
   - 使用 /rule/validate 验证表达式
   - 使用 /rule/execute 测试执行
3. 发布规则 (DRAFT -> PUBLISHED)
4. 执行规则
   - 单规则执行 /rule/execute
   - 批量执行 /rule/execute/batch
   - 规则组执行 /rule/execute/group/{groupCode}
```

---

## 9. 数据导入模板配置

### 9.1 导入类型

| 类型 | 说明 | 文件格式 |
|------|------|----------|
| EXCEL | Excel导入 | .xlsx, .xls |
| CSV | CSV导入 | .csv |
| MANUAL | 手动录入 | - |

### 9.2 Excel导入最佳实践

**文件要求：**
- 格式：.xlsx 或 .xls
- 编码：UTF-8
- 首行：表头（字段映射依据）

**标准格式示例：**

| 期间 | 科目代码 | 科目名称 | 借方金额 | 贷方金额 | 摘要 |
|------|----------|----------|----------|----------|------|
| 2026-04 | 1001 | 现金 | 10000 | 0 | 采购办公用品 |
| 2026-04 | 1002 | 银行存款 | 0 | 10000 | 支付货款 |

### 9.3 CSV导入要求

- 编码：UTF-8
- 分隔符：逗号
- 引号：双引号
- 首行：表头

### 9.4 导入批次状态

| 状态 | 说明 |
|------|------|
| PENDING | 待处理 |
| PROCESSING | 处理中 |
| COMPLETED | 已完成 |
| FAILED | 失败 |
| CANCELLED | 已取消 |

### 9.5 导入流程

```
1. 上传文件 -> POST /data/import/upload
   返回 batchId

2. 执行导入 -> POST /data/import/execute/{batchId}
   解析文件，生成记录

3. 查询进度 -> GET /data/import/progress/{batchId}
   跟踪处理状态

4. 数据校验 -> POST /data/import/validate/{batchId}
   执行规则校验

5. 查看结果 -> GET /data/import/records/{batchId}
             -> GET /data/import/failures/{batchId}
```

---

## 10. 通知配置

### 10.1 通知渠道

| 渠道 | 说明 | 状态 |
|------|------|------|
| EMAIL | 邮件通知 | 需要SMTP配置 |
| WEB_SOCKET | WebSocket通知 | 当前为日志模式 |

### 10.2 邮件配置

在 `application.yml` 中配置：

```yaml
spring:
  mail:
    host: smtp.example.com
    port: 587
    username: noreply@example.com
    password: mail_password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### 10.3 通知模板变量

在模板内容中使用 `{{variable}}` 格式定义变量：

```
尊敬的 {{user}}，请在 {{deadline}} 前完成 {{period}} 的关账操作。
```

发送时传入变量参数：

```json
{
  "templateCode": "TEMPLATE_REMINDER",
  "channel": "EMAIL",
  "recipient": "user@example.com",
  "variables": {
    "user": "张三",
    "deadline": "2026-04-30",
    "period": "2026年4月"
  }
}
```

### 10.4 通知状态

| 状态 | 说明 |
|------|------|
| PENDING | 待发送 |
| SENT | 已发送 |
| FAILED | 发送失败 |
| SKIPPED | 已跳过 |

---

## 11. 系统监控与维护

### 11.1 健康检查

```bash
curl http://localhost:8080/actuator/health
```

**响应：**

```json
{
  "status": "UP"
}
```

### 11.2 日志查看

日志输出到控制台，可通过以下方式查看：

```bash
# 实时查看日志
tail -f /tmp/mc-api.log

# 查看错误日志
grep "ERROR" /tmp/mc-api.log

# 查看最近100行
tail -100 /tmp/mc-api.log
```

### 11.3 API监控

通过 Swagger UI 查看所有 API 端点：

```
http://localhost:8080/swagger-ui.html
```

### 11.4 数据库连接

| 配置项 | 值 |
|--------|-----|
| Host | localhost:5432 |
| Database | monthly_close |
| Username | mcuser |
| Password | mcpassword |

连接池配置（application.yml）：

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
```

---

## 12. 备份与恢复

### 12.1 数据库备份

```bash
# 全量备份
docker exec mc-postgres pg_dump -U mcuser monthly_close > backup_$(date +%Y%m%d).sql

# 压缩备份
docker exec mc-postgres pg_dump -U mcuser monthly_close | gzip > backup_$(date +%Y%m%d).sql.gz
```

### 12.2 数据库恢复

```bash
# 恢复（先删除现有连接）
docker exec mc-postgres psql -U mcuser -d monthly_close < backup_20260424.sql
```

### 12.3 备份策略建议

| 备份类型 | 频率 | 保留时间 |
|----------|------|----------|
| 全量备份 | 每天 | 30天 |
| 增量备份 | 每小时 | 7天 |
| 异地备份 | 每周 | 90天 |

### 12.4 文件备份

重要文件定期备份：

```bash
# 备份上传文件
tar -czf uploads_backup_$(date +%Y%m%d).tar.gz /tmp/uploads/

# 备份配置文件
tar -czf config_backup_$(date +%Y%m%d).tar.gz mc-api/src/main/resources/
```

---

## 13. 安全建议

### 13.1 密码安全

- [ ] 生产环境修改 admin 默认密码
- [ ] 启用强密码策略（当前最小6位）
- [ ] 定期更换密码
- [ ] 不共享账号

### 13.2 JWT配置

当前 JWT 为占位符实现，生产环境需：

1. 配置真实 JWT 密钥
2. 设置 Token 过期时间
3. 实现 Token 刷新机制

### 13.3 数据库安全

- [ ] 修改默认数据库密码
- [ ] 限制数据库访问 IP
- [ ] 启用 SSL 连接
- [ ] 定期更新数据库

### 13.4 API安全

- [ ] 生产环境关闭 Swagger UI
- [ ] 配置 API 限流
- [ ] 实现请求签名验证
- [ ] 记录审计日志

### 13.5 日志安全

- [ ] 不在日志中记录敏感信息（密码、Token）
- [ ] 设置日志轮转策略
- [ ] 配置日志级别（生产环境建议 INFO）

---

## 附录

### A. 数据库表结构速查

| 表名 | 说明 |
|------|------|
| sys_org | 组织树 |
| sys_user | 用户 |
| sys_role | 角色 |
| sys_menu | 菜单 |
| sys_period | 会计期间 |
| wf_template | 流程模板 |
| wf_instance | 流程实例 |
| wf_task | 任务 |
| wf_task_trace | 任务轨迹 |
| import_batch | 导入批次 |
| import_record | 导入记录 |
| rule_config | 规则配置 |
| business_exception | 异常记录 |
| notification_template | 通知模板 |
| notification_record | 通知记录 |

### B. 常用运维命令

```bash
# 启动服务
java -jar mc-api-1.0.0-SNAPSHOT.jar --server.port=8080

# 后台启动
nohup java -jar mc-api-1.0.0-SNAPSHOT.jar > /tmp/mc-api.log 2>&1 &

# 查看进程
ps aux | grep mc-api

# 停止服务
kill $(ps aux | grep mc-api | grep -v grep | awk '{print $2}')

# 重启服务
kill $(ps aux | grep mc-api | grep -v grep | awk '{print $2}') && \
java -jar mc-api-1.0.0-SNAPSHOT.jar --server.port=8080 &
```

### C. 联系支持

如遇问题，请联系：

- 系统管理员：admin@company.com
- 技术支持：support@company.com

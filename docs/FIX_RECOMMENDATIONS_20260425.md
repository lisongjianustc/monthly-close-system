# 月结报表管理系统 - 问题修复方案

> 生成时间：2026-04-25
> 分析对象：测试报告 `docs/test/Test Result/` 下的测试结果

---

## 一、问题总览

| 分类 | 问题数 | 说明 |
|------|--------|------|
| 前端 Bug | 1 | Exception.vue 语法错误 |
| 后端 Bug | 1 | 审计日志表缺失 |
| 前后端字段不一致 | 6 | 导致 500 错误 |

---

## 二、问题 1：Exception.vue 语法错误（前端 Bug）

### 位置
`mc-frontend/src/views/exception/Exception.vue`

### 现象
页面加载时控制台报错，`computed` 函数无法识别。

### 根因
`import { computed }` 语句位于 `<script setup>` 块的**末尾**（第 137 行），但 `computed()` 在第 135 行已被调用：

```javascript
// 第 135 行 - 先使用
const actionTitle = computed(() => currentAction.value === 'handle' ? '处理异常' : '忽略异常')

// 第 137 行 - 后导入（错误）
import { computed } from 'vue'
```

ES Module 必须在使用前导入，此代码在浏览器中会直接抛出 `ReferenceError: computed is not defined`。

### 修复方案
将 `import { computed }` 移动到 `<script setup>` 块的**最顶部**：

```javascript
<script setup>
import { ref, onMounted, computed } from 'vue'  // computed 必须在顶部
import { ElMessage } from 'element-plus'
// ... 其他 import ...

const currentAction = ref('handle')
const actionTitle = computed(() => currentAction.value === 'handle' ? '处理异常' : '忽略异常')  // 现在可以正常使用
</script>
```

---

## 三、问题 2：审计日志表缺失（后端 Bug）

### 位置
`sql/init_schema.sql`

### 现象
所有带 `@Audit` 注解的接口虽然返回成功，但审计日志无法写入数据库，错误被吞掉（AuditAspect 有 try-catch）。

### 根因
`audit_log` 表在 `init_schema.sql` 中**不存在**，导致 AuditAspect 调用 `auditLogService.saveAuditLog()` 时写入失败。

虽然 AuditAspect 捕获了异常（`log.error("审计日志记录失败", e)`），不影响主业务返回，但审计数据永久丢失。

### 修复方案
在 `sql/init_schema.sql` 末尾添加审计日志表：

```sql
-- 审计日志表
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY,
    action VARCHAR(100),
    business_type VARCHAR(50),
    biz_id BIGINT,
    operator_id BIGINT,
    operator_name VARCHAR(100),
    method_name VARCHAR(100),
    class_name VARCHAR(200),
    request_params TEXT,
    execution_time_ms BIGINT,
    ip_address VARCHAR(50),
    remark VARCHAR(500),
    create_by BIGINT,
    create_time TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP
);

COMMENT ON TABLE audit_log IS '审计日志表';
COMMENT ON COLUMN audit_log.action IS '操作类型';
COMMENT ON COLUMN audit_log.business_type IS '业务类型';
COMMENT ON COLUMN audit_log.biz_id IS '业务ID';
COMMENT ON COLUMN audit_log.operator_id IS '操作人ID';
COMMENT ON COLUMN audit_log.operator_name IS '操作人姓名';
```

执行：
```bash
docker exec mc-postgres psql -U mcuser -d monthly_close -f /path/to/init_schema.sql
```

---

## 四、问题 3：前后端字段不一致（导致 500 错误）

### 受影响接口（全部返回 500）

| 接口 | 后端实体字段 | 前端发送字段 | 问题 |
|------|-------------|-------------|------|
| POST /system/org | `code`, `name` | `orgCode`, `orgName` | 字段名不匹配 |
| PUT /system/org | `code`, `name` | `orgCode`, `orgName` | 字段名不匹配 |
| POST /system/role | `code`, `name` | `roleCode`, `roleName` | 字段名不匹配 |
| POST /system/period | `code`, `name` | `periodCode`, `periodName` | 字段名不匹配 |
| POST /rule | `code`, `name` | `ruleCode`, `ruleName` | 字段名不匹配 |
| GET /workflow/template/1 | - | - | 模板节点配置 JSON 解析错误 |

### 根因分析

**场景：以新增组织为例**

1. 前端 Vue form 绑定字段 `orgCode` 和 `orgName`
2. 提交时 axios 发送 JSON：`{ orgCode: "ORG001", orgName: "测试组织", ... }`
3. 后端 `SysOrg` 实体期望字段 `code` 和 `name`
4. Jackson 反序列化时 `orgCode` → 无法匹配 → `SysOrg.code = null`
5. Service 层 `addOrg()` 检查编码唯一性时 `org.getCode()` 为 null
6. 虽然不抛异常，但数据库 `code` 字段是 `NOT NULL`，插入时触发数据库异常
7. MyBatis-Plus 将数据库异常包装为 500 上报

**验证方法**：查看后端启动日志，搜索 `could not execute statement` 或 `null value in column "code"`。

### 修复方案（推荐：统一后端字段名）

为保持 API 兼容性和一致性，建议**修改后端实体**以接收前端字段名，而不是改前端（前端已经在用 `orgCode` 等字段名工作）。

修改 `SysOrg.java`：

```java
// 使用 @JsonProperty 指定前端字段名映射
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_org")
public class SysOrg extends BaseEntity {

    @JsonProperty("orgCode")   // 新增：映射前端 orgCode -> code
    private String code;

    @JsonProperty("orgName")   // 新增：映射前端 orgName -> name
    private String name;

    // ... 其他字段不变 ...
}
```

同样修改其他受影响实体：

| 实体文件 | 需添加的 @JsonProperty |
|---------|----------------------|
| `SysRole.java` | `@JsonProperty("roleCode")` → code, `@JsonProperty("roleName")` → name |
| `SysPeriod.java` | `@JsonProperty("periodCode")` → code, `@JsonProperty("periodName")` → name |
| `RuleConfig.java` | `@JsonProperty("ruleCode")` → code, `@JsonProperty("ruleName")` → name |

> 注意：`RuleConfig` 可能使用不同字段名，需核对实际实体定义。

### 备选修复方案（修改前端）

如果后端实体名称是标准设计，不希望添加 @JsonProperty，则需修改前端 Vue 文件，将所有 `orgCode` 改为 `code`，`orgName` 改为 `name`。

但根据测试结果，前端已经在使用 `orgCode` 等字段，且所有 CRUD 测试已经围绕当前字段名实现，**强烈建议使用 @JsonProperty 方案**。

---

## 五、问题 4：模板详情接口 500 错误

### 接口
`GET /workflow/template/1`

### 根因
模板详情查询返回 500，可能是 `node_config` 字段（JSON）解析失败，或模板关联的节点数据损坏。

### 排查步骤
1. 检查 `wf_template` 表中 `node_config` 字段是否为有效 JSON
2. 查看 `WfTemplateServiceImpl.getTemplateDetail()` 是否有空指针

### 修复建议
```sql
-- 检查模板数据
SELECT id, name, node_config FROM wf_template WHERE id = 1;

-- 如果 node_config 为空或无效，更新为有效 JSON
UPDATE wf_template SET node_config = '[]' WHERE id = 1 AND (node_config IS NULL OR node_config = '');
```

---

## 六、问题 5：流程实例化接口 500 错误

### 接口
`POST /workflow/instance/instantiate`

### 根因
可能原因：
1. 模板未发布（实例化需要 `status = 'PUBLISHED'` 的模板）
2. 期间数据不存在
3. `WfInstanceServiceImpl.instantiate()` 中的业务逻辑错误

### 排查步骤
1. 检查是否有已发布的流程模板：`SELECT * FROM wf_template WHERE status = 'PUBLISHED';`
2. 查看后端日志中 `instantiate` 方法的具体异常堆栈

---

## 七、Dashboard 统计数据未加载

### 位置
`mc-frontend/src/views/dashboard/Dashboard.vue`

### 现象
Dashboard 统计卡片显示硬编码 `0`，未从后端 API 获取真实数据。

### 根因
代码使用了静态值：
```javascript
const stats = ref({
  userCount: 0,
  instanceCount: 0,
  pendingTaskCount: 0,
  exceptionCount: 0
})
```

### 修复方案
在 `onMounted` 中调用后端 API：

```javascript
onMounted(async () => {
  try {
    const [userRes, instanceRes, taskRes, exceptionRes] = await Promise.all([
      axios.get('/system/user/page'),
      axios.get('/workflow/instance/list'),
      axios.get('/workflow/task/list'),
      axios.get('/exception/statistics')
    ])
    stats.value = {
      userCount: userRes.data?.data?.total || 0,
      instanceCount: instanceRes.data?.data?.length || 0,
      pendingTaskCount: taskRes.data?.data?.length || 0,
      exceptionCount: exceptionRes.data?.data?.TOTAL || 0
    }
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
})
```

---

## 八、修复优先级建议

| 优先级 | 问题 | 影响 |
|--------|------|------|
| P0 | Exception.vue import bug | 前端页面报错，功能不可用 |
| P0 | 前后端字段不一致（6个接口） | 核心 CRUD 功能无法使用 |
| P1 | 审计日志表缺失 | 审计数据丢失（不影响业务） |
| P1 | Dashboard 统计未加载 | UI 显示静态 0 |
| P2 | 模板详情/实例化 500 | 流程引擎部分功能不可用 |

---

## 九、修复后验证

修复完成后，建议按以下顺序验证：

1. **前端页面加载**：访问 http://localhost:3000/exception，确认控制台无报错
2. **新增组织**：访问 http://localhost:3000/system/org，点击新增，确认后端返回成功
3. **新增角色/期间/规则**：同上
4. **Dashboard**：确认统计数字与实际数据一致
5. **审计日志**：执行任意带 @Audit 的操作后，查询 `SELECT * FROM audit_log ORDER BY create_time DESC LIMIT 5;`

---

*分析工具：Claude Code*

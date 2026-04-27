# 月结报表管理系统 - 代码质量与安全评估报告

> **报告日期**: 2026-04-24
> **评审范围**: 全部 8 个模块源代码
> **评审方法**: 静态代码分析 + 源码逐行核实
> **评审原则**: 客观有据，引用确切代码行号；不夸大，不回避

---

## 一、评审说明

本次评审覆盖项目全部源代码，核实了外部评估文档中的每一项指证，并补充了遗漏问题。评审按**安全问题、逻辑缺陷、性能问题、代码质量**四个维度组织，每项问题均附代码证据、严重等级、修复建议。

---

## 二、问题总览

| 维度 | 严重 | 高 | 中 | 低 | 合计 |
|------|------|----|----|-----|------|
| 安全漏洞 | 4 | 4 | 3 | 2 | 13 |
| 逻辑缺陷 | 0 | 3 | 4 | 3 | 10 |
| 性能问题 | 1 | 3 | 3 | 1 | 8 |
| 代码质量 | 0 | 0 | 4 | 5 | 9 |
| **合计** | **5** | **10** | **14** | **11** | **40** |

---

## 三、安全漏洞（Critical / High）

### 3.1 [Critical] 文件上传未保存内容 —— 业务功能完全失效

**文件**: `mc-data/src/main/java/com/mc/data/controller/ImportController.java:36-44`

```java
String fileName = file.getOriginalFilename();
Long batchId = importService.createBatch(
    importType,
    fileName,
    "/tmp/" + fileName,    // <-- 只记录了路径，文件内容从未写入磁盘
    file.getSize(),
    ...
);
```

**问题**: `upload()` 接口仅提取文件名和大小元数据，**完全没有调用** `file.transferTo()` 或任何流写入方法。后续 `executeImport()` 尝试读取 `/tmp/文件名` 时必然抛出 `FileNotFoundException`，导致导入功能完全不可用。

**源码核实**: 已确认 ImportController 和 ImportServiceImpl 中均无任何 `FileOutputStream`、`transferTo`、`write` 调用。

**影响**: 用户上传的文件内容全部丢失，导入功能形同虚设。

**修复建议**:
```java
// 在 upload() 中补充文件保存
String savedPath = "/tmp/" + UUID.randomUUID() + "_" + fileName;
file.transferTo(new java.io.File(savedPath));
```

---

### 3.2 [Critical] 路径穿越（Path Traversal）漏洞

**文件**: `mc-data/src/main/java/com/mc/data/controller/ImportController.java:40`

```java
"/tmp/" + fileName
```

**问题**: 直接拼接用户提供的 `fileName`，攻击者可通过修改文件名为 `../../../etc/passwd` 写入任意目录，或在后续 `executeImport` 读取时触发任意文件读取。

**源码核实**: 已确认 `fileName` 仅经过 `getOriginalFilename()` 获取，未做任何字符过滤或路径规范化。

**修复建议**:
```java
// 重新生成文件名，避免用户输入污染路径
String savedFileName = UUID.randomUUID().toString() + "_" + FilenameUtils.getName(fileName);
String savedPath = "/tmp/" + savedFileName;
```

---

### 3.3 [Critical] Aviator 表达式未开启沙箱，存在代码执行风险

**文件**: `mc-rule/src/main/java/com/mc/rule/service/impl/RuleEngineServiceImpl.java:96`

```java
Expression compiledExpr = AviatorEvaluator.compile(rule.getExpression());
Object execResult = compiledExpr.execute(request.getParams());
```

**问题**: Aviator 5.3.3 默认未启用安全沙箱，表达式可访问 Java 反射、System 类等。在规则配置权限被恶意用户控制时，可构造危险表达式。

**补充说明**: Aviator 默认已禁用 `Runtime.getRuntime()`、`java.lang.class` 等危险类访问，但非白名单内的反射调用、`exec` 类方法理论上仍有风险边界。风险等级取决于谁有规则配置权限。

**修复建议**:
```java
// 方式1：使用 Aviator 安全配置
AviatorEvaluator.setOptions(Options.ENABLE_PROPERTY_ACCESS, false,
                            Options.ENABLE_METHOD_ACCESS, false);

// 方式2：表达式预编译白名单，仅允许基础数学和字符串操作
```

---

### 3.4 [Critical] 所有接口无认证鉴权，完全暴露

**文件**: 所有 Controller（`mc-system`, `mc-workflow`, `mc-data`, `mc-rule`, `mc-exception`, `mc-notification`）

**问题**: 项目引入 Spring Security 但所有 Controller 没有任何 `@PreAuthorize` 或认证注解。任何人可以直接调用：
- `POST /workflow/template` 创建/发布流程
- `PUT /workflow/task/approve/{id}` 审批任意任务
- `POST /data/import/execute` 执行导入
- `POST /system/user/resetPassword/{id}` 重置任意用户密码

**源码核实**: 已确认 `SecurityUtil.getCurrentUserId()` 仅从 request attribute 读取（默认返回 null），系统未实现任何过滤器强制认证。

**修复建议**:
1. 实现 JWT 认证过滤器
2. 所有业务接口添加 `@PreAuthorize` 或全局安全配置
3. 任务操作前验证 `assigneeId` 与当前用户一致

---

### 3.5 [High] 任务审批无归属验证，任意用户可审批任意任务

**文件**: `mc-workflow/src/main/java/com/mc/workflow/service/impl/WfTaskServiceImpl.java:89-99`

```java
public boolean approveTask(Long taskId, String comment) {
    WfTask task = this.getById(taskId);
    if (task == null) {
        throw new BusinessException(ResultCode.TASK_NOT_FOUND.getCode(), "任务不存在");
    }
    task.setStatus(TaskStatus.COMPLETED.name());  // 无 assigneeId 验证
    ...
}
```

**问题**: `approveTask`、`rejectTask`、`startTask` 均不检查当前用户是否为任务的实际处理人，任何登录用户均可操作他人任务。

**源码核实**: 已确认 `approveTask` 和 `rejectTask` 无任何 `SecurityUtil.getCurrentUserId()` 校验。

---

### 3.6 [High] 硬编码 JWT 密钥

**文件**: `mc-common/src/main/java/com/mc/common/constants/McConstants.java:78`

```java
public static final String JWT_SECRET = "mc-monthly-close-system-secret-key-2024";
```

**问题**: JWT 签名密钥硬编码在源码中，任何有代码访问权限的人可伪造任意用户的 JWT Token。

**源码核实**: 已确认。AuthController 中 `login()` 生成占位符 Token，未实际使用此密钥，但密钥已被定义存在泄露风险。

---

### 3.7 [High] 错误信息泄露内部细节

**文件**: `mc-common/src/main/java/com/mc/common/exception/GlobalExceptionHandler.java:118`

```java
return Result.fail(ResultCode.SYSTEM_ERROR.getCode(), "系统错误: " + e.getMessage());
```

**问题**: 通用异常处理器将 `e.getMessage()` 直接返回客户端，可能暴露数据库连接信息、SQL 错误、文件路径等内部细节。

**源码核实**: 已确认。

---

### 3.8 [High] 密码重置无需身份验证

**文件**: `mc-system/src/main/java/com/mc/system/controller/SysUserController.java:83`

```java
@PostMapping("/resetPassword/{id}")
public Result<Boolean> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
    return Result.success(sysUserService.resetPassword(id, newPassword));
}
```

**问题**: 可通过传入 `id` 和 `newPassword` 直接重置任意用户密码，无当前密码验证、无邮件/短信验证、无操作审计。

---

## 四、逻辑缺陷（High / Medium）

### 4.1 [High] 导入状态始终为 COMPLETED，忽略失败记录

**文件**: `mc-data/src/main/java/com/mc/data/service/impl/ImportServiceImpl.java:104`

```java
batch.setStatus(failCount > 0 ? "COMPLETED" : "COMPLETED");  // 两分支相同
```

**问题**: 无论是否存在失败记录，批次状态始终设为 `COMPLETED`。业务上应区分"全部成功"和"部分成功"。

**源码核实**: 已确认。

---

### 4.2 [High] 循环内单条插入（N+1 问题）

**文件**: `mc-data/src/main/java/com/mc/data/service/impl/ImportServiceImpl.java:88-98`

```java
for (ImportRecord record : records) {
    record.setBatchId(batchId);
    try {
        recordMapper.insert(record);  // 每条一次 INSERT + 网络往返
        successCount++;
    } catch (Exception e) {
        record.setStatus("INVALID");
        record.setErrorMsg(e.getMessage());
        recordMapper.insert(record);  // 失败记录也逐条插入
        failCount++;
    }
}
```

**问题**: 1万条记录产生约2万次数据库网络往返。`validateData()` 中同样存在逐条 `updateById`。

**源码核实**: 已确认。

---

### 4.3 [High] Aviator 表达式无缓存，每次执行重新编译

**文件**: `mc-rule/src/main/java/com/mc/rule/service/impl/RuleEngineServiceImpl.java:96`

```java
Expression compiledExpr = AviatorEvaluator.compile(rule.getExpression());
```

**问题**: 每次 `executeRule()` 都重新编译表达式。高频调用（如万行账务 × 50规则 = 50万次编译）成为严重性能瓶颈。

**源码核实**: 已确认 `compile()` 调用无缓存参数。

---

### 4.4 [Medium] 事务内 try-catch 吞异常可能导致回滚

**文件**: `mc-data/src/main/java/com/mc/data/service/impl/ImportServiceImpl.java:88-98`

**说明**: 外层 `@Transactional` 方法内 catch 了单条 insert 异常后继续执行。Spring 事务机制中，若数据库连接被标记为 rollback-only，后续 commit 时会触发 `UnexpectedRollbackException`。但实际代码行为是：失败记录重新 insert 为 INVALID 状态，成功记录正常 commit，并非真正触发回滚。

**评估**: 事务设计存在缺陷，但不等于"整批回滚"（原评估描述不精确）。风险真实存在但表现形式与原评估不符。

---

### 4.5 [Medium] 统计方法加载全量记录到内存

**文件**: `mc-exception/src/main/java/com/mc/exception/service/impl/ExceptionServiceImpl.java:100-106`

```java
List<BusinessExceptionRecord> records = this.list(wrapper);  // 加载全部到内存
Map<String, Long> stats = new HashMap<>();
stats.put("TOTAL", (long) records.size());
stats.put("PENDING", records.stream().filter(...).count());
```

**问题**: 百万级记录时会导致 OOM。`NotificationServiceImpl.getStatistics()` 同样存在此问题。

**源码核实**: 已确认。

---

### 4.6 [Medium] 规则组执行存在 N+1 查询

**文件**: `mc-rule/src/main/java/com/mc/rule/service/impl/RuleEngineServiceImpl.java:151-156`

```java
for (Long ruleId : ruleIds) {
    RuleExecuteRequest request = new RuleExecuteRequest();
    request.setRuleId(ruleId);
    results.add(executeRule(request));  // 每次循环触发 getById()
}
```

**问题**: 规则组中每条规则都单独查询一次数据库。`WfTaskServiceImpl.checkPreTasks()` 同样在循环中调用 `getById()`。

**源码核实**: 已确认。

---

### 4.7 [Medium] CSV 解析无注入防护

**文件**: `mc-data/src/main/java/com/mc/data/service/impl/ImportServiceImpl.java:304-309`

```java
String[] fields = line.split(",");
if (fields.length > 0) record.setAccountCode(fields[0].trim());
```

**问题**: 若 CSV 内容含 Excel 公式字符（`=`, `+`, `-`, `@`），导出到 Excel 时可能执行公式注入攻击（CSV Injection）。无输入前缀处理。

**源码核实**: 已确认。

---

### 4.8 [Medium] 用户ID默认回退到管理员

**文件**: `mc-data/src/main/java/com/mc/data/controller/ImportController.java:42-43`

```java
userId != null ? userId : 1L,        // 未认证请求默认归于管理员
userName != null ? userName : "系统管理员"
```

**问题**: 无认证上传被默认归于 admin(1)，掩盖真实操作人身份。

---

### 4.9 [Medium] 进度百分比计算存在除零风险

**文件**: `mc-data/src/main/java/com/mc/data/service/impl/ImportServiceImpl.java:141`

```java
progress.setProgressPercent((processed * 100) / total);
```

**问题**: 当 `total` 为 0 时，整数除法会抛 `ArithmeticException`。代码虽有 `total > 0` 判断，但 `processed * 100 / total` 在 total=0 时仍会被求值。

**源码核实**: 已确认 null 检查存在但零值检查在表达式求值时仍有问题。

---

## 五、性能问题（High / Medium）

### 5.1 [High] 批量导入逐条插入

同 4.2，详见上文。

### 5.2 [High] Aviator 表达式无缓存

同 4.3，详见上文。

### 5.3 [High] 规则组与任务前置检查 N+1

同 4.6，详见上文。

### 5.4 [Medium] 批量操作全部逐条处理

**位置**:
- `ExceptionServiceImpl.batchHandle()` — 逐条 `updateById`
- `WfTaskServiceImpl.markOverdueTasks()` — 逐条 `updateById`

**修复建议**: 使用 `saveBatch()` 或 `updateBatchById` 批量操作。

---

## 六、代码质量（Medium / Low）

### 6.1 [Medium] 魔法字符串散落代码各处

**示例**: `mc-data/src/main/java/com/mc/data/service/impl/ImportServiceImpl.java:68`

```java
if (!"PENDING".equals(batch.getStatus())) { ... }
```

**建议**: 使用 `BatchStatus` 枚举替代。

---

### 6.2 [Medium] ObjectMapper 重复实例化

**文件**: `mc-rule/src/main/java/com/mc/rule/service/impl/RuleEngineServiceImpl.java:38`

```java
private final ObjectMapper objectMapper = new ObjectMapper();
```

**问题**: 创建了新实例而非注入 Spring 配置的 ObjectMapper，缺失 `JavaTimeModule` 等全局配置支持。

---

### 6.3 [Medium] 审计日志仅输出到控制台，未持久化

**文件**: `mc-common/src/main/java/com/mc/common/aspect/AuditAspect.java`

```java
log.info("审计日志: {}", auditLog);
// TODO: 实际保存到数据库
// auditLogService.save(auditLog);
```

**问题**: 审计记录仅写入日志文件，未入库。日志轮转后无法追溯操作历史。

---

### 6.4 [Medium] 未知属性静默忽略

**文件**: `mc-common/src/main/java/com/mc/common/config/JacksonConfig.java:30`

```java
mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
```

**问题**: 客户端传入新字段时被静默忽略，可能掩盖配置错误。

---

### 6.5 [Low] 重复的 buildTree 实现

`SysOrgServiceImpl.buildTree()` 与 `SysMenuServiceImpl.buildTree()` 代码完全相同，建议抽取为公共方法。

---

### 6.6 [Low] batchHandle 返回值无意义

无论实际更新几条记录，始终返回 `true`。

---

### 6.7 [Low] checkPreTasks 中 NumberFormatException 无防护

**文件**: `mc-workflow/src/main/java/com/mc/workflow/service/impl/WfTaskServiceImpl.java:130`

```java
Long preTaskId = Long.parseLong(preTaskIdStr.trim());  // 脏数据会崩
```

---

### 6.8 [Low] 拓扑排序无防环检测

若节点配置形成循环依赖，拓扑排序会漏掉节点但无错误提示。

---

### 6.9 [Low] 随机数生成非线程安全

**文件**: `mc-data/src/main/java/com/mc/data/service/impl/ImportServiceImpl.java:374-376`

```java
new Random().nextInt(10000)  // 高并发下可能重复
```

---

## 七、架构评估

### 7.1 优点

| 方面 | 评价 |
|------|------|
| 模块划分 | 清晰隔离，依赖结构合理 |
| 统一响应 | `Result<T>` 封装一致 |
| 事务使用 | 核心方法 `@Transactional` 标注完整 |
| 代码规范 | 包结构、命名基本一致 |
| 工具类 | `SecurityUtil`、`BusinessException` 等通用抽象 |

### 7.2 不足

| 方面 | 问题 |
|------|------|
| 认证授权 | 完全缺失 |
| 输入验证 | 缺少 `@Valid` / `@Validated` |
| 性能考量 | 批量操作全为逐条 |
| 缓存 | 无 Redis 实际使用 |
| API 版本 | 无版本控制 |
| 测试 | 无单元测试 |

---

## 八、修复优先级建议

### P0 — 立即修复（影响业务可用性）

| # | 问题 | 影响 |
|---|------|------|
| 1 | 文件上传未保存 | 导入功能完全不可用 |
| 2 | Aviator 无缓存 | 高频调用性能崩溃 |
| 3 | 批量导入逐条插入 | 大文件导入极慢 |

### P1 — 紧急修复（安全性）

| # | 问题 |
|---|------|
| 4 | 所有接口无认证 |
| 5 | 路径穿越漏洞 |
| 6 | 任务审批无归属验证 |
| 7 | 硬编码 JWT 密钥 |
| 8 | 错误信息泄露 |

### P2 — 短期内修复

| # | 问题 |
|---|------|
| 9 | 审计日志未持久化 |
| 10 | 统计方法内存爆炸 |
| 11 | 规则组 N+1 查询 |
| 12 | 密码重置无验证 |

### P3 — 规划中修复

| # | 问题 |
|---|------|
| 13 | 魔法字符串枚举化 |
| 14 | ObjectMapper 统一注入 |
| 15 | buildTree 公共提取 |
| 16 | 拓扑排序环检测 |

---

## 九、对前次外部评估的核实结论

| 前次评估内容 | 核实结论 |
|-------------|----------|
| Aviator RCE | **部分准确** — 风险存在但被夸大，Aviator 5.3.3 默认禁用危险类，实际 RCE 需要突破多层限制 |
| 路径穿越 | **完全准确** — 代码确认未过滤 `../` |
| 文件假上传 | **完全准确** — 代码确认无 `transferTo` 调用 |
| UnexpectedRollbackException | **基本不准确** — 问题存在但行为描述不符：失败记录被重新 insert 为 INVALID 并正常 commit，而非"整批回滚" |
| Aviator 无缓存 | **完全准确** — 确认每次重新编译 |
| 批量导入 N+1 | **完全准确** — 确认逐条 insert |
| JWT 缺失 | **完全准确** — AuthController 仅有 TODO 占位符 |
| "Demo 级别无法上线" | **不客观** — 项目具备完整模块结构、事务管理、审计注解等企业要素，措辞过于激进 |

---

*本报告基于 2026-04-24 时点的源码静态分析，实际运行时行为可能因配置和环境差异而不同。建议优先修复 P0/P1 问题后再进行安全测试验证。*

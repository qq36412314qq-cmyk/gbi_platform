# HR模块开发任务计划 v1.1

> 文档版本：v1.1  
> 生成日期：2026-09-23  
> 需求来源：《人力资源模块流程分析与实现方案 v1.4》  
> 关联方案：《薪酬社保核算体系升级方案-v2.0》《多城市薪酬社保核算系统升级设计规范v1.0》

---

## 零、实体字段核验结果（执行前必读）

> 以下字段核验基于 2026-09-23 实际读取的 Java 实体文件，**禁止假设字段不存在而盲目编写 ALTER 脚本**。

### 0.1 HrEmployee.java 已存在字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `cityCode` | String | 工作城市编码，关联 sys_city |
| `industryCode` | String | 所属行业编码，关联 sys_industry |
| `socialDeclareBase` | BigDecimal | 社保申报基数（年度锁定） |
| `housingFundDeclareBase` | BigDecimal | 公积金申报基数（年度锁定） |
| `baseEffectiveYear` | String | 基数生效年度（如"2026"） |
| `socialSecurityBase` | BigDecimal | 旧版社保基数字段（与 socialDeclareBase 并存，需确认业务用途） |
| `salaryGradeCode` | String | 薪酬级别编码 |
| `photoFileId` | Long | 免冠照片文件ID |
| `attachmentContent` | String | 附件内容（富文本HTML） |

**结论**：P0-01、P0-02、P0-03 涉及 `hr_employee` 表的 ALTER 全部**不需要**，字段已存在。

### 0.2 HrSalaryRule.java 已存在字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `socialSecurityRate` | BigDecimal | 社保个人比例(%)，NULL 表示继承全局参数 |
| `housingFundRate` | BigDecimal | 公积金个人比例(%)，NULL 表示继承全局参数 |

**结论**：P0-09 三级费率优先级的逻辑接入只需修改 `HrSocialCalcServiceImpl`，**不需要** ALTER 表或新增字段。

### 0.3 HrSalaryArchive.java 已存在字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `sourceType` | Integer | 来源类型（0=手动 1=入职模板 2=转正 3=调岗） |
| `isCurrent` | Integer | 是否当前生效版本 |
| `versionNo` | Integer | 版本号 |
| `ruleId` | Long | 薪资规则ID |
| `gradeCode` | String | 薪酬级别编码 |

**结论**：P0-02 的 sourceType 确认已完成，无需 ALTER。

### 0.4 HrEntryApply.java 缺失字段（需新增）

| 字段名 | 类型 | 需新增 |
|--------|------|--------|
| `cityCode` | String | ✅ P0-01 |
| `industryCode` | String | ✅ P0-03 |
| `socialDeclareBase` | BigDecimal | ✅ P0-03 |
| `housingFundDeclareBase` | BigDecimal | ✅ P0-03 |
| `baseEffectiveYear` | String | ✅ P0-03 |
| `salaryRuleId` | Long | ✅ P0-04 |
| `salaryGradeCode` | String | ✅ P0-04 |
| `autoCreateSalaryArchive` | TINYINT | ✅ P0-04 |
| `autoSubmitSalaryAudit` | TINYINT | ✅ P0-04 |

**结论**：以上字段在 `HrEntryApply.java` 中均不存在，P0-01/P0-03/P0-04 的数据库 ALTER 仅针对 `hr_entry_apply` 表。

---

## 一、开发约束总则

### 1.1 强制技术约束

| 约束项 | 规则 |
|--------|------|
| 后端技术栈 | Spring Boot 3 + MyBatis-Plus + JDK 21，禁止升级框架版本 |
| 前端技术栈 | Vue3 + TypeScript + Element Plus + wangEditor，禁止引入第三方UI库 |
| 工作流引擎 | HR 模块审批回调通过 `HrTransferService.onXxxApproved()` 直接方法调用触发（如 `onEntryApproved()` / `onRegularApproved()`），**不实现 `FlowBizHandler` 接口**；禁止重新实现工作流逻辑 |
| 多租户隔离 | 所有业务 SQL 必须携带 `company_id`；从 `UserContext.getLoginUser()` 获取租户和操作人（字段为 `getCompanyId()` / `getUserId()`），复用已有工具类 |
| 统一返回 | Controller 层使用项目已有 `Result<T>`（`Result.success(data)` / `Result.error(msg)`）；**Service 层返回原始类型**（Long / PageVO / void），不在 Service 层包裹 Result |
| 异常处理 | Service 层异常使用 `BizException`；**审批回调方法内部 try-catch 包裹业务逻辑，异常仅记录日志，不向上抛**，避免打断工作流主线程 |
| 审批回调幂等 | 检查 `apply.getStatus() != CommonConst.APPLY_STATUS_AUDITING` 时直接 return；**不使用 exec_status/exec_msg/exec_time 字段**（该字段在当前项目中不存在），幂等判断以申请单 status 字段为准 |
| 审计日志 | 复用 `AuditLogUtil`，模块名使用 `CommonConst.MODULE_HR_*` 常量；新增操作必须记录操作人、时间、业务ID、操作类型 |
| 薪资版本管理 | `version_no` + `is_current` 双字段控制；旧版本置历史，乐观锁 `version` 字段防并发 |
| 历史数据保护 | 补调薪只新增记录，**禁止 UPDATE** 已发放的 `hr_salary_month` 数据 |
| 存量代码融合 | 存在的类不做全量重写，只做字段新增、方法新增；`createFromEntry()` 等已有逻辑做增强，不重写整个方法 |
| 权限常量 | 新增前必须先审查 `PermissionConst.java`，**禁止虚构常量名**；实际已定义常量见文档第四节 P2-01 |
| 第三方依赖 | **禁止引入任何新的 Maven 或 npm 第三方依赖包** |
| 实体字段核验 | **每个任务开始前，必须先读取对应 Java 实体文件（HrEntryApply.java / HrEmployee.java / HrSalaryArchive.java 等），确认字段是否存在后再规划 ALTER 脚本**，禁止假设字段不存在 |
| 输出粒度 | 每次只完成一个最小原子任务，输出物清单明确 |

### 1.2 每轮输出交付规范

> ⚠️ **强制前置步骤**：执行任何数据库 ALTER 脚本前，**必须先读取目标 Java 实体文件**，确认字段是否已存在。若字段已存在则跳过 ALTER，直接在实体中补充 `@TableField` 注解映射即可。

每一轮开发任务必须输出以下清单（按需裁剪，标注"不需要"时写明原因）：

```
【输出物清单】
□ 数据库脚本（ALTER，禁止CREATE TABLE/DROP；执行前须先读实体确认字段）
□ Java 实体/枚举/DTO（如有新增）
□ Mapper 接口 + XML（如有新增）
□ Service 方法 + 单元测试（Service 方法不包裹 Result<T>，直接返回原始类型）
□ Controller 接口（如有新增接口；HR 模块审批回调不新增 Controller）
□ 前端 TS 类型定义（如有新增类型）
□ 前端 Vue 组件修改（如有变更）
□ 迁移数据脚本（如涉及存量数据）
```

---

## 二、P0 任务清单（阻断业务闭环，优先级最高）

### P0-01：入职申请表单增加 cityCode 字段

**业务目标**：入职申请表单补充就职城市字段，解决社保核算时无法匹配城市费率的根本问题。

**关联需求**：文档 11.6.1、11.6.2、11.6.3

**开发范围**：

| 分层 | 变更内容 |
|------|----------|
| 数据库 | **HrEmployee.java 已存在 `cityCode` / `industryCode` / `socialDeclareBase` / `housingFundDeclareBase` / `baseEffectiveYear` 字段，无需 ALTER hr_employee**；仅需确认 hr_entry_apply 表中 city_code 字段是否存在（当前 HrEntryApply.java 未定义该字段，需新增） |
| 实体 | `HrEntryApply.java` 新增 `cityCode / industryCode / socialDeclareBase / housingFundDeclareBase / baseEffectiveYear` 字段 |
| Service | `HrEmployeeServiceImpl.createFromEntry()` 增强：从申请单读取上述字段写入 HrEmployee（**hr_employee 已有对应字段，无需 ALTER**） |
| 前端 Vue | `transfer/index.vue` 表单新增就职城市选择器，联动 sys_city 列表接口 |

**接口契约**：

```
GET  /api/hr/city/list          → 获取城市列表（sys_city），供前端下拉选择
```

**存量融合约束**：
- 不修改已有字段的任何逻辑
- `city_code` 字段设置 `NOT NULL DEFAULT ''`，存量数据无需迁移
- 表单校验：cityCode 必填，提交时校验不为空

**单元测试**：
- 用例1：提交入职申请时 cityCode 为空 → 抛出 BizException（校验失败）
- 用例2：提交入职申请时 cityCode 有效 → 成功写入 hr_entry_apply
- 用例3：createFromEntry 增强后，员工档案 cityCode 正确继承自申请单

**数据库脚本**：
```sql
-- hr_entry_apply 新增 city_code 字段
ALTER TABLE hr_entry_apply
  ADD COLUMN city_code VARCHAR(32) NOT NULL DEFAULT '' COMMENT '就职城市编码，关联sys_city' AFTER employment_type;
```

**不需要**：Mapper XML（MyBatis-Plus LambdaQueryWrapper 足够，无需手写 SQL）；Controller（复用已有 HrEntryApplyController）。

---

### P0-02：入职审批通过后自动创建薪资档案（含 cityCode 传递）

**业务目标**：入职申请审批通过后，自动在 `hr_salary_archive` 创建 V1 版本薪资档案，并正确传递 cityCode 至员工档案。

**关联需求**：文档 11.3、11.6.3

**开发范围**：

| 分层 | 变更内容 |
|------|----------|
| Service | `HrEmployeeServiceImpl.createFromEntry()` 增强：写入 cityCode、industryCode 等字段到 HrEmployee（**HrEmployee.java 已有 cityCode/industryCode/socialDeclareBase/housingFundDeclareBase/baseEffectiveYear 字段，无需 ALTER hr_employee**）；调用新增方法创建初始薪资档案 |
| Service | 新增 `HrSalaryArchiveService.createInitialArchive(HrEntryApply apply, Long employeeId)` |
| 实体 | `HrSalaryArchive.java` 确认 sourceType 字段存在（当前已存在，值为 0=手动/1=入职模板/2=转正/3=调岗） |
| 审批回调 | 在 `HrTransferService.onEntryApproved()` 方法末尾调用上述逻辑（**非 FlowBizHandler 接口**，是直接方法调用） |

**接口契约**：无新增 HTTP 接口（纯内部 Service 逻辑增强）。

**存量融合约束**：
- `createFromEntry()` 原有逻辑完整保留，只做增强
- 新增字段使用 `@TableField` 注解，存量数据无需迁移（DEFAULT NULL 可接受）
- 薪资档案版本控制：`is_current` 初始化为 1，`version_no` 从当前最大值 +1
- 并发安全：使用 `SELECT MAX(version_no) FROM hr_salary_archive WHERE employee_id=? FOR UPDATE` 乐观锁

**单元测试**：
- 用例1：入职申请包含 cityCode，审批通过后员工档案 cityCode 正确写入
- 用例2：入职申请未关联薪资规则，薪资档案以 basicSalary 手动填写值创建，gradeCode/ruleId 置空标记待配置
- 用例3：重复审批回调幂等测试：`apply.getStatus() != CommonConst.APPLY_STATUS_AUDITING` 时直接 return，不重复创建薪资档案
- 用例4：`socialDeclareBase` 为 null 时，默认使用 basicSalary 作为社保申报基数

**数据库脚本**：
```sql
-- ⚠️ HrEmployee.java 已存在 cityCode/industryCode/socialDeclareBase/housingFundDeclareBase/baseEffectiveYear 字段
-- 以下 ALTER 仅针对 hr_entry_apply 表（HrEntryApply.java 未定义这些字段）
ALTER TABLE hr_entry_apply
  ADD COLUMN city_code VARCHAR(32) NOT NULL DEFAULT '' COMMENT '就职城市编码，关联sys_city' AFTER employment_type,
  ADD COLUMN industry_code VARCHAR(32) COMMENT '所属行业编码，关联sys_industry' AFTER city_code;
-- social_declare_base / housing_fund_declare_base / base_effective_year 同理，需先确认表结构后再执行
```

-- hr_salary_archive 确认 source_type 字段存在
-- 若不存在，新增：
-- ALTER TABLE hr_salary_archive ADD COLUMN source_type TINYINT DEFAULT 0 COMMENT '来源类型 0手动 1入职模板 2转正 3调岗';
```

**不需要**：Controller（复用已有薪资档案 CRUD 接口）。

---

### P0-03：入职申请表单增加社保申报基数字段

**业务目标**：入职申请时主动填写社保/公积金申报基数，审批通过后写入员工档案，供月度核算使用。

**关联需求**：文档 11.6.2、11.6.4

**开发范围**：

| 分层 | 变更内容 |
|------|----------|
| 数据库 | `hr_entry_apply` 新增 `industry_code / social_declare_base / housing_fund_declare_base / base_effective_year`（**HrEmployee.java 已有对应字段，无需 ALTER hr_employee**） |
| 实体 | `HrEntryApply.java` 新增对应字段 |
| Service | `createFromEntry()` 增强：将上述字段写入 HrEmployee（**目标字段已存在**） |
| 前端 Vue | `transfer/index.vue` 表单新增基数输入区域，选择城市后自动推荐该城市社保基数上下限提示 |

**接口契约**：
```
GET  /api/hr/social-param/config?cityCode={code}  → 获取指定城市当前生效的社保参数配置（含基数上下限）
```

**存量融合约束**：
- 新增字段允许 NULL，不影响已有逻辑
- 前端：选择城市后异步查询该城市社保参数，填充基数上下限提示文字（不强制预填值）

**单元测试**：
- 用例1：选择城市后，前端能正确加载该城市社保参数上下限提示
- 用例2：提交入职申请，社保申报基数正确写入 HrEntryApply
- 用例3：createFromEntry 后，HrEmployee 对应基数字段（socialSecurityBase / socialDeclareBase，**以实体确认为准**）正确继承

**数据库脚本**：
```sql
ALTER TABLE hr_entry_apply
  ADD COLUMN industry_code VARCHAR(32) COMMENT '所属行业编码，关联sys_industry' AFTER city_code,
  ADD COLUMN social_declare_base DECIMAL(12,2) COMMENT '社保申报基数' AFTER industry_code,
  ADD COLUMN housing_fund_declare_base DECIMAL(12,2) COMMENT '公积金申报基数' AFTER social_declare_base,
  ADD COLUMN base_effective_year VARCHAR(8) COMMENT '基数生效年度' AFTER housing_fund_declare_base;
```

**不需要**：新增 Controller 接口（复用社保参数查询已有接口，若无则新增一个轻量 GET 接口）。

---

### P0-04：入职申请表单增加薪资规则选择与自动建档控制

**业务目标**：入职申请时可选薪资规则，审批通过后自动创建薪资档案。

**关联需求**：文档 11.6.4

**开发范围**：

| 分层 | 变更内容 |
|------|----------|
| 数据库 | `hr_entry_apply` 新增 `salary_rule_id / salary_grade_code / auto_create_salary_archive / auto_submit_salary_audit` |
| 实体 | `HrEntryApply.java` 新增对应字段 |
| Service | `createFromEntry()` 增强：根据 salary_rule_id 查找薪资规则，填充 gradeCode/ruleId/ruleName 到薪资档案 |
| 前端 Vue | `transfer/index.vue` 新增薪资与档案生成区域（可折叠），选择岗位后自动推荐薪资规则 |

**接口契约**：
```
GET  /api/hr/salary-rule/list?postId={id}  → 根据岗位ID获取匹配的薪资规则列表
GET  /api/hr/salary-rule/{id}              → 获取单条薪资规则详情（含各项薪资构成）
```

**存量融合约束**：
- `auto_create_salary_archive` 默认值为 1（开启），`auto_submit_salary_audit` 默认值为 0（关闭）
- 若未选择薪资规则，使用申请单 basicSalary 手动填写值，gradeCode/ruleId 置空，薪资档案标记为"待配置"

**单元测试**：
- 用例1：选择薪资规则后提交，审批通过创建薪资档案时 ruleId/gradeCode 正确写入
- 用例2：未选择薪资规则，审批通过后薪资档案 ruleId 为空，is_current=1 正常生效
- 用例3：`auto_create_salary_archive=0` 时，不创建薪资档案，员工档案 salary_archive_status=0

**数据库脚本**：
```sql
ALTER TABLE hr_entry_apply
  ADD COLUMN salary_rule_id BIGINT COMMENT '薪资规则ID（从模板自动匹配）' AFTER base_effective_year,
  ADD COLUMN salary_grade_code VARCHAR(32) COMMENT '薪酬级别编码' AFTER salary_rule_id,
  ADD COLUMN auto_create_salary_archive TINYINT DEFAULT 1 COMMENT '是否自动创建薪资档案 0否 1是' AFTER salary_grade_code,
  ADD COLUMN auto_submit_salary_audit TINYINT DEFAULT 0 COMMENT '是否自动提交薪资档案审批 0否 1是' AFTER auto_create_salary_archive;
```

---

### P0-05：转正/调岗/离职审批回调自动更新员工档案

**业务目标**：三类审批通过后，自动更新员工档案对应字段，避免 HR 手动维护。

**关联需求**：文档四 P0-02

**开发范围**：

| 分层 | 变更内容 |
|------|----------|
| Service | 在现有 `HrTransferService` 接口中新增 `onRegularApproved()` / `onTransferApproved()` / `onResignApproved()` 方法增强（**不新建独立 CallbackService，不实现 FlowBizHandler 接口**）；转正通过后更新员工状态、试用期结束日期；调岗通过后更新 orgId/postId；离职通过后更新员工状态为已离职 |
| 审批回调 | 在 `HrTransferServiceImpl` 中已有 `onRegularApproved()` / `onTransferApproved()` / `onResignApproved()` 方法基础上做增强 |

**接口契约**：无新增 HTTP 接口。

**存量融合约束**：
- 复用已有 `HrEmployeeServiceImpl.updateById()`，不重写更新逻辑
- 每个回调方法内部必须捕获所有异常（try-catch），记录日志，**不向上抛**
- 幂等判断：检查员工当前状态是否已为目标状态，已一致则直接 return

**单元测试**：
- 用例1：转正审批通过，员工状态从试用期→正式，regularDate 写入
- 用例2：调岗审批通过，orgId/postId 更新，历史数据保留
- 用例3：离职审批通过，员工状态→已离职，拦截后续薪资生成
- 用例4：重复回调幂等——员工状态已是目标状态，不重复执行
- 用例5：异常容错——updateById 抛异常时，方法内部 try-catch 捕获并记录日志，不向上抛异常

**不需要**：数据库变更（目标字段已存在于 hr_employee）。

---

### P0-06：薪资档案审批通过后自动激活新版本

**业务目标**：薪资档案审批通过后，自动将新版本 is_current=1，旧版本 is_current=0。

**关联需求**：文档四 P0-03

**开发范围**：

| 分层 | 变更内容 |
|------|----------|
| Service | 在现有 `HrTransferService` 中新增 `onSalaryArchiveApproved()` 方法（**不实现 FlowBizHandler 接口**）：将新版本 is_current=1，旧版本 is_current=0 |
| 审批回调 | 在 `HrTransferServiceImpl` 中实现 `onSalaryArchiveApproved()` 方法 |

**接口契约**：无新增 HTTP 接口。

**存量融合约束**：
- 使用事务：`UPDATE hr_salary_archive SET is_current=0 WHERE employee_id=? AND is_current=1` + `UPDATE ... SET is_current=1 WHERE id=?`
- 乐观锁：检查待激活版本 version 字段与数据库一致，否则抛 BizException
- 审批驳回：新版本保留 is_current=0，不覆盖原生效版本

**单元测试**：
- 用例1：V1 生效（is_current=1），审批通过 V2 → V1 is_current=0，V2 is_current=1
- 用例2：驳回 V2 → V2 is_current 保持 0，V1 不受影响
- 用例3：重复审批回调幂等——is_current 已是 1 的版本，直接 return

**数据库脚本**：不需要（目标字段已存在）。

---

### P0-07：批量调薪审批通过后执行调薪逻辑

**业务目标**：批量调薪审批通过后，自动为涉及员工创建新薪资档案版本。

**关联需求**：文档四 P0-04

**开发范围**：

| 分层 | 变更内容 |
|------|----------|
| Service | 在现有 `HrTransferService` 中新增 `onBatchAdjustApproved()` 方法（**不实现 FlowBizHandler 接口**）：遍历调薪单中的员工列表，逐个创建新薪资档案版本 |

**接口契约**：无新增 HTTP 接口。

**存量融合约束**：
- 遍历调薪单中的员工列表，逐个调用 `HrSalaryArchiveService.createVersion()` 创建新版本
- 单员工失败不影响其他员工：每个员工独立事务，失败记录日志，继续处理下一个
- 版本控制：newVersionNo = max(current version) + 1，is_current=0 等待审批激活

**单元测试**：
- 用例1：调薪单涉及 3 名员工，全部成功创建新版本
- 用例2：调薪单涉及 3 名员工，1 名失败（员工已离职），其余 2 名正常
- 用例3：重复审批回调幂等——新版本已存在，直接 return

**不需要**：数据库变更（调薪单表已存在）。

---

### P0-08：月度薪资生成空值保护

**业务目标**：月度薪资生成时，检测无薪资档案的员工，抛出明确告警，不静默跳过。

**关联需求**：文档 11.9.3、四 P0-05

**开发范围**：

| 分层 | 变更内容 |
|------|----------|
| Service | `HrSalaryServiceImpl.generateMonth()` 增加空值检测：无薪资档案员工列表收集 + 告警 |
| 前端 | 生成结果页展示缺失档案员工列表，引导 HR 处理 |

**接口契约**：
```
GET  /api/hr/salary/month/pending-archives?month={yyyy-MM}  → 获取指定月份缺少薪资档案的员工列表
```

**存量融合约束**：
- 原有 generateMonth 逻辑完整保留，只在其前面增加检测阶段
- 检测到缺失档案时，抛出 BizException，不执行后续核算（避免生成空数据）
- 提供独立查询接口，不影响正常生成流程

**单元测试**：
- 用例1：所有员工均有薪资档案 → 正常生成
- 用例2：存在无薪资档案员工 → 抛出 BizException，消息中包含缺失员工名单
- 用例3：特殊人员（auto_create_salary_archive=0）→ 不参与检测，正常生成

**不需要**：数据库变更。

---

### P0-09：薪资模板社保/公积金比例参与月度核算

**业务目标**：月度薪资核算时，优先从薪资模板（hr_salary_rule）读取社保公积金比例，再回退到城市参数和 sys_config。

**关联需求**：文档 11.7

**开发范围**：

| 分层 | 变更内容 |
|------|----------|
| Service | `HrSocialCalcServiceImpl.calculateSocialAmount()` 增加三级费率优先级逻辑 |
| 工具类 | 新增 `SocialRateResolver` 工具类，封装三级查找逻辑 |
| sys_config | 新增集团级默认社保比例兜底配置 |

**三级费率优先级**：
```
1. hr_salary_rule.social_security_rate（不为 NULL 时优先使用）
2. hr_social_param_config（city_code + insurance_code + 生效期匹配）
3. sys_config（config_key='default_social_*_rate'，集团兜底）
4. null → 该项社保金额为 0，记录 WARN 日志
```

**接口契约**：无新增 HTTP 接口。

**存量融合约束**：
- `hr_salary_rule` 的 `socialSecurityRate` / `housingFundRate` 字段已存在，仅做逻辑接入（**注意字段名无空格，非 `housing fund_rate`**）
- sys_config INSERT 使用 company_id=0（集团级），不影响租户隔离
- 不修改现有 `HrSocialCalcServiceImpl` 的核心方法签名

**单元测试**：
- 用例1：薪资模板有 socialSecurityRate → 使用该值
- 用例2：薪资模板 rate 为 NULL → 回退到 hr_social_param_config
- 用例3：两者均为 NULL → 回退到 sys_config 兜底值
- 用例4：三者均为 NULL → 记录 WARN 日志，该项金额为 0，不抛异常

**数据库脚本**：
```sql
-- 集团级默认社保比例兜底
INSERT INTO sys_config (company_id, config_key, config_value, config_name, remark) VALUES
(0, 'default_social_pension_personal_rate', '8.00', '养老保险个人默认比例(%)', 'hr_social_param_config未配置时的兜底值'),
(0, 'default_social_medical_personal_rate', '2.00', '医疗保险个人默认比例(%)', ''),
(0, 'default_social_unemployment_personal_rate', '0.50', '失业保险个人默认比例(%)', ''),
(0, 'default_housing_fund_employee_rate', '12.00', '公积金个人默认比例(%)', 'hr_housing_fund_config未配置时的兜底值');
```

---

### P0-10：月度薪资核算集成个人所得税计算

**业务目标**：月度薪资生成时，按累计预扣法计算个人所得税，写入 hr_salary_month.tax_amount。

**关联需求**：文档 11.8、四 P0-09

**开发范围**：

| 分层 | 变更内容 |
|------|----------|
| Service | 新增 `IncomeTaxCalculator` 工具类，实现累计预扣法个税计算 |
| Service | `HrSalaryServiceImpl.generateMonth()` 在社保核算后调用个税计算 |
| 实体 | `HrSalaryMonth.java` 确认 taxAmount 字段存在 |

**个税计算公式**：
```
应纳税所得额 = 应发工资 - 社保个人部分合计 - 公积金个人部分 - 5000起征点
应纳税额 = 应纳税所得额 × 适用税率 - 速算扣除数

税率表（月度累计）：
  ≤3000   税率3%   速算扣除数0
  3000~12000  税率10%  速算扣除数210
  12000~25000  税率20%  速算扣除数1410
  25000~35000  税率25%  速算扣除数2660
  35000~55000  税率30%  速算扣除数4410
  55000~80000  税率35%  速算扣除数7160
  >80000    税率45%  速算扣除数15160
```

**接口契约**：无新增 HTTP 接口。

**存量融合约束**：
- 个税计算是纯数学逻辑，不涉及数据库读写，工具类无状态
- 累计预扣法需要跨月累加：查询该员工当年 1~(当前月-1) 的已纳税额
- 不修改 generateMonth 的方法签名，只在其内部增加个税计算调用

**单元测试**：
- 用例1：月应发 13500，社保 1450，公积金 1440 → 应纳税所得额 5610 → 税额 331（10%税率-210速算扣除）
- 用例2：累计预扣法——第 2 个月累计应纳税所得额正确累加
- 用例3：应纳税所得额为负数 → 税额为 0
- 用例4：员工当年无历史纳税记录 → 正常计算当月税额

**不需要**：数据库变更。

---

## 三、P1 任务清单（提升业务完整性）

### P1-01：考勤手动修正功能

**开发范围**：新建 `hr_attendance_correction` 表 + CRUD Controller/Service + 前端页面

**数据库脚本**：
```sql
CREATE TABLE hr_attendance_correction (
  id BIGINT PRIMARY KEY,
  company_id BIGINT NOT NULL,
  employee_id BIGINT NOT NULL,
  correction_date DATE NOT NULL COMMENT '修正日期',
  original_status TINYINT COMMENT '原始状态 0正常 1迟到 2早退 3缺卡',
  corrected_status TINYINT COMMENT '修正后状态',
  reason VARCHAR(500) COMMENT '修正原因',
  apply_by BIGINT COMMENT '申请人',
  approve_by BIGINT COMMENT '审批人',
  flow_instance_id BIGINT COMMENT '流程实例ID',
  status TINYINT DEFAULT 0 COMMENT '0草稿 1审批中 2已通过 3已驳回',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME,
  UNIQUE KEY uk_emp_date (company_id, employee_id, correction_date)
);
```

**不需要**：新增 Maven 依赖。

---

### P1-02：请假/加班与考勤数据联动

**开发范围**：
- 请假审批通过后，自动在 hr_attendance 中标记对应日期为请假状态
- 加班审批通过后，自动累加员工加班时长
- 月度薪资生成时，读取考勤扣款数据（缺勤/迟到/早退扣款）

**接口契约**：
```
GET  /api/hr/attendance/deduction?month={yyyy-MM}&companyId={id}  → 获取指定月份考勤扣款汇总
```

---

### P1-03：月度薪资明细导出后端接口

**开发范围**：在现有 HrSalaryMonthController 中新增导出接口，返回 Excel 格式。

**接口契约**：
```
GET  /api/hr/salary/month/export?month={yyyy-MM}&companyId={id}  → Excel 文件下载
```

**存量融合约束**：复用项目已有的 Excel 导出工具类（如 EasyExcel 或 POI）。

---

### P1-04：社保核算明细导出后端接口

**开发范围**：在 HrSocialCalcController 中新增导出接口。

**接口契约**：
```
GET  /api/hr/social/calc/export?month={yyyy-MM}&companyId={id}  → Excel 文件下载
```

---

### P1-05：补调薪模块

**开发范围**：完整模块开发，含申请表、明细表、审批流程、差额计算、审批回调。

**数据库脚本**：
```sql
CREATE TABLE hr_salary_adjust_apply (
  id BIGINT PRIMARY KEY,
  company_id BIGINT NOT NULL,
  employee_id BIGINT NOT NULL,
  employee_name VARCHAR(64),
  adjust_type TINYINT NOT NULL COMMENT '1=转正调薪 2=晋升调薪 3=手动补调 4=批量调薪',
  original_month VARCHAR(7) NOT NULL COMMENT '追溯月份 yyyy-MM',
  new_basic_salary DECIMAL(10,2),
  new_performance_salary DECIMAL(10,2),
  new_position_allowance DECIMAL(10,2),
  new_other_allowance DECIMAL(10,2),
  gross_diff DECIMAL(10,2) COMMENT '应发差额',
  net_diff DECIMAL(10,2) COMMENT '实发差额（含社保个税重算）',
  reason VARCHAR(500) COMMENT '补调原因',
  source_archive_id BIGINT COMMENT '关联的薪资档案ID',
  flow_instance_id BIGINT COMMENT '流程实例ID',
  exec_status TINYINT DEFAULT 0 COMMENT '0待执行 1已执行 2执行失败',
  exec_msg VARCHAR(500),
  exec_time DATETIME,
  status TINYINT DEFAULT 0 COMMENT '0草稿 1审批中 2已通过 3已驳回 4已撤回',
  create_by BIGINT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT,
  update_time DATETIME
);

CREATE TABLE hr_salary_adjust_detail (
  id BIGINT PRIMARY KEY,
  apply_id BIGINT NOT NULL,
  company_id BIGINT NOT NULL,
  employee_id BIGINT NOT NULL,
  salary_month VARCHAR(7) NOT NULL COMMENT '追溯月份',
  old_gross_amount DECIMAL(10,2) COMMENT '原应发金额',
  old_net_amount DECIMAL(10,2) COMMENT '原实发金额',
  new_gross_amount DECIMAL(10,2) COMMENT '新应发金额',
  new_net_amount DECIMAL(10,2) COMMENT '新实发金额',
  diff_amount DECIMAL(10,2) COMMENT '差额',
  pay_status TINYINT DEFAULT 0 COMMENT '0未发放 1已发放',
  pay_time DATETIME,
  finance_remark VARCHAR(500) COMMENT '财务备注',
  voucher_no VARCHAR(64) COMMENT '凭证号',
  remark VARCHAR(500)
);
```

**接口契约**：
```
POST   /api/hr/salary/adjust/apply      → 提交补调薪申请
GET    /api/hr/salary/adjust/apply/{id} → 查询申请详情
GET    /api/hr/salary/adjust/apply/list → 列表查询
POST   /api/hr/salary/adjust/preview    → 差额预览（不提交）
```

---

### P1-06：审批提交流程提示（useFlowTip Composable）

**开发范围**：
- 新增 `useFlowTip.ts` 组合式函数，封装各审批类型的流程提示文案
- 在各审批提交弹窗底部增加 `el-alert` 提示区域
- 覆盖 7 种审批类型：入职、转正、调岗、离职、薪资档案、批量调薪、补调薪

**前端 TypeScript 类型**：
```typescript
// types/flowTip.ts
export interface FlowTipConfig {
  tipKey: string;       // 提示文案 key
  tipType: 'success' | 'warning' | 'info';
  tipVisible: boolean;  // 是否显示
}

export const FLOW_TIP_CONFIGS: Record<string, FlowTipConfig> = {
  entry_approve:    { tipKey: '入职申请审批通过后，系统将自动创建员工档案和薪资档案', tipType: 'success', tipVisible: true },
  regular_approve:  { tipKey: '转正审批通过后，员工状态将更新为正式员工', tipType: 'info', tipVisible: true },
  transfer_approve: { tipKey: '调岗审批通过后，员工组织和岗位信息将自动更新', tipType: 'info', tipVisible: true },
  resign_approve:   { tipKey: '离职审批通过后，员工状态将更新为已离职，薪资核算将自动停止', tipType: 'warning', tipVisible: true },
  salary_archive:   { tipKey: '薪资档案审批通过后，新版本将自动生效，旧版本转为历史版本', tipType: 'info', tipVisible: true },
  batch_adjust:     { tipKey: '批量调薪审批通过后，将为所有涉及员工创建新薪资档案版本', tipType: 'info', tipVisible: true },
  salary_adjust:    { tipKey: '补调薪审批通过后，差额将随下月薪资一并发放', tipType: 'warning', tipVisible: true },
};
```

---

## 四、P2 任务清单（健壮性优化）

### P2-01：HR 模块权限常量补全

> ⚠️ **执行前必须读取 `PermissionConst.java` 确认常量是否已存在**，禁止虚构。

**已确认存在的常量**（来自 PermissionConst.java，无需补全）：
- `HR_SOCIAL_CALC_LIST` = "hr:social:calc:list" ✅
- `HR_SOCIAL_CALC_EXPORT` = "hr:social:calc:export" ✅
- `HR_RECALC_EXECUTE` = "hr:recalc:execute" ✅（实际常量名为 `HR_RECALC_TRIGGER`）
- `HR_SALARY_ARCHIVE_LIST/ADD/EDIT/DELETE` ✅
- `HR_SALARY_MONTH_LIST/GENERATE/PAY/EXPORT` ✅
- `HR_REGULAR_LIST/ADD/REVOKE` ✅
- `HR_TRANSFER_LIST/ADD/REVOKE` ✅
- `HR_RESIGN_LIST/ADD/REVOKE` ✅

**待补全的常量**（需先审查 PermissionConst.java 中是否已存在，若不存在再新增）：
- `HR_SALARY_MONTH_VIEW` = "hr:salary:month:view"（如有明细查看接口需求）
- 其他缺失常量以实际代码审查结果为准

### P2-02：员工数据一致性校验

**开发范围**：
- 删除员工前检查是否存在关联的薪资档案、月度薪资记录
- 存在关联数据时禁止删除，提示用户先处理关联数据
- 软删除时级联更新关联记录状态

### P2-03：审批流程审计日志完善

**开发范围**：
- 所有审批回调业务执行（成功/失败）强制记录审计日志
- 运维手动补偿重试操作全程记录
- 薪资档案变更完整记录变更前后的薪资明细

---

## 五、存量数据迁移脚本

### 5.1 入职城市字段存量数据

```sql
-- hr_entry_apply.city_code 新增字段，存量数据为空字符串，无需迁移
-- hr_employee.city_code 同理，无需迁移
```

### 5.2 存量无薪资档案在职员工批量初始化

```sql
-- 查看需要初始化的员工
SELECT e.id, e.employee_name, e.employee_no, e.org_id, e.post_id
FROM hr_employee e
LEFT JOIN hr_salary_archive sa ON sa.employee_id = e.id AND sa.is_current = 1 AND sa.is_delete = 0
WHERE e.company_id = {companyId}
  AND e.employee_status IN (1, 2)  -- 在职、试用期
  AND sa.id IS NULL;

-- 批量初始化脚本（按岗位匹配薪资规则）
INSERT INTO hr_salary_archive (
  company_id, employee_id, rule_id, grade_code, grade_name,
  basic_salary, performance_salary, position_allowance, other_allowance,
  source_type, source_id, is_current, version_no, effective_date,
  create_by, create_time
)
SELECT
  e.company_id, e.id, r.id, r.grade_code, r.grade_name,
  r.basic_salary, r.performance_salary, r.position_allowance, r.other_allowance,
  0, e.id, 1, 1, e.hire_date,
  {operatorId}, NOW()
FROM hr_employee e
LEFT JOIN hr_salary_archive sa ON sa.employee_id = e.id AND sa.is_current = 1 AND sa.is_delete = 0
LEFT JOIN hr_salary_rule r ON r.id = e.current_salary_rule_id
WHERE e.company_id = {companyId}
  AND e.employee_status IN (1, 2)
  AND sa.id IS NULL
  AND r.id IS NOT NULL;
```

---

## 六、开发顺序建议

```
Week 1（P0 核心链路）
├── P0-01  入职申请 cityCode 字段         1天
├── P0-03  入职申请社保基数字段           0.5天
├── P0-04  入职申请薪资规则选择           1天
├── P0-02  入职审批后自动创建薪资档案      1.5天
└── 联调测试                             1天

Week 2（P0 审批回调链路）
├── P0-05  转正/调岗/离职审批回调         1.5天
├── P0-06  薪资档案审批激活新版本          0.5天
├── P0-07  批量调薪审批执行                1天
└── 联调测试                             1天

Week 3（P0 核算链路）
├── P0-08  月度薪资空值保护                0.5天
├── P0-09  薪资模板社保比例参与核算         1天
├── P0-10  个税计算集成                    1.5天
└── 联调测试                             1.5天

Week 4（P1 业务完善）
├── P1-05  补调薪完整模块                  2天
├── P1-01  考勤手动修正                    2天
├── P1-02  请假加班联动                    1.5天
├── P1-03/P1-04  导出接口                  1天
└── P1-06  审批流程提示                    1天

Week 5（P2 加固 + 整体验收）
├── P2-01/P2-02/P2-03                     2天
├── 存量数据迁移验证                       1天
└── 全链路 UAT 测试                        2天
```

---

## 七、每轮开发任务启动模板

```
【当前具体小任务】P0-XX：任务名称

输出物清单：
1. 数据库脚本（ALTER/INSERT）
2. Java 实体/枚举/DTO
3. Mapper（如有手写 SQL）
4. Service 方法 + 单元测试
5. Controller（如有新增接口）
6. 前端 TS 类型 / Vue 组件

约束检查：
□ 所有 SQL 携带 company_id
□ 复用 UserContext.getLoginUser() 获取租户和操作人
□ Controller 层复用 Result<T>，Service 层不包裹
□ 审批回调使用 apply.getStatus() 做幂等判断，**禁止使用 exec_status/exec_msg/exec_time**
□ 异常在回调方法内部 try-catch 捕获，不向上抛
□ 不修改已有方法签名
□ 不引入新依赖
```

---

*本文档为 HR 模块开发任务的执行基准，AI 开发时严格按此文档逐任务输出，禁止跳步。*

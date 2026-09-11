# 中小型企业轻量化薪酬标准体系与薪资档案自动化设计方案（v6‑final 完整版）

> 文档说明

1. 本模块为现有HR系统**增量扩展**，完全兼容已有业务代码、数据库表、流程引擎、权限、审计日志体系；仅新增表、新增方法、扩展字段，无破坏性修改，存量历史数据支持平滑迁移。

2. REST接口规范：接口路径全部使用`/`分层分隔，**不使用下划线、连字符**。

3. 本版本为**独立完整的轻量化薪酬体系方案**，无需依赖任何旧版薪资方案，可全新落地部署；涵盖薪酬级别带宽体系、多模式薪资模板、全维度批量调薪、版本化薪资档案、奖金管控全能力，为完整闭环的薪资绩效解决方案。

**适用企业规模**：中小型企业（50‑500人）
**技术栈**：Spring Boot 3 + MyBatis‑Plus + JDK 21 + MySQL 8.0

## 一、设计目标

1. **入职零薪资录入**：选择岗位/薪酬级别自动带出薪资模板，审批通过自动生成员工档案与V1版本薪资档案；支持个别人员手动调整薪资，增加薪级带宽合规校验。

2. **薪资版本全程可追溯**：调薪、岗位异动、晋升仅新增新版本，历史版本只读，不可修改删除，满足财务审计追溯要求；薪资档案快照保存当时员工薪酬级别。

3. **模板与个人档案解耦**：薪资模板仅作为新员工默认基线；修改、停用模板，**不会改动存量员工薪资档案，仅对后续新入职生效**。

4. **薪酬级别轻量化管理**：支持薪酬级别配置（带宽：最低‑中位‑最高）；支持模板绑定岗位 / 绑定薪酬级别 / 岗位+薪酬级别组合三种模式；员工打上薪级标签，支持按薪酬级别筛选批量调薪。

5. **支持多维度批量调薪**：支持单员工、手动勾选一批、按部门、按岗位、按薪酬级别、全员普调；底层复用单员工调薪逻辑，全部生成新版本，历史记录完整留存。

6. **月度核算全自动**：自动读取员工当前生效薪资档案作为计算基线，叠加考勤、绩效、扣款、各类奖金动态业务数据生成月度工资。

7. **奖金灵活管控**：区分自动计算奖金、手工浮动奖金；支持开关控制专项奖金是否单独审批，月度工资单统一发放审批，适配内控审计要求。

8. **敏感配置流程管控**：薪资模板、批量调薪任务、年终奖新增修改均可接入审批流程，规避人为误操作风险。

## 二、数据库设计

> 说明：原有存量表只做ALTER追加字段；全新业务新增数据表。

### 2.1 薪资规则模板表 `hr_salary_rule`（v6新增绑定薪酬级别字段）

```SQL
CREATE TABLE hr_salary_rule (
  id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  company_id            BIGINT NOT NULL DEFAULT 0 COMMENT '所属公司ID，0=集团总部',
  post_id               BIGINT NULL DEFAULT NULL COMMENT '关联岗位ID，NULL表示通用模板',
  post_level            VARCHAR(32) NULL DEFAULT NULL COMMENT '职级快照',
  grade_code            VARCHAR(32) NULL COMMENT '关联薪酬级别编码，hr_salary_grade.grade_code',
  bind_type             TINYINT NOT NULL DEFAULT 1 COMMENT '绑定类型：1绑定岗位 2绑定薪酬级别 3岗位+薪酬级别组合',
  rule_name             VARCHAR(128) NOT NULL COMMENT '模板名称',
  basic_salary          DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '基本工资标准',
  performance_base      DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '绩效基数标准',
  position_allowance    DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '岗位津贴标准',
  other_allowance       DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '其他固定补贴标准',
  fixed_month_bonus     DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '岗位默认月度奖金参考值（仅算薪默认，非实际发放）',
  social_security_rate  DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '社保个人缴纳比例（%）',
  housing_fund_rate     DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '公积金个人缴纳比例（%）',
  is_general            TINYINT NOT NULL DEFAULT 0 COMMENT '是否通用模板 0否 1是',
  status                TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0停用 1启用',
  remark                VARCHAR(500) NULL DEFAULT NULL,
  create_by             BIGINT NOT NULL DEFAULT 0,
  create_time           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by             BIGINT NULL DEFAULT NULL,
  update_time           DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_delete             TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_company_post_general (company_id, post_id, is_general),
  KEY idx_company_status (company_id, status, is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪资规则模板表';
```

> bind_type枚举
1：绑定岗位（原有模式）；2：绑定薪酬级别；3：岗位+薪酬级别组合。

### 2.2 新增薪酬级别表 `hr_salary_grade`

```SQL
CREATE TABLE hr_salary_grade (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL DEFAULT 0 COMMENT '公司ID，多租户隔离',
  grade_code VARCHAR(32) NOT NULL COMMENT '薪酬级别编码，例：P4、P5、P6、M1',
  grade_name VARCHAR(64) NOT NULL COMMENT '薪酬级别名称',
  salary_min DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '带宽下限，该级别工资最小值',
  salary_mid DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '带宽中位参考薪资',
  salary_max DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '带宽上限，该级别工资最大值',
  remark VARCHAR(500) NULL COMMENT '级别说明',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0停用，1启用',
  create_by BIGINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT NULL DEFAULT NULL,
  update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_delete TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_company_gradecode (company_id,grade_code,is_delete),
  KEY idx_company_status (company_id,status,is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪酬级别（薪级）配置表';
```

> **示例初始化数据（可直接执行）**

```SQL
INSERT INTO hr_salary_grade(company_id,grade_code,grade_name,salary_min,salary_mid,salary_max,status,create_by)
VALUES
(0,'P4','专员P4',8000,9500,11000,1,1),
(0,'P5','高级专员P5',10000,12000,14000,1,1),
(0,'P6','资深专员P6',13000,15500,18000,1,1),
(0,'M1','主管M1',16000,19000,22000,1,1);
```

### 2.3 员工主档案扩展，增加薪酬级别字段

```SQL
ALTER TABLE hr_employee
ADD COLUMN salary_grade_code VARCHAR(32) NULL COMMENT '员工当前薪酬级别编码，关联hr_salary_grade.grade_code';
```
> 存量兼容说明：存量员工无薪酬级别，salary_grade_code保持NULL；入职时必填grade_code；
> 若需存量初始化，可执行：UPDATE hr_employee SET salary_grade_code = 'P1' WHERE salary_grade_code IS NULL AND employee_status IN (1,2);

### 2.4 员工薪资档案表 `hr_salary_archive`（扩展薪级快照）

```SQL
-- 移除MySQL8.0不支持的DROP INDEX IF EXISTS内联语法，索引删除前置校验由Java代码层实现
ALTER TABLE hr_salary_archive
  ADD COLUMN version_no         INT NOT NULL DEFAULT 1 COMMENT '版本号' AFTER employee_name,
  ADD COLUMN effective_date     DATE NOT NULL DEFAULT '1970-01-01' COMMENT '生效日期' AFTER version_no,
  ADD COLUMN effective_end_date DATE NULL DEFAULT NULL COMMENT '失效日期' AFTER effective_date,
  ADD COLUMN source_type        TINYINT NOT NULL DEFAULT 1 COMMENT '来源 1模板自动 2人工录入 3调薪' AFTER effective_end_date,
  ADD COLUMN adjust_reason      VARCHAR(500) NULL DEFAULT NULL COMMENT '调薪原因' AFTER source_type,
  ADD COLUMN prev_archive_id    BIGINT NULL DEFAULT NULL COMMENT '上一版档案ID' AFTER adjust_reason,
  ADD COLUMN salary_grade_code  VARCHAR(32) NULL COMMENT '生成该版本时员工薪酬级别快照';

ALTER TABLE hr_salary_archive
  ADD KEY idx_employee_current (employee_id, is_delete, effective_date, effective_end_date),
  ADD KEY idx_company_employee (company_id, employee_id, is_delete, version_no),
  ADD UNIQUE KEY uk_employee_version (employee_id, version_no, is_delete);
```

字段说明

|字段|说明|
|---|---|
|version_no|薪资档案版本号，入职V1，每次调薪版本号+1|
|effective_date|版本生效日期，业务强制赋值；DDL默认1970-01-01仅作为NOT NULL约束占位值，入库前必须覆写为真实入职日期或调薪生效日期|
|effective_end_date|版本失效日期；当前生效版本为NULL；调薪时自动填充为新版本生效前一日|
|source_type|1‑模板自动生成；2‑人工录入；3‑调薪生成|
|adjust_reason|调薪备注、调整原因|
|prev_archive_id|关联上一个版本ID，用于版本链路追溯|
|salary_grade_code|薪资版本生成时刻员工薪酬级别快照，用于历史审计|

### 2.5（可选）员工薪级变更流水表，完整留痕

```SQL
CREATE TABLE hr_employee_grade_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL,
  employee_id BIGINT NOT NULL,
  old_grade_code VARCHAR(32) NULL,
  new_grade_code VARCHAR(32) NOT NULL,
  change_reason VARCHAR(500) NULL COMMENT '晋升/调级',
  effective_date DATE NOT NULL COMMENT '级别生效日期',
  create_by BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_delete TINYINT NOT NULL DEFAULT 0
) COMMENT='员工薪酬级别变更流水';
```

### 2.6 薪资模板审批申请表 `hr_salary_rule_apply`

```SQL
CREATE TABLE hr_salary_rule_apply (
  id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  company_id          BIGINT NOT NULL DEFAULT 0,
  apply_type          TINYINT NOT NULL COMMENT '1新增 2修改 3停用',
  rule_id             BIGINT NULL DEFAULT NULL,
  rule_name           VARCHAR(128) NOT NULL,
  post_id             BIGINT NULL DEFAULT NULL,
  grade_code          VARCHAR(32) NULL COMMENT '关联薪酬级别编码',
  bind_type           TINYINT NOT NULL DEFAULT 1 COMMENT '绑定类型 1岗位 2薪酬级别 3岗位+薪酬级别组合',
  basic_salary        DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  performance_base    DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  position_allowance  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  other_allowance     DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  fixed_month_bonus   DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '岗位默认月度奖金参考值',
  social_security_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  housing_fund_rate   DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  remark              VARCHAR(500) NULL,
  apply_user_id       BIGINT NOT NULL,
  apply_user_name     VARCHAR(64) NULL,
  status              TINYINT NOT NULL DEFAULT 0 COMMENT '0审批中 1已通过 2已驳回 3已撤回',
  flow_instance_id    BIGINT NULL DEFAULT NULL,
  is_delete           TINYINT NOT NULL DEFAULT 0,
  create_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time         DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_company_status (company_id, status, is_delete),
  KEY idx_apply_user (apply_user_id, create_time),
  KEY idx_rule_id (rule_id, is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪资模板审批申请表';
```

### 2.7 批量调薪任务主‑子表（新增，用于批量调薪能力）

```SQL
-- 批量调薪任务主表
CREATE TABLE hr_salary_batch_adjust (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL DEFAULT 0,
  batch_no VARCHAR(64) NOT NULL COMMENT '批量任务编号',
  adjust_effective_date DATE NOT NULL COMMENT '调薪统一生效日期',
  adjust_reason VARCHAR(500) NULL COMMENT '调薪原因（年度普调/岗位晋升等）',
  apply_user_id BIGINT NOT NULL COMMENT '操作人ID',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿 1待审批 2已执行完成 3已驳回 4部分失败',
  flow_instance_id BIGINT NULL COMMENT '审批流程实例ID，可为null',
  total_count INT NOT NULL DEFAULT 0 COMMENT '总人数',
  success_count INT NOT NULL DEFAULT 0 COMMENT '成功生成版本人数',
  fail_count INT NOT NULL DEFAULT 0 COMMENT '失败人数',
  remark VARCHAR(1000) NULL COMMENT '失败汇总备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_delete TINYINT NOT NULL DEFAULT 0,
  KEY idx_company_status (company_id,status,is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批量调薪任务头';

-- 批量调薪任务子表，存储每一个员工调薪明细快照
CREATE TABLE hr_salary_batch_adjust_item (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  batch_id BIGINT NOT NULL COMMENT '关联主表id',
  employee_id BIGINT NOT NULL COMMENT '员工ID',
  old_version_id BIGINT NULL COMMENT '旧薪资档案版本ID',
  new_basic_salary DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  new_performance_salary DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  new_position_allowance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  new_other_allowance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  exec_status TINYINT NOT NULL DEFAULT 0 COMMENT '0待执行 1成功 2失败',
  fail_msg VARCHAR(500) NULL COMMENT '失败原因文本',
  is_delete TINYINT NOT NULL DEFAULT 0,
  KEY idx_batch (batch_id,is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批量调薪任务明细';
```

### 2.8 月度工资单表扩展 `hr_salary_month`

```SQL
ALTER TABLE hr_salary_month
ADD COLUMN month_bonus DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '月度奖金',
ADD COLUMN other_bonus DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '其他临时奖金（含发放月份带入年终奖）',
ADD COLUMN bonus_remark VARCHAR(500) NULL COMMENT '奖金备注说明',
ADD COLUMN bonus_flow_instance_id BIGINT NULL COMMENT '专项奖金审批流程实例ID；自动计算奖金可为NULL';
```

### 2.9 年度奖金表 `hr_year_bonus`

```SQL
CREATE TABLE hr_year_bonus (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL DEFAULT 0 COMMENT '公司ID',
  employee_id BIGINT NOT NULL COMMENT '员工ID',
  bonus_year INT NOT NULL COMMENT '奖金归属年度，例：2026',
  bonus_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '年终奖应发金额',
  actual_pay_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '实际发放金额',
  pay_month VARCHAR(7) NULL COMMENT '实际发放月份 yyyy‑MM',
  salary_month_id BIGINT NULL COMMENT '关联月度工资单ID',
  flow_instance_id BIGINT NULL COMMENT '年终奖审批流程实例ID，自动计算可为NULL',
  remark VARCHAR(500) NULL COMMENT '核算说明',
  create_by BIGINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT NULL DEFAULT NULL,
  update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_delete TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_emp_year (company_id,employee_id,bonus_year,is_delete),
  KEY idx_company_year (company_id,bonus_year,is_delete)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工年度奖金表';
```

### 2.10 系统配置新增

```SQL
INSERT INTO sys_config(company_id,config_key,config_name,config_value,remark)
VALUES
(0,'salary/bonus/singleAuditEnable','专项奖金是否开启单独审批','0','0关闭，1开启；开启后人手工录入专项奖金必须走审批流程'),
(0,'salary/batchAdjust/auditEnable','批量调薪是否开启审批','0','0关闭可直接执行，1开启批量调薪需要审批'),
(0,'salary/grade/warnOnly','薪级带宽校验仅警告不拦截','1','1超出带宽仅警告，0超出直接拦截保存');
```

### 2.11 流程定义初始化SQL

```SQL
INSERT INTO flow_definition (company_id, def_name, def_code, biz_type, node_config_json, status, remark, create_by)
VALUES (
  0,
  '薪资模板变更审批',
  'salary_rule',
  'hr_salary_rule',
  '[{"nodeName":"子公司经理审批","nodeMode":"single","handlerType":"role","handlerValue":"sub_manager"}]',
  1,
  '薪资规则模板的新增/修改/停用必须经此流程审批后生效',
  1
),
(
  0,
  '批量调薪审批',
  'salary_batch_adjust',
  'hr_salary_batch_adjust',
  '[{"nodeName":"部门负责人审批","nodeMode":"single","handlerType":"role","handlerValue":"dept_manager"}]',
  1,
  '批量调薪任务审批流程',
  1
);
```

### 2.12 存量在职员工初始化脚本

```SQL
INSERT INTO hr_salary_archive (company_id, employee_id, employee_name, version_no, effective_date, source_type, basic_salary, create_by, create_time)
SELECT
  company_id,
  id,
  name,
  1,
  IFNULL(entry_date, '1970-01-01') AS effective_date

  2 AS source_type,
  basic_salary,
  create_by,
  create_time
FROM hr_employee
WHERE is_delete = 0
  AND employee_status IN (1,2)
  AND id NOT IN (SELECT employee_id FROM hr_salary_archive WHERE is_delete = 0);
  -- 注释说明：entry_date为NULL的存量数据视为未登记入职日期，归档时由HR手动补充
```
### 2.13 升级SQL执行顺序（不可跳步）
1. 新建 hr_salary_grade / hr_employee_grade_log
2. ALTER hr_employee 新增 salary_grade_code
3. ALTER hr_salary_archive（先DROP INDEX，再ADD COLUMN，再ADD KEY）
4. 新建 hr_salary_rule / hr_salary_rule_apply
5. 新建 hr_salary_batch_adjust / hr_salary_batch_adjust_item
6. ALTER hr_salary_month 新增4个奖金字段
7. INSERT sys_config（3条）
8. INSERT flow_definition（2条）
9. 执行存量员工初始化脚本（步骤2.12）

## 三、核心业务流程

### 3.1 薪资模板匹配优先级（同时支持岗位、薪酬级别、组合模式）

> 匹配顺序由高到低

1. bind_type=3 岗位+薪酬级别组合模板

2. bind_type=2 薪酬级别模板

3. bind_type=1 岗位模板

4. 系统通用模板兜底

### 3.2 入职申请自动建档流程（整合薪酬级别）

```Plaintext
HR填写入职申请
├─填写岗位（可选）
├─填写员工薪酬级别 grade_code（启用薪级体系必填）
↓
系统按优先级自动匹配薪资模板
↓
自动预填整套薪资（基本工资、津贴、绩效基数）
↓
带宽校验：拟定薪资对比该薪酬级别min‑max；配置控制警告/拦截
↓
HR可手动修改基本工资（津贴绩效取自模板）
↓
提交hr_entry审批流
↓
审批通过
↓
创建hr_employee员工档案，写入salary_grade_code
↓
自动生成hr_salary_archive V1版本薪资档案
  取值逻辑：一期仅基本工资允许表单填写；津贴/绩效/其他补贴从模板读取（入职申请表暂不扩展津贴绩效字段，二期迭代可扩展） > 通用模板 > 0
  档案快照salary_grade_code保存当前员工薪级
  sourceType自动判断：完全匹配模板=1，人工改动=2
↓
流程结束

【分支】审批驳回 / 申请撤回：不生成员工档案，也不生成薪资档案
```

### 3.3 单人调薪版本化流程（带宽校验）

```Plaintext
HR发起调薪，填写新薪资、调薪生效日期（支持未来生效）
↓
读取员工当前薪酬级别，做薪级带宽校验（警告或拦截）
↓
数据库行锁FOR UPDATE锁定员工当前生效薪资档案，防止并发冲突
↓
校验：新版本生效日期必须晚于旧版本生效日期
↓
旧版本档案设置effective_end_date = 新版本生效日期 - 1天（旧版本失效，记录保留）
↓
创建新版本档案，version+1，source_type=3调薪，prev_archive_id关联旧版本ID
  新版本快照salary_grade_code取员工当前薪级
↓
记录审计日志，流程结束
```
> 【技术说明】MyBatis-Plus LambdaQueryWrapper不支持FOR UPDATE，
> updateArchive方法中需用原生@Select注解或JdbcTemplate执行带行锁的查询：
> @Select("SELECT * FROM hr_salary_archive WHERE employee_id=#{employeeId}
>   AND company_id=#{companyId} AND is_delete=0
>   AND effective_date<=#{today} AND (effective_end_date IS NULL
>   OR effective_end_date>=#{today})
>   ORDER BY version_no DESC LIMIT 1 FOR UPDATE")
> HrSalaryArchive selectCurrentForUpdate(...);

### 3.4 晋升调级流程（薪酬级别变更）

> 两件事情：①更新员工薪酬级别；②生成薪资新版本；可以合并一套审批

```Plaintext
HR发起晋升申请，填写新薪酬级别、调薪薪资、生效日期
↓
审批流
↓
审批通过
  1、写入hr_employee_grade_log变更流水
  2、更新hr_employee.salary_grade_code为新grade_code
  3、带宽校验：新薪资必须落在新薪级带宽区间
  4、执行调薪逻辑生成薪资新版本，新版本快照记录新grade_code
↓
历史薪资档案保留旧薪级快照，可追溯
```

### 3.5 批量调薪完整业务流程（支持：单人、勾选一批、按部门、岗位、薪酬级别、全员）

```Plaintext
HR打开【批量调薪】功能
↓
方式A：Excel导入；方式B：页面筛选（筛选条件：部门、岗位、薪酬级别、在职状态）
↓
可以选择：统一涨固定金额 / 统一涨百分比 / 逐行手工录入每个人新薪资
↓
页面做前置校验：员工在职、存在生效薪资档案、薪资不为负数、薪级带宽校验
↓
填写统一生效日期、调薪原因；保存生成批量调薪任务（主表+子表）
↓
读取系统配置：salary/batchAdjust/auditEnable
  开关开启：提交批量调薪审批流
  开关关闭：直接进入执行阶段
↓
审批通过后执行批量任务
  ⚠️每个员工独立小事务；调用已有updateArchive()调薪方法；内部自带FOR UPDATE行锁
  单条员工失败记录fail_msg，其他员工继续执行，不整体回滚
↓
执行完成输出报告：总条数、成功条数、失败明细，可下载
↓
流程结束

重要约束：批量调薪**只会生成薪资档案新版本，不会修改薪资模板；如需新人同步新标准，需要HR手动更新薪资模板**
```

### 3.6 月度薪资核算完整流程（含奖金）

```Plaintext
选择算薪月份
↓
查询在职员工列表
↓
循环每个员工：查询该月份生效、未逻辑删除的薪资档案
  查询条件：effective_date ≤ 当月1号 AND (effective_end_date IS NULL OR effective_end_date ≥当月1号)
↓
基线取值优先级：当月生效薪资档案 → 岗位薪资模板 → 通用模板 → 员工档案basicSalary快照
↓
生成hr_salary_month月度工资单基线（基本工资、津贴、社保公积金扣款）
↓
分支1：绩效系统自动计算月奖金 → 直接回写month_bonus，bonus_flow_instance_id留空
分支2：手工录入专项月奖金
  读取配置开关salary/bonus/singleAuditEnable
  开关开启：必须提交奖金审批，审批通过才写入month_bonus并回填bonus_flow_instance_id
  开关关闭：允许直接录入month_bonus
↓
分支3：年终奖发放：从hr_year_bonus读取数据，带入other_bonus，回填bonus_flow_instance_id、关联salary_month_id
↓
汇总计算应发工资 = 基线薪资 +月奖金 +其他奖金 -考勤、扣款
↓
提交月度工资单整体发放审批
↓
审批通过后工资单归档，支持导出银行代发文件
```

### 3.7 薪资模板审批流程

```Plaintext
HR提交模板新增/修改/停用申请（可选择绑定岗位/薪酬级别/组合模式）
↓
提交hr/salaryRule审批流
↓
子公司经理审批
├─通过 → 更新hr_salary_rule模板数据生效，仅对后续新员工生效，存量档案不受影响
└─驳回 → 申请标记作废，模板无变化
```

### 3.8 专项奖金业务说明

1. **自动计算奖金（绩效输出）**：无需单独奖金审批；但归属的月度工资单**必须执行整体发放审批**。

2. **手工浮动/一次性奖金（项目奖、评优奖、年终奖）**：

    - 配置开关开启：必须走奖金审批，流程实例ID落库；驳回/撤回不生成奖金数据。

    - 配置开关关闭：允许直接录入，页面提示为简化模式，审计建议开启审批开关。

3. 薪资模板内`fixed_month_bonus`仅作为算薪默认参考值；当月实际发放奖金以月度工资单字段为准；修改模板不修改历史工资单。

## 四、接口设计（路径全部使用`/`分层，无下划线、连字符）

### 4.1 薪酬级别管理接口

|接口|请求方式|说明|
|---|---|---|
|`/hr/salary/grade/page`|GET|薪酬级别分页列表|
|`/hr/salary/grade/get/{gradeCode}`|GET|获取单个薪酬级别详情|
|`/hr/salary/grade/add`|POST|新增薪酬级别|
|`/hr/salary/grade/update`|POST|修改薪酬级别带宽信息|
|`/hr/salary/grade/disable`|POST|停用薪酬级别|

### 4.2 薪资模板接口

|接口|请求方式|说明|
|---|---|---|
|`/hr/salary/rule/page`|GET|薪资模板分页列表|
|`/hr/salary/rule/getByPost/{postId}`|GET|根据岗位ID获取当前生效薪资模板|
|`/hr/salary/rule/getByGrade/{gradeCode}`|GET|根据薪酬级别获取模板|
|`/hr/salary/rule/apply/add`|POST|提交新增薪资模板申请|
|`/hr/salary/rule/apply/update`|POST|提交修改薪资模板申请|
|`/hr/salary/rule/apply/disable`|POST|提交停用薪资模板申请|
|`/hr/salary/rule/apply/page`|GET|薪资模板审批申请分页列表|

### 4.3 薪资档案接口

|接口|请求方式|说明|
|---|---|---|
|`/hr/salary/archive/page`|GET|薪资档案分页列表|
|`/hr/salary/archive/getByEmployee/{employeeId}`|GET|查询该员工全部薪资版本历史|
|`/hr/salary/archive/getCurrent/{employeeId}`|GET|查询员工当前正在生效的薪资版本|
|`/hr/salary/archive/addVersion`|POST|单人调薪，生成新版本薪资档案|
|`/hr/salary/archive/history/{employeeId}`|GET|获取薪资版本时间线数据|

### 4.4 批量调薪接口

|接口|请求方式|说明|
|---|---|---|
|`/hr/salary/batchAdjust/page`|GET|批量调薪任务分页列表|
|`/hr/salary/batchAdjust/get/{batchId}`|GET|获取批量调薪任务以及明细|
|`/hr/salary/batchAdjust/createByImport`|POST|Excel导入创建批量调薪任务|
|`/hr/salary/batchAdjust/createByFilter`|POST|页面筛选条件创建批量调薪任务（部门/岗位/薪酬级别筛选）|
|`/hr/salary/batchAdjust/submitAudit/{batchId}`|POST|提交批量调薪任务去审批|
|`/hr/salary/batchAdjust/execute/{batchId}`|POST|执行批量调薪任务|
|`/hr/salary/batchAdjust/downloadFail/{batchId}`|GET|下载失败明细Excel|

### 4.5 月度工资单接口

|接口|请求方式|说明|
|---|---|---|
|`/hr/salary/month/page`|GET|月度工资单分页列表|
|`/hr/salary/month/generate`|POST|生成指定月份工资基线|
|`/hr/salary/month/saveBonus`|POST|保存当月奖金数据|
|`/hr/salary/month/submitAudit`|POST|提交月度工资整体发放审批|

### 4.6 年度奖金接口

|接口|请求方式|说明|
|---|---|---|
|`/hr/salary/yearBonus/page`|GET|年度奖金分页列表|
|`/hr/salary/yearBonus/apply/add`|POST|提交年终奖新增申请|
|`/hr/salary/yearBonus/apply/update`|POST|提交年终奖修改申请|
|`/hr/salary/yearBonus/getByEmp/{employeeId}`|GET|获取员工历年年终奖记录|

## 五、后端核心代码（关键片段）

### 5.1 获取生效薪资模板（支持薪酬级别、岗位组合匹配）

```Java
/**
 * 获取生效薪资模板
 * 优先级：岗位+薪级组合模板 > 薪级模板 > 岗位模板 > 通用模板
 */
public HrSalaryRule getActiveRule(Long companyId, Long postId, String gradeCode) {
    //1、优先找 岗位+薪酬级别组合 bind_type=3
    if(postId != null && gradeCode != null){
        HrSalaryRule comboRule = salaryRuleMapper.selectOne(
                Wrappers.lambdaQuery(HrSalaryRule.class)
                        .eq(HrSalaryRule::getCompanyId,companyId)
                        .eq(HrSalaryRule::getPostId,postId)
                        .eq(HrSalaryRule::getGradeCode,gradeCode)
                        .eq(HrSalaryRule::getBindType,3)
                        .eq(HrSalaryRule::getStatus,1)
                        .eq(HrSalaryRule::getIsDelete,0)
                        .last("LIMIT 1"));
        if(comboRule != null) return comboRule;
    }
    //2、匹配薪酬级别模板 bind_type=2
    if(gradeCode != null){
        HrSalaryRule gradeRule = salaryRuleMapper.selectOne(
                Wrappers.lambdaQuery(HrSalaryRule.class)
                        .eq(HrSalaryRule::getCompanyId,companyId)
                        .eq(HrSalaryRule::getGradeCode,gradeCode)
                        .eq(HrSalaryRule::getBindType,2)
                        .eq(HrSalaryRule::getStatus,1)
                        .eq(HrSalaryRule::getIsDelete,0)
                        .last("LIMIT 1"));
        if(gradeRule != null) return gradeRule;
    }
    //3、匹配岗位模板 bind_type=1
    if(postId != null){
        HrSalaryRule postRule = salaryRuleMapper.selectOne(
                Wrappers.lambdaQuery(HrSalaryRule.class)
                        .eq(HrSalaryRule::getCompanyId,companyId)
                        .eq(HrSalaryRule::getPostId,postId)
                        .eq(HrSalaryRule::getBindType,1)
                        .eq(HrSalaryRule::getStatus,1)
                        .eq(HrSalaryRule::getIsDelete,0)
                        .last("LIMIT 1"));
        if(postRule != null) return postRule;
    }
    //4、通用模板兜底
    return salaryRuleMapper.selectOne(
            Wrappers.lambdaQuery(HrSalaryRule.class)
                    .eq(HrSalaryRule::getCompanyId,companyId)
                    .isNull(HrSalaryRule::getPostId)
                    .eq(HrSalaryRule::getIsGeneral,1)
                    .eq(HrSalaryRule::getStatus,1)
                    .eq(HrSalaryRule::getIsDelete,0)
                    .last("LIMIT 1"));
}
```

### 5.2 批量调薪执行伪代码（独立子事务，复用原有updateArchive）

```Java
public void executeBatchAdjust(Long batchId){
    HrSalaryBatchAdjust batchTask = batchAdjustMapper.selectById(batchId);
    List<HrSalaryBatchAdjustItem> itemList = itemMapper.selectList(Wrappers.lambdaQuery(HrSalaryBatchAdjustItem.class)
            .eq(HrSalaryBatchAdjustItem::getBatchId,batchId).eq(HrSalaryBatchAdjustItem::getExecStatus,0));

    int success = 0;
    int fail = 0;
    for(HrSalaryBatchAdjustItem item : itemList){
        Boolean execResult = transactionTemplate.execute(status -> {
            try {
                SalaryArchiveDTO dto = new SalaryArchiveDTO();
                dto.setEmployeeId(item.getEmployeeId());
                dto.setNewEffectiveDate(batchTask.getAdjustEffectiveDate()); // TODO: 需在SalaryArchiveDTO中新增newEffectiveDate字段（LocalDate类型）
                dto.setBasicSalary(item.getNewBasicSalary());
                dto.setPerformanceSalary(item.getNewPerformanceSalary());
                dto.setPositionAllowance(item.getNewPositionAllowance());
                dto.setOtherAllowance(item.getNewOtherAllowance());
                dto.setRemark(batchTask.getAdjustReason());
                //复用原有单人调薪逻辑，自带行锁FOR UPDATE
                HrSalaryArchive newArchive = salaryArchiveService.updateArchive(dto);
                item.setExecStatus(1);
                item.setOldVersionId(newArchive.getPrevArchiveId());
                itemMapper.updateById(item);
                return true;
            }catch (Exception e){
                item.setExecStatus(2);
                item.setFailMsg(e.getMessage());
                itemMapper.updateById(item);
                status.setRollbackOnly();
                return false;
            }
        });
        if(Boolean.TRUE.equals(execResult)) success++;
        else fail++;
    }
    //更新批量任务统计
    batchTask.setSuccessCount(success);
    batchTask.setFailCount(fail);
    if(fail == 0){
        batchTask.setStatus(2);
    }else if(success >0){
        batchTask.setStatus(4);
    }
    batchAdjustMapper.updateById(batchTask);
}
```
### 5.3 入职申请自动生成薪资档案（createFromEntry）

> 触发时机：hr_entry审批流FlowHandler.onPass回调时调用

```Java
/**
 * 审批通过后自动创建薪资档案
 * 优先级：入职申请表手动填写 > 岗位模板默认值（逐字段独立判断）
 */
@Transactional(rollbackFor = Exception.class)
public void createFromEntry(Long entryApplyId) {
    //1. 查询入职申请表
    HrEntryApply apply = entryApplyMapper.selectById(entryApplyId);
    if (apply == null || !CommonConst.APPLY_STATUS_PASS.equals(apply.getStatus())) {
        throw new BusinessException("入职申请不存在或未通过审批");
    }

    //2. 查询匹配薪资模板（companyId从上下文获取，租户隔离）
    Long companyId = UserContext.getCompanyId();
    HrSalaryRule rule = salaryRuleService.getActiveRule(companyId, apply.getPostId(), apply.getGradeCode());

    //3. 创建薪资档案V1版本
    HrSalaryArchive archive = new HrSalaryArchive();
    archive.setCompanyId(companyId);
    archive.setEmployeeId(apply.getEmployeeId());
    archive.setEmployeeName(apply.getEmployeeName());
    archive.setVersionNo(1);
    archive.setEffectiveDate(apply.getEntryDate() != null ? apply.getEntryDate() : LocalDate.now());
    archive.setSourceTypeId(1); // 模板自动带出
    archive.setSalaryGradeCode(apply.getGradeCode()); // 薪级快照
    archive.setAdjustReason("入职自动生成");

    //4. 逐字段独立判断取值（入职申请表字段 > 模板字段 > 默认0）
    // 注意：hr_entry_apply目前只有basicSalary字段，津贴类从模板取
    archive.setBasicSalary(
        apply.getBasicSalary() != null 
            ? apply.getBasicSalary() 
            : (rule != null ? rule.getBasicSalary() : BigDecimal.ZERO)
    );
    archive.setPerformanceBase(
        rule != null ? rule.getPerformanceBase() : BigDecimal.ZERO
    );
    archive.setPositionAllowance(
        rule != null ? rule.getPositionAllowance() : BigDecimal.ZERO
    );
    archive.setOtherAllowance(
        rule != null ? rule.getOtherAllowance() : BigDecimal.ZERO
    );

    //5. 带宽校验（如配置开关开启）
    if (rule != null && rule.getGradeCode() != null) {
        HrSalaryGrade grade = salaryGradeMapper.selectOne(
            Wrappers.lambdaQuery(HrSalaryGrade.class)
                .eq(HrSalaryGrade::getCompanyId, companyId)
                .eq(HrSalaryGrade::getGradeCode, rule.getGradeCode())
                .eq(HrSalaryGrade::getIsDelete, 0)
        );
        if (grade != null) {
            validateBandwidth(archive.getBasicSalary(), grade);
        }
    }

    //6. 保存薪资档案
    salaryArchiveMapper.insert(archive);
    
    log.info("入职自动生成薪资档案成功 employeeId={} versionNo=1 archiveId={}", 
             archive.getEmployeeId(), archive.getId());
}

/**
 * 带宽校验辅助方法
 */
private void validateBandwidth(BigDecimal salary, HrSalaryGrade grade) {
    String warnOnlyConfig = sysConfigService.getValue("salary/grade/warnOnly");
    boolean warnOnly = "1".equals(warnOnlyConfig);
    
    if (salary.compareTo(grade.getSalaryMin()) < 0 
        || salary.compareTo(grade.getSalaryMax()) > 0) {
        if (!warnOnly) {
            throw new BusinessException(
                String.format("薪资%.2f超出薪酬级别[%s]带宽[%.2f, %.2f]", 
                    salary, grade.getGradeCode(), grade.getSalaryMin(), grade.getSalaryMax())
            );
        }
        log.warn("薪资带宽警告 employeeSalary={} gradeCode={}", salary, grade.getGradeCode());
    }
}
```

## 六、前端页面清单（新增页面）

1. **入职申请页 ****`transfer/index.vue`**：增加薪酬级别选择；根据岗位+薪级自动匹配模板；带宽校验提示；仅基本工资允许编辑。

2. **薪酬级别管理页面 ****`salary/grade/index.vue`**：维护薪级编码、带宽min/mid/max，启用停用。

3. **薪资模板管理页面 ****`salary/rule/index.vue`**：新增bind_type绑定类型选择，可以绑定岗位/薪酬级别/组合。

4. **薪资档案页面 ****`salary/archive/index.vue`**：列表展示版本、生效日期、来源、薪酬级别快照；调薪弹窗带宽校验提示。

5. **批量调薪页面 ****`salary/batchAdjust/index.vue`**：

    - 两种创建方式：Excel导入；页面筛选（部门/岗位/薪酬级别/在职）；

    - 支持统一涨薪比例/固定金额；预览新旧薪资；提交审批或直接执行；查看任务结果、下载失败明细。

6. **月度工资页面 ****`salary/month/index.vue`**：生成工资基线、维护奖金、提交整体发放审批。

7. **年度奖金页面 ****`salary/yearBonus/index.vue`**：年终奖维护、发起审批申请。

## 七、关键业务约束（重点，避免踩坑）

1. **薪酬级别hr_salary_grade只是标签+带宽校验规则**

    - 修改薪酬级别带宽min/max，**不会自动更新任何在职员工薪资档案**。

    - 如果需要给某薪酬级别员工普调工资，走【批量调薪】功能生成新版本薪资档案。

2. **薪资模板和薪酬级别关系**

    - 模板只是新人入职的默认样板；模板绑定薪酬级别，只影响新入职；存量员工不受模板修改影响。

3. **批量调薪约束**

    - 批量调薪**不会修改薪资模板**；普调完成后，如果希望后续新人使用新标准，需要HR手动更新薪资模板。

    - 每个员工独立小事务；个别失败不影响其他人；依靠原有调薪内部悲观行锁防止并发冲突。

4. **晋升（薪酬级别变更）**

    - 修改员工的`salary_grade_code`属于员工属性变更；**必须配套生成新版本薪资档案**，不能只改字段不改薪资。

    - 薪资档案保存薪级快照，历史审计不受员工现在改薪级的干扰。

5. **带宽校验开关**：`salary/grade/warnOnly`；1只弹窗警告，0直接拦截保存，适配不同阶段管理诉求。

6. **多租户隔离：companyId全部从UserContext上下文获取，不信任前端入参。**

7. **奖金隔离：薪资档案只保存固定薪资基线；月奖金、年终奖保存在独立业务表，不存入薪资档案版本。**

## 八、上线实施步骤

|步骤|内容|验证点|
|---|---|---|
|1|数据库全量备份，执行全部升级SQL脚本|新增表、字段、索引、示例薪级数据全部创建成功；存量员工迁移数据无报错|
|2|CommonConst常量补充：`FLOW_DEF_SALARY_RULE`、`FLOW_DEF_SALARY_BATCH_ADJUST`、`MODULE_HR_SALARY`等常量|编译无报错，符合项目规范|
|3|开发薪酬级别全套模块、批量调薪主子表业务、批量调薪流程处理器|单元测试通过；批量任务幂等校验|
|4|改造原有服务：入职生成档案时写入薪级快照；单人调薪增加薪级带宽校验|入职、单人调薪端到端测试|
|5|原有薪资模板模块扩展bind_type、grade_code字段；审批申请表同步扩展字段|薪资模板新增修改审批完整测试|
|6|前端页面开发：薪酬级别、批量调薪页面；入职页面增加薪酬级别选择；带宽校验提示交互|页面交互完整；Excel导入导出可用|
|7|全链路端到端测试：薪级‑模板匹配、入职、单人调薪、批量调薪、晋升调级、月度算薪、奖金审批|全部业务链路跑通|
|8|存量业务回归测试，旧入职流程、旧算薪不受改动影响|回归全部通过|
|9|生产部署上线||

## 九、测试用例（新增重点用例）

|编号|场景|预期结果|
|---|---|---|
|T11|入职填写岗位+薪酬级别P5，存在【岗位+薪级】组合模板|优先匹配组合模板，预填薪资，档案快照保存P5|
|T12|新员工薪资超出薪级带宽；配置warnOnly=1|弹窗警告，仍然允许保存；sourceType=2人工录入|
|T13|新员工薪资超出薪级带宽；warnOnly=0|直接拦截，无法提交入职申请|
|T14|批量调薪，筛选全部P5薪酬级别在职员工，统一涨8%|批量任务生成；每个P5员工生成薪资新版本；旧版本保留；失败记录明细；模板不自动变更|
|T15|员工晋升P5→P6，做晋升审批|员工主档案grade_code更新；生成薪资新版本；档案快照记录P6；薪级变更流水写入（开启该表时）|
|T16|修改薪酬级别P5带宽，min/max上调|存量P5员工薪资档案完全不变，只改变后续校验规则|
|T17|批量调薪开关开启，不提交审批直接执行|不允许执行，必须走审批通过才可以生成薪资版本|
|T18|批量调薪中某员工已经离职|标记该条失败，其他正常员工完成调薪|

## 十、方案独立落地摘要

本方案为**全新独立完整的中小企业薪酬薪资解决方案**，无需依赖任何旧版薪资体系、无需承接历史版本逻辑，可独立部署、全新落地。方案自主涵盖薪酬级别带宽配置、多类型薪资模板绑定、版本化薪资档案管理、多维度批量调薪、月度全自动算薪、专项/年度奖金管控、全流程审批审计等全套核心能力。整体采用系统增量开发模式，无强制历史版本绑定，适配50-500人中小企业从零搭建标准化、可追溯、可审计的薪酬体系，所有业务流程、数据库脚本、接口、页面、测试用例均为独立配套，可直接作为落地实施标准方案使用。

> v6版本在原有薪资模板、版本化薪资档案、奖金管理基础之上，新增轻量化薪酬级别（薪级带宽），支持岗位、薪酬级别、岗位+薪酬级别组合三种模板绑定模式；
支持多种筛选维度批量调薪：单人、勾选部分员工、按部门、岗位、薪酬级别、全员普调；批量调薪底层复用单人调薪逻辑，全部生成薪资新版本，历史记录完整可审计；
薪酬级别仅作为员工标签和薪资带宽校验规则，**修改级别带宽或者薪资模板，不会自动改动存量员工工资；在职员工薪资变更，一律生成薪资档案新版本，保证审计留痕。**


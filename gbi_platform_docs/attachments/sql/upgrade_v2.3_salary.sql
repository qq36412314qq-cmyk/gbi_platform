-- =====================================================================
-- 薪酬体系升级脚本 v2.3
-- 执行顺序：按脚本内注释编号依次执行，不可跳步
-- 作者：gbi
-- 日期：2026-09-11
-- =====================================================================

-- ===================== 1. 薪酬级别表 =====================
CREATE TABLE IF NOT EXISTS `hr_salary_grade` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `company_id` BIGINT NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `grade_code` VARCHAR(32) NOT NULL COMMENT '薪酬级别编码 如P1/P2/...',
  `grade_name` VARCHAR(64) NOT NULL COMMENT '薪酬级别名称',
  `grade_level` INT NOT NULL DEFAULT 0 COMMENT '级别层级 1最低',
  `band_min` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '带宽下限',
  `band_mid` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '带宽中位',
  `band_max` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '带宽上限',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_by` BIGINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT NULL DEFAULT NULL,
  `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_grade` (`company_id`, `grade_code`, `is_delete`),
  KEY `idx_company_status` (`company_id`, `status`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪酬级别表';

-- ===================== 2. 薪酬级别变更流水表 =====================
CREATE TABLE IF NOT EXISTS `hr_employee_grade_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `company_id` BIGINT NOT NULL DEFAULT 0,
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `employee_name` VARCHAR(64) NULL,
  `from_grade_code` VARCHAR(32) NULL COMMENT '原薪酬级别',
  `to_grade_code` VARCHAR(32) NOT NULL COMMENT '新薪酬级别',
  `change_type` TINYINT NOT NULL DEFAULT 1 COMMENT '1晋升 2调薪 3入职初始',
  `change_reason` VARCHAR(200) NULL COMMENT '变更原因',
  `flow_instance_id` BIGINT NULL COMMENT '关联审批流实例ID',
  `effective_date` DATE NOT NULL COMMENT '生效日期',
  `basic_salary_before` DECIMAL(12,2) NULL,
  `basic_salary_after` DECIMAL(12,2) NULL,
  `create_by` BIGINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_employee` (`employee_id`, `create_time`),
  KEY `idx_company` (`company_id`, `is_delete`),
  KEY `idx_flow_instance` (`flow_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪酬级别变更流水表';

-- ===================== 3. 薪资规则模板表 =====================
CREATE TABLE IF NOT EXISTS `hr_salary_rule` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `company_id` BIGINT NOT NULL DEFAULT 0,
  `rule_name` VARCHAR(128) NOT NULL COMMENT '模板名称',
  `bind_type` TINYINT NOT NULL DEFAULT 1 COMMENT '1岗位 2薪酬级别 3岗位+薪酬级别组合',
  `post_id` BIGINT NULL DEFAULT NULL COMMENT '关联岗位ID',
  `grade_code` VARCHAR(32) NULL DEFAULT NULL COMMENT '关联薪酬级别编码',
  `basic_salary` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '基本工资',
  `performance_base` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '绩效基数',
  `position_allowance` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '岗位津贴',
  `other_allowance` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '其他津贴',
  `fixed_month_bonus` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '月度默认奖金参考值',
  `social_security_rate` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '社保个人比例',
  `housing_fund_rate` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '公积金个人比例',
  `remark` VARCHAR(500) NULL,
  `flow_instance_id` BIGINT NULL DEFAULT NULL COMMENT '当前关联审批流实例ID',
  `apply_status` TINYINT NOT NULL DEFAULT 1 COMMENT '1直接生效 0审批中 2已驳回 3已撤回',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `create_by` BIGINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT NULL DEFAULT NULL,
  `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_company_status` (`company_id`, `status`, `is_delete`),
  KEY `idx_post` (`post_id`, `is_delete`),
  KEY `idx_grade` (`grade_code`, `is_delete`),
  KEY `idx_flow_instance` (`flow_instance_id`),
  KEY `idx_apply_status` (`company_id`, `apply_status`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪资规则模板表';

-- ===================== 4. 批量调薪任务表 =====================
CREATE TABLE IF NOT EXISTS `hr_salary_batch_adjust` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `company_id` BIGINT NOT NULL DEFAULT 0,
  `adjust_name` VARCHAR(128) NOT NULL COMMENT '任务名称 如"2026年度调薪"',
  `adjust_mode` TINYINT NOT NULL DEFAULT 1 COMMENT '1统一比例 2统一固定金额 3各员工不同',
  `adjust_value` DECIMAL(12,4) NOT NULL DEFAULT 0.00 COMMENT '调整值 比例时0.05表示5%',
  `target_grade_code` VARCHAR(32) NULL DEFAULT NULL COMMENT '目标薪酬级别（晋升调级时填）',
  `effective_date` DATE NOT NULL COMMENT '生效日期',
  `total_count` INT NOT NULL DEFAULT 0 COMMENT '总人数',
  `success_count` INT NOT NULL DEFAULT 0 COMMENT '成功人数',
  `fail_count` INT NOT NULL DEFAULT 0 COMMENT '失败人数',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿 1审批中 2已执行 3已驳回 4已撤回 5部分完成',
  `flow_instance_id` BIGINT NULL DEFAULT NULL COMMENT '关联审批流实例ID',
  `remark` VARCHAR(500) NULL,
  `create_by` BIGINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT NULL DEFAULT NULL,
  `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_company_status` (`company_id`, `status`, `is_delete`),
  KEY `idx_flow_instance` (`flow_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批量调薪任务表';

-- ===================== 5. 批量调薪明细表 =====================
CREATE TABLE IF NOT EXISTS `hr_salary_batch_adjust_item` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `batch_id` BIGINT NOT NULL COMMENT '批量任务ID',
  `employee_id` BIGINT NOT NULL,
  `employee_name` VARCHAR(64) NULL,
  `stall_id` BIGINT NULL COMMENT '铺位ID（员工关联的铺位）',
  `basic_salary_before` DECIMAL(12,2) NULL,
  `basic_salary_after` DECIMAL(12,2) NULL,
  `grade_code_before` VARCHAR(32) NULL,
  `grade_code_after` VARCHAR(32) NULL,
  `adjust_value` DECIMAL(12,4) NOT NULL DEFAULT 0.00 COMMENT '本行调整值',
  `adjust_mode` TINYINT NOT NULL DEFAULT 1 COMMENT '本行调整模式 1比例 2固定金额',
  `result_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待执行 1成功 2失败',
  `result_msg` VARCHAR(500) NULL COMMENT '失败原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_batch` (`batch_id`),
  KEY `idx_employee` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批量调薪明细表';

-- ===================== 6. 年终奖表 =====================
CREATE TABLE IF NOT EXISTS `hr_year_bonus` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `company_id` BIGINT NOT NULL DEFAULT 0,
  `employee_id` BIGINT NOT NULL,
  `employee_name` VARCHAR(64) NULL,
  `bonus_year` INT NOT NULL COMMENT '发放年度 如2026',
  `bonus_type` TINYINT NOT NULL DEFAULT 1 COMMENT '1年终奖 2项目奖 3评优奖 4其他一次性奖金',
  `bonus_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '奖金金额',
  `bonus_reason` VARCHAR(200) NULL COMMENT '奖金发放原因',
  `pay_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0未发放 1已发放到工资单',
  `flow_instance_id` BIGINT NULL DEFAULT NULL COMMENT '关联审批流实例ID',
  `apply_status` TINYINT NOT NULL DEFAULT 1 COMMENT '1直接生效 0审批中 2已驳回 3已撤回',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 0作废',
  `remark` VARCHAR(500) NULL,
  `create_by` BIGINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT NULL DEFAULT NULL,
  `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_company_year` (`company_id`, `bonus_year`, `is_delete`),
  KEY `idx_employee` (`employee_id`, `bonus_year`),
  KEY `idx_flow_instance` (`flow_instance_id`),
  KEY `idx_apply_status` (`company_id`, `apply_status`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='年终奖/一次性奖金表';

-- ===================== 7. ALTER hr_employee 加 salary_grade_code =====================
ALTER TABLE `hr_employee`
  ADD COLUMN IF NOT EXISTS `salary_grade_code` VARCHAR(32) NULL DEFAULT NULL COMMENT '关联薪酬级别编码 如P1/P2' AFTER `post_level`,
  ADD KEY IF NOT EXISTS `idx_grade_code` (`salary_grade_code`);

-- ===================== 8. ALTER hr_salary_archive 扩展为版本化结构 =====================
-- 先 DROP 旧索引（如果存在）
ALTER TABLE `hr_salary_archive`
  DROP INDEX IF EXISTS `idx_employee`,
  DROP INDEX IF EXISTS `idx_company`;

-- 扩展字段
ALTER TABLE `hr_salary_archive`
  ADD COLUMN IF NOT EXISTS `version_no` INT NOT NULL DEFAULT 1 COMMENT '版本号 从1递增' AFTER `company_id`,
  ADD COLUMN IF NOT EXISTS `source_type` TINYINT NOT NULL DEFAULT 1 COMMENT '1模板生成 2人工录入 3批量调薪 4晋升调级' AFTER `version_no`,
  ADD COLUMN IF NOT EXISTS `source_id` BIGINT NULL DEFAULT NULL COMMENT '来源单据ID 如批量调薪任务ID/调薪流程实例ID' AFTER `source_type`,
  ADD COLUMN IF NOT EXISTS `grade_code` VARCHAR(32) NULL COMMENT '版本快照 当时的薪酬级别编码' AFTER `source_id`,
  ADD COLUMN IF NOT EXISTS `grade_name` VARCHAR(64) NULL COMMENT '版本快照 当时的薪酬级别名称' AFTER `grade_code`,
  ADD COLUMN IF NOT EXISTS `rule_id` BIGINT NULL COMMENT '来源模板ID 模板生成时有值' AFTER `grade_name`,
  ADD COLUMN IF NOT EXISTS `rule_name` VARCHAR(128) NULL COMMENT '版本快照 模板名称' AFTER `rule_id`,
  ADD COLUMN IF NOT EXISTS `effective_date` DATE NOT NULL DEFAULT (CURRENT_DATE) COMMENT '生效日期' AFTER `rule_name`,
  ADD COLUMN IF NOT EXISTS `is_current` TINYINT NOT NULL DEFAULT 1 COMMENT '1当前生效版本 0历史版本' AFTER `effective_date`;

-- 重建索引
ALTER TABLE `hr_salary_archive`
  ADD KEY IF NOT EXISTS `idx_employee_current` (`employee_id`, `is_current`, `is_delete`),
  ADD KEY IF NOT EXISTS `idx_employee_version` (`employee_id`, `version_no`),
  ADD KEY IF NOT EXISTS `idx_company_grade` (`company_id`, `grade_code`);

-- ===================== 9. ALTER hr_salary_month 加奖金字段 =====================
ALTER TABLE `hr_salary_month`
  ADD COLUMN IF NOT EXISTS `performance_bonus` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '绩效奖金' AFTER `performance_salary`,
  ADD COLUMN IF NOT EXISTS `year_bonus` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '年终奖/一次性奖金' AFTER `performance_bonus`,
  ADD COLUMN IF NOT EXISTS `other_bonus` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '其他奖金' AFTER `year_bonus`,
  ADD COLUMN IF NOT EXISTS `leave_deduction` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '请假扣款' AFTER `other_bonus`,
  ADD COLUMN IF NOT EXISTS `salary_grade_code` VARCHAR(32) NULL COMMENT '版本快照 当月薪酬级别编码' AFTER `leave_deduction`;

-- ===================== 10. 新增 sys_config 配置项 =====================
INSERT INTO sys_config (company_id, config_key, config_value, remark) VALUES
(0, 'hr.salary.archive.bandwidth_warn_only', '1', '带宽校验模式 1仅警告允许保存 0直接拦截'),
(0, 'hr.salary.batch_adjust.require_audit', '1', '批量调薪是否必须走审批 1是 0否'),
(0, 'hr.salary.rule.require_audit', '1', '薪资模板变更是否必须走审批 1是 0否')
ON DUPLICATE KEY UPDATE config_value=VALUES(config_value);

-- ===================== 11. flow_definition 新增审批定义 =====================
-- 注意：薪资模板变更和批量调薪审批可能已存在，用 INSERT IGNORE 避免重复
INSERT IGNORE INTO flow_definition (company_id, def_name, def_code, biz_type, node_config_json, status, remark, create_by) VALUES
(0, '调薪审批', 'salary_archive_adjust', 'hr_salary_archive',
 '[{"nodeName":"部门负责人审批","nodeMode":"single","handlerType":"role","handlerValue":"dept_manager"},{"nodeName":"HR审批","nodeMode":"single","handlerType":"role","handlerValue":"hr_manager"}]',
 1, '单人调薪/晋升调级审批（本期实现）', 1),
(0, '年终奖审批', 'salary_year_bonus', 'hr_year_bonus',
 '[{"nodeName":"部门负责人审批","nodeMode":"single","handlerType":"role","handlerValue":"dept_manager"},{"nodeName":"财务复核","nodeMode":"single","handlerType":"role","handlerValue":"finance_manager"}]',
 1, '年终奖新增/修改审批（本期实现）', 1);

-- ===================== 12. 示例薪酬级别数据 =====================
INSERT INTO hr_salary_grade (company_id, grade_code, grade_name, grade_level, band_min, band_mid, band_max, status, create_by) VALUES
(0, 'P1', '初级专员', 1, 3000, 3500, 4000, 1, 1),
(0, 'P2', '专员', 2, 4000, 5000, 6000, 1, 1),
(0, 'P3', '高级专员', 3, 6000, 7500, 9000, 1, 1),
(0, 'P4', '主管', 4, 9000, 11000, 13000, 1, 1),
(0, 'P5', '经理', 5, 13000, 16000, 19000, 1, 1),
(0, 'P6', '高级经理', 6, 19000, 23000, 28000, 1, 1),
(0, 'M1', '总经理', 7, 28000, 35000, 42000, 1, 1);

-- ===================== 13. 存量数据迁移（给已有薪资档案补上版本字段默认值） =====================
UPDATE hr_salary_archive SET
  version_no = COALESCE(version_no, 1),
  source_type = COALESCE(source_type, 1),
  is_current = COALESCE(is_current, 1)
WHERE version_no IS NULL OR source_type IS NULL OR is_current IS NULL;

-- part2: ALTER + INSERT（MySQL 8.0.12 不支持 ADD COLUMN IF NOT EXISTS，直接执行）

ALTER TABLE `hr_employee`
  ADD COLUMN `salary_grade_code` VARCHAR(32) NULL DEFAULT NULL COMMENT '关联薪酬级别编码' AFTER `post_level`;
ALTER TABLE `hr_employee`
  ADD KEY `idx_grade_code` (`salary_grade_code`);

ALTER TABLE `hr_salary_archive`
  ADD COLUMN `version_no` INT NOT NULL DEFAULT 1 COMMENT '版本号' AFTER `company_id`,
  ADD COLUMN `source_type` TINYINT NOT NULL DEFAULT 1 COMMENT '1模板生成 2人工录入 3批量调薪 4晋升调级' AFTER `version_no`,
  ADD COLUMN `source_id` BIGINT NULL COMMENT '来源单据ID' AFTER `source_type`,
  ADD COLUMN `grade_code` VARCHAR(32) NULL COMMENT '版本快照 薪酬级别编码' AFTER `source_id`,
  ADD COLUMN `grade_name` VARCHAR(64) NULL COMMENT '版本快照 薪酬级别名称' AFTER `grade_code`,
  ADD COLUMN `rule_id` BIGINT NULL COMMENT '来源模板ID' AFTER `grade_name`,
  ADD COLUMN `rule_name` VARCHAR(128) NULL COMMENT '版本快照 模板名称' AFTER `rule_id`,
  ADD COLUMN `effective_date` DATE NOT NULL COMMENT '生效日期（代码层赋值）' AFTER `rule_name`,
  ADD COLUMN `is_current` TINYINT NOT NULL DEFAULT 1 COMMENT '1当前版本 0历史版本' AFTER `effective_date`,
  ADD KEY `idx_employee_current` (`employee_id`, `is_current`, `is_delete`),
  ADD KEY `idx_employee_version` (`employee_id`, `version_no`),
  ADD KEY `idx_company_grade` (`company_id`, `grade_code`);

ALTER TABLE `hr_salary_month`
  ADD COLUMN `performance_bonus` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '绩效奖金' AFTER `performance_salary`,
  ADD COLUMN `year_bonus` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '年终奖' AFTER `performance_bonus`,
  ADD COLUMN `other_bonus` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '其他奖金' AFTER `year_bonus`,
  ADD COLUMN `leave_deduction` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '请假扣款' AFTER `other_bonus`,
  ADD COLUMN `salary_grade_code` VARCHAR(32) NULL COMMENT '当月薪酬级别快照' AFTER `leave_deduction`;

INSERT INTO sys_config (company_id, config_key, config_value, remark) VALUES
(0, 'hr.salary.archive.bandwidth_warn_only', '1', '带宽校验模式 1仅警告 0拦截'),
(0, 'hr.salary.batch_adjust.require_audit', '1', '批量调薪是否必须审批 1是 0否'),
(0, 'hr.salary.rule.require_audit', '1', '薪资模板变更是否必须审批 1是 0否')
ON DUPLICATE KEY UPDATE config_value=VALUES(config_value);

INSERT IGNORE INTO flow_definition (company_id, def_name, def_code, biz_type, node_config_json, status, remark, create_by) VALUES
(0, '调薪审批', 'salary_archive_adjust', 'hr_salary_archive',
 '[{"nodeName":"部门负责人审批","nodeMode":"single","handlerType":"role","handlerValue":"dept_manager"},{"nodeName":"HR审批","nodeMode":"single","handlerType":"role","handlerValue":"hr_manager"}]',
 1, '单人调薪/晋升调级审批（本期实现）', 1),
(0, '年终奖审批', 'salary_year_bonus', 'hr_year_bonus',
 '[{"nodeName":"部门负责人审批","nodeMode":"single","handlerType":"role","handlerValue":"dept_manager"},{"nodeName":"财务复核","nodeMode":"single","handlerType":"role","handlerValue":"finance_manager"}]',
 1, '年终奖新增/修改审批（本期实现）', 1);

INSERT INTO hr_salary_grade (company_id, grade_code, grade_name, grade_level, band_min, band_mid, band_max, status, create_by) VALUES
(0, 'P1', '初级专员', 1, 3000, 3500, 4000, 1, 1),
(0, 'P2', '专员', 2, 4000, 5000, 6000, 1, 1),
(0, 'P3', '高级专员', 3, 6000, 7500, 9000, 1, 1),
(0, 'P4', '主管', 4, 9000, 11000, 13000, 1, 1),
(0, 'P5', '经理', 5, 13000, 16000, 19000, 1, 1),
(0, 'P6', '高级经理', 6, 19000, 23000, 28000, 1, 1),
(0, 'M1', '总经理', 7, 28000, 35000, 42000, 1, 1);

UPDATE hr_salary_archive SET
  version_no = COALESCE(version_no, 1),
  source_type = COALESCE(source_type, 1),
  is_current = COALESCE(is_current, 1)
WHERE version_no IS NULL OR source_type IS NULL OR is_current IS NULL;

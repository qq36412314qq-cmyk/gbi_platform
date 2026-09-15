SET NAMES utf8mb4; 
SET NAMES utf8mb4;


-- Step1: CREATE hr_salary_grade
CREATE TABLE IF NOT EXISTS hr_salary_grade (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  company_id BIGINT NOT NULL DEFAULT 0 COMMENT '所属公司ID，0=集团总部',
  grade_code VARCHAR(32) NOT NULL COMMENT '薪酬级别编码',
  grade_name VARCHAR(64) NOT NULL COMMENT '薪酬级别名称',
  salary_min DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '带宽下限',
  salary_mid DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '带宽中位',
  salary_max DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '带宽上限',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0停用 1启用',
  remark VARCHAR(500) NULL DEFAULT NULL,
  create_by BIGINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT NULL DEFAULT NULL,
  update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_delete TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id) USING BTREE,
  UNIQUE KEY uk_company_gradecode (company_id, grade_code, is_delete) USING BTREE,
  KEY idx_company_status (company_id, status, is_delete) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='薪酬级别配置表';

-- Step2: CREATE hr_employee_grade_log
CREATE TABLE IF NOT EXISTS hr_employee_grade_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL,
  employee_id BIGINT NOT NULL COMMENT 'employee id',
  old_grade_code VARCHAR(32) NULL,
  new_grade_code VARCHAR(32) NOT NULL,
  change_reason VARCHAR(500) NULL,
  effective_date DATE NOT NULL,
  create_by BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_delete TINYINT NOT NULL DEFAULT 0,
  KEY idx_company_employee (company_id, employee_id, is_delete) USING BTREE,
  KEY idx_effective_date (effective_date) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='grade_change_log';

-- Step3: ALTER hr_employee
ALTER TABLE hr_employee ADD COLUMN salary_grade_code VARCHAR(32) NULL DEFAULT NULL COMMENT 'salary grade code' AFTER post_level;

-- Step4: ALTER hr_entry_apply
ALTER TABLE hr_entry_apply ADD COLUMN grade_code VARCHAR(32) NULL DEFAULT NULL COMMENT 'grade code at application' AFTER post_id;

-- Step15: Migrate existing salary archives to versioned format
UPDATE hr_salary_archive
SET version_no = 1,
    effective_date = COALESCE((SELECT entry_date FROM hr_employee WHERE hr_employee.id = hr_salary_archive.employee_id AND hr_employee.is_delete = 0), create_time),
    effective_end_date = NULL,
    source_type = 2,
    salary_grade_code = (SELECT e.salary_grade_code FROM hr_employee e WHERE e.id = hr_salary_archive.employee_id AND e.is_delete = 0)
WHERE is_delete = 0 AND version_no = 0;

SELECT 'upgrade completed successfully' AS result;

-- Step14: INSERT hr_salary_grade sample data
INSERT INTO hr_salary_grade (company_id, grade_code, grade_name, salary_min, salary_mid, salary_max, status, create_by)
VALUES
  (0, 'P4', '专员P4', 8000.00, 9500.00, 11000.00, 1, 1),
  (0, 'P5', '高级专员P5', 10000.00, 12000.00, 14000.00, 1, 1),
  (0, 'P6', '资深专员P6', 13000.00, 15500.00, 18000.00, 1, 1),
  (0, 'M1', '主管M1', 16000.00, 19000.00, 22000.00, 1, 1)
ON DUPLICATE KEY UPDATE grade_name=VALUES(grade_name);

-- Step12: INSERT sys_config
INSERT INTO sys_config (company_id, config_key, config_name, config_value, remark, create_by)
VALUES
  (0, 'salary/bonus/singleAuditEnable', 'single_audit_enable', '0', 'bonus single audit', 1),
  (0, 'salary/batchAdjust/auditEnable', 'batch_adjust_audit_enable', '0', 'batch adjust audit', 1),
  (0, 'salary/grade/warnOnly', 'grade_warn_only', '1', 'grade warn only mode', 1)
ON DUPLICATE KEY UPDATE config_name=VALUES(config_name), config_value=VALUES(config_value);

-- Step11: CREATE hr_year_bonus
CREATE TABLE IF NOT EXISTS hr_year_bonus (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL DEFAULT 0,
  employee_id BIGINT NOT NULL,
  bonus_year INT NOT NULL,
  bonus_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  actual_pay_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  pay_month VARCHAR(7) NULL,
  salary_month_id BIGINT NULL,
  flow_instance_id BIGINT NULL,
  remark VARCHAR(500) NULL,
  create_by BIGINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT NULL DEFAULT NULL,
  update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_delete TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_emp_year (company_id, employee_id, bonus_year, is_delete) USING BTREE,
  KEY idx_company_year (company_id, bonus_year, is_delete) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='year_bonus';

-- Step10: ALTER hr_salary_month
ALTER TABLE hr_salary_month
  ADD COLUMN month_bonus DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'monthly bonus' AFTER net_amount,
  ADD COLUMN other_bonus DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'other bonus' AFTER month_bonus,
  ADD COLUMN bonus_remark VARCHAR(500) NULL DEFAULT NULL COMMENT 'bonus remark' AFTER other_bonus,
  ADD COLUMN bonus_flow_instance_id BIGINT NULL DEFAULT NULL COMMENT 'bonus flow instance id' AFTER bonus_remark;

-- Step9: ALTER hr_salary_archive
ALTER TABLE hr_salary_archive DROP INDEX uk_employee;
ALTER TABLE hr_salary_archive
  ADD COLUMN version_no INT NOT NULL DEFAULT 1 COMMENT 'version no' AFTER employee_name,
  ADD COLUMN effective_date DATE NOT NULL DEFAULT '1970-01-01' COMMENT 'effective date' AFTER version_no,
  ADD COLUMN effective_end_date DATE NULL DEFAULT NULL COMMENT 'end date' AFTER effective_date,
  ADD COLUMN source_type TINYINT NOT NULL DEFAULT 1 COMMENT '1=template 2=manual 3=adjust' AFTER effective_end_date,
  ADD COLUMN adjust_reason VARCHAR(500) NULL DEFAULT NULL AFTER source_type,
  ADD COLUMN prev_archive_id BIGINT NULL DEFAULT NULL AFTER adjust_reason,
  ADD COLUMN salary_grade_code VARCHAR(32) NULL DEFAULT NULL AFTER prev_archive_id,
  ADD KEY idx_employee_current (employee_id, is_delete, effective_date, effective_end_date) USING BTREE,
  ADD KEY idx_company_employee (company_id, employee_id, is_delete, version_no) USING BTREE,
  ADD UNIQUE KEY uk_employee_version (employee_id, version_no, is_delete) USING BTREE;

-- Step8: CREATE hr_salary_batch_adjust_item
CREATE TABLE IF NOT EXISTS hr_salary_batch_adjust_item (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  batch_id BIGINT NOT NULL,
  employee_id BIGINT NOT NULL,
  old_version_id BIGINT NULL,
  new_basic_salary DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  new_performance_salary DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  new_position_allowance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  new_other_allowance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  exec_status TINYINT NOT NULL DEFAULT 0 COMMENT '0=pending 1=success 2=fail',
  fail_msg VARCHAR(500) NULL,
  is_delete TINYINT NOT NULL DEFAULT 0,
  KEY idx_batch (batch_id, is_delete) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='batch_adjust_item';

-- Step7: CREATE hr_salary_batch_adjust
CREATE TABLE IF NOT EXISTS hr_salary_batch_adjust (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL DEFAULT 0,
  batch_no VARCHAR(64) NOT NULL,
  adjust_effective_date DATE NOT NULL,
  adjust_reason VARCHAR(500) NULL,
  apply_user_id BIGINT NOT NULL,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0=draft 1=pending 2=done 3=rejected 4=partial_fail',
  flow_instance_id BIGINT NULL,
  total_count INT NOT NULL DEFAULT 0,
  success_count INT NOT NULL DEFAULT 0,
  fail_count INT NOT NULL DEFAULT 0,
  remark VARCHAR(1000) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_delete TINYINT NOT NULL DEFAULT 0,
  KEY idx_company_status (company_id, status, is_delete) USING BTREE,
  KEY idx_batch_no (batch_no) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='batch_adjust_head';

-- Step6: CREATE hr_salary_rule_apply
CREATE TABLE IF NOT EXISTS hr_salary_rule_apply (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  company_id BIGINT NOT NULL DEFAULT 0,
  apply_type TINYINT NOT NULL COMMENT '1=add 2=update 3=disable',
  rule_id BIGINT NULL DEFAULT NULL,
  rule_name VARCHAR(128) NOT NULL,
  post_id BIGINT NULL DEFAULT NULL,
  grade_code VARCHAR(32) NULL,
  bind_type TINYINT NOT NULL DEFAULT 1,
  basic_salary DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  performance_base DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  position_allowance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  other_allowance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  fixed_month_bonus DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  social_security_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  housing_fund_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  is_general TINYINT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0=draft 1=pending 2=pass 3=reject 4=revoke',
  flow_instance_id BIGINT NULL DEFAULT NULL,
  apply_user_id BIGINT NOT NULL,
  apply_user_name VARCHAR(64) NULL DEFAULT NULL,
  remark VARCHAR(500) NULL DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_delete TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id) USING BTREE,
  KEY idx_company_status (company_id, status, is_delete) USING BTREE,
  KEY idx_apply_user (apply_user_id, create_time) USING BTREE,
  KEY idx_rule_id (rule_id, is_delete) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='salary_rule_apply';

-- Step5: CREATE hr_salary_rule
CREATE TABLE IF NOT EXISTS hr_salary_rule (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  company_id BIGINT NOT NULL DEFAULT 0,
  post_id BIGINT NULL DEFAULT NULL,
  post_level VARCHAR(32) NULL DEFAULT NULL,
  grade_code VARCHAR(32) NULL,
  bind_type TINYINT NOT NULL DEFAULT 1 COMMENT '1=post 2=grade 3=combo',
  rule_name VARCHAR(128) NOT NULL,
  basic_salary DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  performance_base DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  position_allowance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  other_allowance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  fixed_month_bonus DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  social_security_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  housing_fund_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  is_general TINYINT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(500) NULL DEFAULT NULL,
  create_by BIGINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT NULL DEFAULT NULL,
  update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_delete TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id) USING BTREE,
  UNIQUE KEY uk_company_post_general (company_id, post_id, is_general) USING BTREE,
  KEY idx_company_status (company_id, status, is_delete) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='salary_rule';

-- Step13: INSERT flow_definition (salary_rule_approval)
INSERT INTO flow_definition (company_id, def_name, def_code, biz_type, node_config_json, status, remark, create_by)
VALUES (0, 'salary_rule_approval', 'salary_rule', 'salary_rule', '[{\ nodeName\:\sub_manager\,\handlerType\:\role},{\nodeName\:\hr_director\,\handlerType\:\role}]', 1, 'salary rule approval', 1)
ON DUPLICATE KEY UPDATE def_name=VALUES(def_name);

-- Step13b: INSERT flow_definition (batch_adjust_approval)
INSERT INTO flow_definition (company_id, def_name, def_code, biz_type, node_config_json, status, remark, create_by)
VALUES (0, 'batch_adjust_approval', 'salary_batch_adjust', 'salary_batch_adjust', '[{\nodeName\:\sub_manager\,\handlerType\:\role},{\nodeName\:\finance_director\,\handlerType\:\role}]', 1, 'batch adjust approval', 1)
ON DUPLICATE KEY UPDATE def_name=VALUES(def_name);

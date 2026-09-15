-- =====================================================
-- 升级脚本 v2.8：员工工作经历与学业经历扩展
-- 执行顺序：第1步
-- =====================================================

-- 工作经历表
CREATE TABLE IF NOT EXISTS hr_employee_work_exp (
  id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id          BIGINT NOT NULL DEFAULT 0 COMMENT '所属公司ID',
  employee_id         BIGINT NOT NULL COMMENT '关联hr_employee.id',
  company_name        VARCHAR(128) NOT NULL COMMENT '公司名称',
  position            VARCHAR(64)  DEFAULT NULL COMMENT '职位',
  department          VARCHAR(64)  DEFAULT NULL COMMENT '部门',
  start_date          DATE NOT NULL COMMENT '入职时间',
  end_date            DATE DEFAULT NULL COMMENT '离职时间（当前在职可为空）',
  is_current          TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否仍在职 1是 0否',
  reason_for_leaving  VARCHAR(255) DEFAULT NULL COMMENT '离职原因',
  remark              VARCHAR(500) DEFAULT NULL,
  create_by           BIGINT NOT NULL DEFAULT 0,
  create_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by           BIGINT DEFAULT NULL,
  update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_delete           TINYINT NOT NULL DEFAULT 0,
  KEY idx_emp (company_id, employee_id, is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工工作经历表';

-- 学业经历表
CREATE TABLE IF NOT EXISTS hr_employee_edu_exp (
  id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id          BIGINT NOT NULL DEFAULT 0 COMMENT '所属公司ID',
  employee_id         BIGINT NOT NULL COMMENT '关联hr_employee.id',
  school_name         VARCHAR(128) NOT NULL COMMENT '学校名称',
  degree              VARCHAR(32)  DEFAULT NULL COMMENT '学历：本科/硕士/博士/大专/其他',
  major               VARCHAR(128) DEFAULT NULL COMMENT '专业',
  education_level     VARCHAR(32)  DEFAULT NULL COMMENT '教育形式：全日制/在职/自考等',
  start_date          DATE NOT NULL COMMENT '入学时间',
  graduation_date     DATE DEFAULT NULL COMMENT '毕业时间',
  is_graduated        TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已毕业 1是 0否',
  certificate_no      VARCHAR(64)  DEFAULT NULL COMMENT '学位证书号',
  remark              VARCHAR(500) DEFAULT NULL,
  create_by           BIGINT NOT NULL DEFAULT 0,
  create_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by           BIGINT DEFAULT NULL,
  update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_delete           TINYINT NOT NULL DEFAULT 0,
  KEY idx_emp (company_id, employee_id, is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工学业经历表';

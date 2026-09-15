-- =====================================================
-- 升级脚本 v2.7：社保公积金参数配置表创建
-- 执行顺序：第2步
-- =====================================================

-- 4. 社保公积金参数配置表
CREATE TABLE IF NOT EXISTS hr_social_param_config (
  id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id          BIGINT NOT NULL DEFAULT 0 COMMENT '所属公司ID，0=集团全局',
  city_code           VARCHAR(32) NOT NULL COMMENT '城市编码',
  insurance_code      VARCHAR(32) NOT NULL COMMENT '险种编码',
  industry_code       VARCHAR(32) DEFAULT NULL COMMENT '行业编码，仅工伤保险等按行业浮动险种填写',
  period_start        DATE NOT NULL COMMENT '生效起始日期',
  period_end          DATE DEFAULT NULL COMMENT '生效截止日期，NULL表示持续有效',
  base_min            DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '缴费基数下限',
  base_max            DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '缴费基数上限',
  personal_rate       DECIMAL(6,4) NOT NULL DEFAULT 0.0000 COMMENT '个人缴纳比例(%)',
  company_rate        DECIMAL(6,4) NOT NULL DEFAULT 0.0000 COMMENT '单位缴纳比例(%)',
  is_active           TINYINT NOT NULL DEFAULT 1 COMMENT '是否当前有效 0否 1是',
  remark              VARCHAR(500) DEFAULT NULL,
  create_by           BIGINT NOT NULL DEFAULT 0,
  create_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by           BIGINT DEFAULT NULL,
  update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_delete           TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_city_insurance_period (city_code, insurance_code, period_start, is_delete),
  KEY idx_company_city (company_id, city_code, is_delete),
  KEY idx_effective (city_code, insurance_code, period_start, period_end, is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社保公积金参数配置表';

-- 5. 公积金参数配置表
CREATE TABLE IF NOT EXISTS hr_housing_fund_config (
  id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id          BIGINT NOT NULL DEFAULT 0,
  city_code           VARCHAR(32) NOT NULL,
  period_start        DATE NOT NULL,
  period_end          DATE DEFAULT NULL,
  base_min            DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  base_max            DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  employee_rate       DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '员工个人比例(%)',
  company_rate        DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '单位比例(%)',
  is_active           TINYINT NOT NULL DEFAULT 1,
  remark              VARCHAR(500) DEFAULT NULL,
  create_by           BIGINT NOT NULL DEFAULT 0,
  create_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by           BIGINT DEFAULT NULL,
  update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_delete           TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_city_period (city_code, period_start, is_delete),
  KEY idx_company_city (company_id, city_code, is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公积金参数配置表';

-- 6. 社保公积金核算明细表（历史存档）
CREATE TABLE IF NOT EXISTS hr_social_calc_detail (
  id                      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id              BIGINT NOT NULL,
  employee_id             BIGINT NOT NULL,
  employee_name           VARCHAR(64) NOT NULL COMMENT '员工姓名快照',
  city_code               VARCHAR(32) NOT NULL COMMENT '城市快照',
  salary_month            VARCHAR(7) NOT NULL COMMENT '薪资月份，如2026-09',
  base_effective_year     VARCHAR(8) NULL COMMENT '基数生效年度快照',
  social_base             DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '社保实际缴费基数',
  housing_fund_base       DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '公积金实际缴费基数',
  -- 养老
  pension_personal        DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  pension_company         DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  pension_rate_personal   DECIMAL(6,4) NOT NULL DEFAULT 0.0000 COMMENT '养老个人比例快照',
  pension_rate_company    DECIMAL(6,4) NOT NULL DEFAULT 0.0000 COMMENT '养老单位比例快照',
  -- 医疗
  medical_personal        DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  medical_company         DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  medical_rate_personal   DECIMAL(6,4) NOT NULL DEFAULT 0.0000,
  medical_rate_company    DECIMAL(6,4) NOT NULL DEFAULT 0.0000,
  -- 失业
  unemployment_personal   DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  unemployment_company    DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  unemployment_rate_personal DECIMAL(6,4) NOT NULL DEFAULT 0.0000,
  unemployment_rate_company  DECIMAL(6,4) NOT NULL DEFAULT 0.0000,
  -- 工伤
  work_injury_company     DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  work_injury_rate        DECIMAL(6,4) NOT NULL DEFAULT 0.0000 COMMENT '工伤费率快照',
  -- 生育
  maternity_company       DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  maternity_rate          DECIMAL(6,4) NOT NULL DEFAULT 0.0000,
  -- 长护险
  long_care_personal      DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  long_care_company       DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  long_care_rate_personal DECIMAL(6,4) NOT NULL DEFAULT 0.0000,
  long_care_rate_company  DECIMAL(6,4) NOT NULL DEFAULT 0.0000,
  -- 公积金
  housing_fund_personal   DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  housing_fund_company    DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  housing_fund_rate       DECIMAL(6,4) NOT NULL DEFAULT 0.0000 COMMENT '公积金比例快照',
  -- 尾差记录
  rounding_diff           DECIMAL(12,4) NOT NULL DEFAULT 0.0000 COMMENT '计算尾差',
  create_time             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_delete               TINYINT NOT NULL DEFAULT 0,
  KEY idx_company_month (company_id, salary_month, is_delete),
  KEY idx_employee_month (employee_id, salary_month, is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社保公积金核算明细表';

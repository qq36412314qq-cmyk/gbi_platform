-- =====================================================
-- 升级脚本 v2.7：月度薪资表扩展
-- 执行顺序：第5步
-- =====================================================

-- 逐个添加列，忽略已存在的列
ALTER TABLE hr_salary_month ADD COLUMN IF EXISTS pension_personal DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '养老个人扣除';
ALTER TABLE hr_salary_month ADD COLUMN IF EXISTS pension_company DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '养老单位缴纳';
ALTER TABLE hr_salary_month ADD COLUMN IF EXISTS medical_personal DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '医疗个人扣除';
ALTER TABLE hr_salary_month ADD COLUMN IF EXISTS medical_company DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '医疗单位缴纳';
ALTER TABLE hr_salary_month ADD COLUMN IF EXISTS unemployment_personal DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '失业个人扣除';
ALTER TABLE hr_salary_month ADD COLUMN IF EXISTS unemployment_company DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '失业单位缴纳';
ALTER TABLE hr_salary_month ADD COLUMN IF EXISTS work_injury_company DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '工伤单位缴纳';
ALTER TABLE hr_salary_month ADD COLUMN IF EXISTS maternity_company DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '生育单位缴纳';
ALTER TABLE hr_salary_month ADD COLUMN IF EXISTS long_care_personal DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '长护险个人扣除';
ALTER TABLE hr_salary_month ADD COLUMN IF EXISTS long_care_company DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '长护险单位缴纳';
ALTER TABLE hr_salary_month ADD COLUMN IF EXISTS housing_fund_personal DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '公积金个人扣除';
ALTER TABLE hr_salary_month ADD COLUMN IF EXISTS housing_fund_company DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '公积金单位缴纳';
ALTER TABLE hr_salary_month ADD COLUMN IF EXISTS social_base DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '社保实际缴费基数';
ALTER TABLE hr_salary_month ADD COLUMN IF EXISTS housing_fund_base DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '公积金实际缴费基数';
ALTER TABLE hr_salary_month ADD COLUMN IF EXISTS base_effective_year VARCHAR(8) NULL COMMENT '基数生效年度快照';

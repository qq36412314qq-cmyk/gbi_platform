-- =====================================================
-- 升级脚本 v2.7：薪资模板改造
-- 执行顺序：第4步
-- =====================================================

-- 社保/公积金比例改为允许NULL，NULL表示继承全局参数
ALTER TABLE hr_salary_rule
  MODIFY COLUMN social_security_rate DECIMAL(5,2) DEFAULT NULL COMMENT '社保个人比例(%),NULL表示继承全局参数',
  MODIFY COLUMN housing_fund_rate    DECIMAL(5,2) DEFAULT NULL COMMENT '公积金个人比例(%),NULL表示继承全局参数';

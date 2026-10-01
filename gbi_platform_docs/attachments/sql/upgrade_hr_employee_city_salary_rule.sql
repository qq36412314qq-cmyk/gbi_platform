-- =====================================================
-- 升级脚本：员工档案 hr_employee 增加就职城市和薪资模板字段
-- 执行前请先执行：upgrade_hr_entry_city_salary_rule.sql
-- =====================================================

-- ===================== Step 1：hr_employee 扩展 =====================
ALTER TABLE hr_employee
  ADD COLUMN city_id BIGINT NULL COMMENT '就职城市ID（关联sys_city.id）' AFTER city_code,
  ADD COLUMN salary_rule_id BIGINT NULL COMMENT '薪资模板ID（关联hr_salary_rule.id）' AFTER basic_salary;

-- ===================== Step 2：存量数据同步（从 hr_entry_apply 回填） =====================
-- 将已通过审批的入职申请中的城市/模板信息同步到员工档案
UPDATE hr_employee e
INNER JOIN hr_entry_apply a ON a.employee_no = e.employee_no
    AND a.company_id = e.company_id
    AND a.status = 2   -- 仅同步已审批通过的记录
SET
    e.city_id         = a.city_id,
    e.salary_rule_id  = a.salary_rule_id
WHERE e.city_id IS NULL;

-- ===================== Step 3：默认值处理 =====================
-- 若 sys_city 表中 id=1 的记录（青岛）存在，则未填写城市的新建档员工默认填青岛
UPDATE hr_employee
SET city_id = 1
WHERE city_id IS NULL
  AND EXISTS (SELECT 1 FROM sys_city WHERE id = 1);

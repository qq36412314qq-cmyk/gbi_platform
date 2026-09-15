-- ============================================================
-- 入职申请表：增加公司+工号唯一索引
-- 执行前请确认无重复数据，否则需先清理
-- ============================================================
ALTER TABLE `hr_entry_apply`
  ADD UNIQUE INDEX `uk_company_employee_no` (`company_id`, `employee_no`)
  IF NOT EXISTS;
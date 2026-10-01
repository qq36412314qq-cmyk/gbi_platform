-- =============================================
-- 考勤模块升级SQL：增加休息日配置和是否参与考勤字段
-- 版本: v2.15
-- 日期: 2026-09-28
-- =============================================

-- 1. hr_employee 表增加字段（如尚未存在）
ALTER TABLE `hr_employee` 
  ADD COLUMN `workweek_config_id` bigint DEFAULT NULL COMMENT '休息日配置ID（关联sys_workweek_config.id）' AFTER `salary_rule_id`,
  ADD COLUMN `exempt_attendance` tinyint NOT NULL DEFAULT 0 COMMENT '是否免考勤 0参与 1不参与';

-- 2. hr_entry_apply 表增加字段（如尚未存在）
ALTER TABLE `hr_entry_apply` 
  ADD COLUMN `workweek_config_id` bigint DEFAULT NULL COMMENT '休息日配置ID（关联sys_workweek_config.id）' AFTER `salary_rule_id`,
  ADD COLUMN `exempt_attendance` tinyint NOT NULL DEFAULT 0 COMMENT '是否免考勤 0参与 1不参与';

-- 3. 初始化休息日配置默认数据（如sys_workweek_config表为空）
INSERT IGNORE INTO `sys_workweek_config` (`id`, `company_id`, `config_name`, `workweek_type`, `rest_day_pattern`, `status`, `remark`, `create_by`, `create_time`, `is_delete`) VALUES
(1, 0, '双休（默认）', 2, '周六、周日', 1, '标准双休', 1, NOW(), 0),
(2, 0, '单休', 1, '周日', 1, '单休制', 1, NOW(), 0),
(3, 0, '做五休二', 3, '周五、周六', 1, '周五六休息', 1, NOW(), 0),
(4, 0, '做六休一', 4, '周日', 1, '周日休息', 1, NOW(), 0);

-- 4. 为现有员工设置默认值（可选执行）
-- 将现有员工的休息日配置设置为默认的双休配置
UPDATE `hr_employee` SET `workweek_config_id` = 1 WHERE `workweek_config_id` IS NULL;
UPDATE `hr_entry_apply` SET `workweek_config_id` = 1 WHERE `workweek_config_id` IS NULL;

-- 5. 索引优化（可选）
-- 如需按休息日配置查询员工，可添加索引
-- ALTER TABLE `hr_employee` ADD INDEX `idx_workweek_config_id` (`workweek_config_id`);
-- ALTER TABLE `hr_entry_apply` ADD INDEX `idx_workweek_config_id` (`workweek_config_id`);

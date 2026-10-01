-- ============================================================
-- 修复：数据库表缺少 workweek_config_id / shift_type 列
-- 执行前请确认已在数据库中运行过 upgrade_v2.15_attendance_sync.sql
-- 本文件仅补全未执行的 ALTER TABLE 语句
-- ============================================================

-- 1. hr_entry_apply 表：新增 workweek_config_id（修复入职申请查询异常）
ALTER TABLE `hr_entry_apply`
  ADD COLUMN `workweek_config_id` bigint DEFAULT NULL COMMENT '休息日配置ID（关联sys_workweek_config.id）' AFTER `salary_rule_id`;

-- 2. hr_employee 表：新增 workweek_config_id（预防：若尚未执行upgrade_v2.15第5部分）
-- 注意：special_schedule 和 exempt_attendance 字段请确认是否已存在
ALTER TABLE `hr_employee`
  ADD COLUMN `workweek_config_id` bigint DEFAULT NULL COMMENT '休息日配置ID（关联sys_workweek_config.id）' AFTER `salary_rule_id`,
  ADD COLUMN `special_schedule` json DEFAULT NULL COMMENT '特殊排班JSON（格式：{"2026-10-01":"休息","2026-10-02":"上班"}）' AFTER `workweek_config_id`,
  ADD COLUMN `exempt_attendance` tinyint NOT NULL DEFAULT 0 COMMENT '是否免考勤 0参与 1不参与（不参与考勤的员工同步时默认为满勤）' AFTER `special_schedule`;

-- 3. hr_post 表：新增 workweek_config_id 和 shift_type（预防：若尚未执行upgrade_v2.15第6部分）
ALTER TABLE `hr_post`
  ADD COLUMN `workweek_config_id` bigint DEFAULT NULL COMMENT '岗位默认休息日配置ID' AFTER `dept_id`,
  ADD COLUMN `shift_type` tinyint DEFAULT NULL COMMENT '班次类型 1标准工时 2早班 3晚班 4夜班 5综合工时' AFTER `workweek_config_id`;

-- 4. sys_org 表：新增 default_workweek_config_id
-- 已确认可正常查询（日志277行），如未执行则取消注释下方语句
-- ALTER TABLE `sys_org`
--   ADD COLUMN `default_workweek_config_id` bigint DEFAULT 1 COMMENT '默认休息日配置ID' AFTER `org_type`;

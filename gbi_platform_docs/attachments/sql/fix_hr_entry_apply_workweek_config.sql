-- ============================================================
-- 修复：hr_entry_apply 表缺少 workweek_config_id 列
-- 原因：Java 实体 HrEntryApply.java 已添加 workweekConfigId 字段，
--       但数据库表尚未同步，导致 MyBatis-Plus 查询报 Unknown column
-- ============================================================

ALTER TABLE `hr_entry_apply`
  ADD COLUMN `workweek_config_id` bigint DEFAULT NULL COMMENT '休息日配置ID（关联sys_workweek_config.id）' AFTER `salary_rule_id`;

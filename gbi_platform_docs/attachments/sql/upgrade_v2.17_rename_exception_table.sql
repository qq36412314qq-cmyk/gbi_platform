-- =============================================
-- 考勤异常表重命名：attendance_exception -> hr_attendance_exception
-- 版本: v2.17
-- 日期: 2026-09-28
-- =============================================

-- 重命名表
RENAME TABLE `attendance_exception` TO `hr_attendance_exception`;

-- 添加缺失的审计字段（如果尚未存在）
ALTER TABLE `hr_attendance_exception`
  ADD COLUMN IF NOT EXISTS `create_by` bigint NOT NULL DEFAULT 0 COMMENT '创建人ID' AFTER `status`,
  ADD COLUMN IF NOT EXISTS `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  ADD COLUMN IF NOT EXISTS `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 验证表结构
SHOW COLUMNS FROM `hr_attendance_exception`;

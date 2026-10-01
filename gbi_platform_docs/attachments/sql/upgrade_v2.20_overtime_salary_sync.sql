-- =====================================================
-- upgrade_v2.20_overtime_salary_sync.sql
-- 用途：hr_salary_month 表增加 overtime_amount 字段，支撑薪资生成时同步加班补偿金额
-- 版本：v2.20
-- 日期：2026-09-29
-- =====================================================

ALTER TABLE `hr_salary_month`
    ADD COLUMN `overtime_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '加班补偿金额（元），由 generateMonth 同步' AFTER `skip_attendance`;

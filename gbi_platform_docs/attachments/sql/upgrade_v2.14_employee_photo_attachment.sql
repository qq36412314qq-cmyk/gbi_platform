-- ============================================================
-- 升级 v2.14：员工档案新增免冠照片和附件富文本字段
-- 执行方式: mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v2.14_employee_photo_attachment.sql
-- ============================================================
SET NAMES utf8mb4;

ALTER TABLE `hr_employee`
    ADD COLUMN `photo_file_id` bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '免冠照片对应的sys_file.id，0表示未上传' AFTER `remark`,
    ADD COLUMN `attachment_content` longtext COMMENT '附件内容（富文本HTML）' AFTER `photo_file_id`;

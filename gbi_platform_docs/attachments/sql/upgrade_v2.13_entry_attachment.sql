-- ============================================================
-- 升级 v2.13：入职申请新增附件富文本字段
-- 执行方式: mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v2.13_entry_attachment.sql
-- ============================================================
SET NAMES utf8mb4;

ALTER TABLE `hr_entry_apply`
    ADD COLUMN `attachment_content` LONGTEXT COMMENT '附件内容（富文本HTML）' AFTER `photo_file_id`;

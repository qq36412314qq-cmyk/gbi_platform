-- ============================================================
-- 升级 v2.12：入职申请新增免冠照片字段
-- 执行方式: mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v2.12_entry_photo.sql
-- ============================================================
SET NAMES utf8mb4;

ALTER TABLE `hr_entry_apply`
    ADD COLUMN `photo_file_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '免冠照片对应的sys_file.id，0表示未上传' AFTER `experience_data`;

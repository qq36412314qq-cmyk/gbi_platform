-- =====================================================
-- 升级脚本：入职申请增加就职城市和薪资模板字段
-- 关联：gbi_platform_admin/src/views/hr/transfer/index.vue
-- =====================================================

-- ===================== Step 1：hr_entry_apply 扩展 =====================
ALTER TABLE hr_entry_apply
  ADD COLUMN city_id BIGINT NULL COMMENT '就职城市ID（关联sys_city.id）' AFTER attachment_content,
  ADD COLUMN salary_rule_id BIGINT NULL COMMENT '薪资模板ID（关联hr_salary_rule.id）' AFTER city_id;

-- 存量数据：默认就职城市设为 1（青岛）
UPDATE hr_entry_apply SET city_id = 1 WHERE city_id IS NULL;

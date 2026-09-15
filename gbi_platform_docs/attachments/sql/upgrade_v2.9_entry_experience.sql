-- =====================================================
-- 升级脚本 v2.9：入职申请增加经历数据JSON字段
-- 执行顺序：在v2.8之后执行
-- =====================================================

-- 入职申请表新增经历数据JSON字段
ALTER TABLE hr_entry_apply 
ADD COLUMN experience_data TEXT DEFAULT NULL COMMENT '工作经历和学业经历JSON数据，格式：{"workExps":[{...}], "eduExps":[{...}]}';

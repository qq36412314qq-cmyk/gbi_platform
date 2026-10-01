-- ============================================================
-- 修复考勤配置管理菜单路径冲突
-- 问题：菜单224和228路径相同导致Vue Router注册失败
-- ============================================================

-- 步骤1：删除重复的菜单224（考勤配置管理，挂在考勤管理下）
DELETE FROM sys_menu WHERE id = 224;

-- 步骤2：更新菜单228的路径为唯一路径
UPDATE sys_menu 
SET path = '/hr/config' 
WHERE id = 228;

-- 步骤3：验证修复结果
SELECT 
  id, 
  parent_id, 
  menu_name, 
  permission, 
  path, 
  menu_type,
  CASE menu_type WHEN 1 THEN '目录' WHEN 2 THEN '页面' WHEN 3 THEN '按钮' END AS menu_type_text,
  visible
FROM sys_menu 
WHERE id = 228 
   OR path = '/hr/config';

-- 步骤4：确保角色权限已关联（如之前未执行）
INSERT INTO sys_role_menu_rel (role_id, menu_id)
SELECT 7, 228 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu_rel WHERE role_id = 7 AND menu_id = 228);

-- 步骤5：清除前端缓存后刷新页面测试
-- 访问 http://localhost:5173/hr/config 或 http://localhost:5173/hr/config/attendance

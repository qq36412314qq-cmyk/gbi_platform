-- ============================================================
-- 修复考勤管理菜单层级结构
-- 问题：考勤管理(id=111)有子菜单后变成了纯目录，无法直接访问页面
-- 方案：将考勤异常管理和配置管理提升为与考勤管理平级的独立菜单
-- ============================================================

-- 步骤1：查询当前考勤管理及其子菜单的实际层级
-- SELECT id, parent_id, menu_name, permission, path, menu_type FROM sys_menu WHERE id BETWEEN 111 AND 130;

-- 步骤2：查看是否有子菜单挂在111下面
-- SELECT id, parent_id, menu_name, permission, path, menu_type FROM sys_menu WHERE parent_id = 111;

-- 步骤3：修复方案 - 将子菜单提升为独立一级菜单（挂载在107人力资源下）
-- 注意：执行前请先确认111的子菜单ID，假设是223, 224（根据上次INSERT结果）

-- 方式A：如果111有子菜单，更新其parent_id从111改为107
UPDATE sys_menu 
SET parent_id = 107 
WHERE parent_id = 111 AND menu_type = 2;

-- 方式B：如果111本身被改成了目录（menu_type=1），恢复为页面
UPDATE sys_menu 
SET menu_type = 2, visible = 1 
WHERE permission = 'hr:attendance:list';

-- 步骤4：验证修复结果
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
WHERE id BETWEEN 111 AND 130 OR parent_id IN (107, 111)
ORDER BY id;

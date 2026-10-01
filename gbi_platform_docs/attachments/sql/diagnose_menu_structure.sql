-- ============================================================
-- 菜单结构问题诊断SQL
-- 请依次执行以下查询，确认实际菜单层级
-- ============================================================

-- 1. 查看考勤管理(id=111)的完整信息
SELECT 
  id,
  parent_id,
  menu_name,
  permission,
  path,
  menu_type,
  CASE menu_type 
    WHEN 1 THEN '目录（文件夹）' 
    WHEN 2 THEN '页面（可直接访问）' 
    WHEN 3 THEN '按钮' 
  END AS menu_type_desc,
  visible,
  icon,
  sort_order
FROM sys_menu 
WHERE id = 111;

-- 2. 查看111下面有哪些子菜单（这会阻止111作为页面直接访问）
SELECT 
  id,
  parent_id,
  menu_name,
  permission,
  path,
  menu_type,
  CASE menu_type 
    WHEN 1 THEN '目录' 
    WHEN 2 THEN '页面' 
    WHEN 3 THEN '按钮' 
  END AS menu_type_desc
FROM sys_menu 
WHERE parent_id = 111 AND is_delete = 0
ORDER BY sort_order;

-- 3. 查看HR模块完整菜单树（107-130范围）
SELECT 
  id,
  parent_id,
  menu_name,
  permission,
  path,
  menu_type,
  CASE menu_type 
    WHEN 1 THEN '目录' 
    WHEN 2 THEN '页面' 
    WHEN 3 THEN '按钮' 
  END AS menu_type_desc,
  visible
FROM sys_menu 
WHERE id BETWEEN 107 AND 150 AND is_delete = 0
ORDER BY id;

-- 4. 查看所有挂在111下的菜单（包括历史可能存在的）
SELECT 
  m.id,
  m.parent_id,
  m.menu_name,
  m.permission,
  m.path,
  m.menu_type,
  CASE m.menu_type 
    WHEN 1 THEN '目录' 
    WHEN 2 THEN '页面' 
    WHEN 3 THEN '按钮' 
  END AS menu_type_desc
FROM sys_menu m
WHERE m.parent_id = 111 
  AND m.is_delete = 0
ORDER BY m.sort_order;

-- 5. 修复建议（根据实际情况选择执行）
-- 方案1：将子菜单提升到107下（推荐）
-- UPDATE sys_menu SET parent_id = 107 WHERE parent_id = 111 AND menu_type = 2;

-- 方案2：将111改为目录类型，只保留子菜单作为页面
-- UPDATE sys_menu SET menu_type = 1 WHERE id = 111;

-- 方案3：删除111的子菜单，恢复111为独立页面
-- DELETE FROM sys_menu WHERE parent_id = 111;

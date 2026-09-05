-- ============================================================
-- 新增缴费明细单菜单（menu_id 131-135）
-- 执行方式: mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_004_add_pay_order_menu.sql
-- ============================================================

SET NAMES utf8mb4;

-- 1. 缴费明细单页面（id=131，parent=40 财务管理）
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_by)
VALUES (131, 40, '缴费明细单', 'finance:payOrder:list', '/finance/payOrder', 'DocumentChecked', 8, 2, 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 2. 超级管理员授予缴费明细单菜单
INSERT INTO sys_role_menu_rel (role_id, menu_id)
SELECT 1, 131 FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu_rel r WHERE r.role_id = 1 AND r.menu_id = 131);

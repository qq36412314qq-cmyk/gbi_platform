-- ============================================================
-- 升级脚本 v2.11：sys_menu 对齐 router 静态路由结构
-- 执行方式: mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v2.11_sync_menu_to_router.sql
-- 原则：以 router/index.ts 路由代码为基准，补全缺失、删除多余、调整层级
-- 保留「系统设置」整棵子树，将其从独立顶级目录并入「中台管理」
-- 新增「社保参数」父级目录收纳 HR 参数字典
-- ============================================================
SET NAMES utf8mb4;

-- ============================================================
-- 第一步：清理路由中不存在的菜单（删除）
-- ============================================================

-- 1.1 删除「优惠阈值配置」及其按钮（id=131~134）
--     路由中无此页面
DELETE FROM sys_role_menu_rel WHERE menu_id IN (131, 132, 133, 134);
DELETE FROM sys_menu WHERE id IN (131, 132, 133, 134);

-- ============================================================
-- 第二步：调整层级（parent_id 变更 + menu_type 修正）
-- ============================================================

-- 2.1 系统设置(id=2) 从独立顶级目录 → 中台管理(id=1) 的子目录
UPDATE sys_menu SET parent_id = 1
WHERE id = 2;

-- 2.2 新增「社保参数」父级目录 (id=207)，用于收纳 HR 参数字典类菜单
--     将 id=160~164 统一移入此目录
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_time)
VALUES (207, 1, '社保参数', NULL, '/platform/socialParam', 'Setting', 10, 1, 1, NOW())
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name);

-- 2.3 城市字典~公积金参数(id=160~164) 从参数配置(id=8)下 → 社保参数(id=207) 子菜单
UPDATE sys_menu SET parent_id = 207
WHERE id IN (160, 161, 162, 163, 164);

-- 2.4 水电表管理(id=37) 提升为目录类型(menu_type=1)，作为水电表子页面的父容器
--     路由结构：/property/meter 是 Layout 组件，下有 bill 和 pay 两个子页面
UPDATE sys_menu SET menu_type = 1, permission = NULL, sort_order = 1
WHERE id = 37;

-- 2.5 水电费账单(id=38) 改为 水电表管理(id=37) 的子菜单
--     路由：/property/meter/bill
UPDATE sys_menu SET parent_id = 37, sort_order = 2
WHERE id = 38;

-- 2.6 水电缴费管理(id=196) 改为 水电表管理(id=37) 的子菜单
--     路由：/property/meter/pay
UPDATE sys_menu SET parent_id = 37, sort_order = 3
WHERE id = 196;

-- 2.7 物业费账单目录(id=193) 保留为目录，账单列表(id=194)改为其子菜单
--     路由：/property/feeBill（目录）→ /property/feeBill/list（页面）
UPDATE sys_menu SET parent_id = 193, sort_order = 1
WHERE id = 194;

-- 2.8 缴费管理(id=199) permission 修正为 property:feePay:list（原为 add，与路由不一致）
--     路由：/property/feePay
UPDATE sys_menu SET permission = 'property:feePay:list'
WHERE id = 199;

-- 2.9 未支付订单 icon 修正为 Wallet（与路由 meta.icon 一致）
UPDATE sys_menu SET icon = 'Wallet' WHERE id = 195 AND icon != 'Wallet';

-- 2.10 摊位管理(name修正为铺位管理，与路由 title 一致)
UPDATE sys_menu SET menu_name = '铺位管理' WHERE id = 54;

-- ============================================================
-- 第三步：新增路由中有但数据库缺失的菜单
-- ============================================================

-- 3.1 铺位画布（挂在 铺位管理 id=54 下）
--     路由：/property/lease/stall/canvas，permission=lease:stall:list
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_time)
VALUES (203, 54, '铺位画布', 'lease:stall:list', '/property/lease/stall/canvas', 'Coordinate', 4, 2, 1, NOW())
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), permission=VALUES(permission), path=VALUES(path);

-- 3.2 缴费明细单（挂在 财务管理 id=40 下）
--     路由：/finance/payOrder，permission=finance:payOrder:list
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_time)
VALUES (204, 40, '缴费明细单', 'finance:payOrder:list', '/finance/payOrder', 'DocumentChecked', 8, 2, 1, NOW())
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), permission=VALUES(permission), path=VALUES(path);

-- 3.3 请假管理（挂在 OA办公 id=188 下）
--     路由：/oa/leave，permission=oa:leave:list
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_time)
VALUES (205, 188, '请假管理', 'oa:leave:list', '/oa/leave', 'DocumentChecked', 1, 2, 1, NOW())
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), permission=VALUES(permission), path=VALUES(path);

-- 3.4 会议室预约（挂在 OA办公 id=188 下）
--     路由：/oa/meetingBooking，permission=oa:meeting:booking:list
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_time)
VALUES (206, 188, '会议室预约', 'oa:meeting:booking:list', '/oa/meetingBooking', 'Calendar', 7, 2, 1, NOW())
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), permission=VALUES(permission), path=VALUES(path);

-- ============================================================
-- 第四步：重新分配角色权限
-- ============================================================

-- 4.1 删除已移除菜单的角色关联
DELETE FROM sys_role_menu_rel WHERE menu_id IN (131, 132, 133, 134);

-- 4.2 为新菜单分配业务角色权限（business_operator=7, sub_manager=4, group_finance=6）
INSERT IGNORE INTO sys_role_menu_rel (role_id, menu_id, create_time)
SELECT r.id, m.id, NOW()
FROM sys_role r
CROSS JOIN (SELECT 203 AS id UNION ALL SELECT 204 UNION ALL SELECT 205 UNION ALL SELECT 206 UNION ALL SELECT 207) m
JOIN sys_menu sm ON sm.id = m.id
WHERE r.role_code IN ('business_operator', 'sub_manager', 'group_finance')
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu_rel rm WHERE rm.role_id = r.id AND rm.menu_id = m.id
  );

-- ============================================================
-- 第五步：验证
-- ============================================================

SELECT '=== 顶级目录 ===' AS check_item;
SELECT id, parent_id, menu_name, path, menu_type FROM sys_menu WHERE parent_id = 0 ORDER BY sort_order;

SELECT '=== 中台管理(id=1) 直接子菜单 ===' AS check_item;
SELECT id, parent_id, menu_name, permission, path FROM sys_menu WHERE parent_id = 1 ORDER BY sort_order;

SELECT '=== 社保参数( id=207) 子菜单 ===' AS check_item;
SELECT id, parent_id, menu_name, permission, path FROM sys_menu WHERE parent_id = 207 ORDER BY sort_order;

SELECT '=== 物业管理(id=36) 完整结构 ===' AS check_item;
SELECT id, parent_id, menu_name, permission, path, menu_type FROM sys_menu
WHERE parent_id = 36 OR parent_id IN (
  SELECT id FROM sys_menu WHERE parent_id = 36
) OR parent_id IN (
  SELECT id FROM sys_menu WHERE parent_id IN (SELECT id FROM sys_menu WHERE parent_id = 36)
) ORDER BY parent_id, sort_order;

SELECT '=== 财务管理(id=40) 子菜单 ===' AS check_item;
SELECT id, parent_id, menu_name, permission, path FROM sys_menu WHERE parent_id = 40 ORDER BY sort_order;

SELECT '=== OA办公(id=188) 子菜单 ===' AS check_item;
SELECT id, parent_id, menu_name, permission, path FROM sys_menu WHERE parent_id = 188 ORDER BY sort_order;

SELECT '=== 角色权限数量 ===' AS check_item;
SELECT r.role_code, COUNT(rm.menu_id) AS menu_count
FROM sys_role r
LEFT JOIN sys_role_menu_rel rm ON rm.role_id = r.id
WHERE r.role_code IN ('super_admin','business_operator','sub_manager','group_finance','auditor','finance_admin')
GROUP BY r.role_code ORDER BY r.role_code;

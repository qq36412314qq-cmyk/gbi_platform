-- ============================================================
-- 升级脚本 v2.10 fix：无角色用户默认权限补全 + 缺失菜单补录
-- 执行方式: mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v2.10_fix_default_role.sql
-- 说明：
--   1. 新增 business_operator 角色（业务操作员），拥有所有业务模块操作权限
--   2. 将 test 用户关联到 business_operator 角色
--   3. 授予 business_operator 所有业务菜单权限（排除系统管理模块）
--   4. 为 sub_manager、group_finance 分配业务菜单权限
--   5. 补录缺失的菜单记录（OA办公、设备授权、物业费账单、水电缴费等）
-- ============================================================

SET NAMES utf8mb4;

-- 1. 新增业务操作员角色（幂等）
INSERT IGNORE INTO sys_role (company_id, role_name, role_code, remark, create_by)
VALUES (0, '业务操作员', 'business_operator', '默认业务操作员角色，拥有所有业务模块操作权限，无系统管理权限', 1);

-- 2. 将 test 用户（username=test, id=2）关联到 business_operator 角色（幂等）
INSERT IGNORE INTO sys_user_role_rel (user_id, role_id, create_by)
SELECT 2, r.id, 1 FROM sys_role r WHERE r.role_code = 'business_operator'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role_rel ur WHERE ur.user_id = 2 AND ur.role_id = r.id);

-- 3. 授予 business_operator 角色所有业务菜单权限（目录+页面+按钮，排除系统管理模块）
INSERT IGNORE INTO sys_role_menu_rel (role_id, menu_id, create_time)
SELECT r.id, m.id, NOW()
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_code = 'business_operator'
  AND m.menu_type IN (1, 2, 3)
  AND m.permission IS NOT NULL
  AND m.permission != ''
  AND m.permission NOT REGEXP '^(org:|user:|role:|menu:|dict:|config:|theme:|audit:|permission:)'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu_rel rm
    WHERE rm.role_id = r.id AND rm.menu_id = m.id
  );

-- 4. 为 sub_manager 分配所有业务菜单权限
INSERT IGNORE INTO sys_role_menu_rel (role_id, menu_id, create_time)
SELECT 4, m.id, NOW()
FROM sys_menu m
WHERE m.menu_type IN (1, 2, 3)
  AND m.permission IS NOT NULL
  AND m.permission != ''
  AND m.permission NOT REGEXP '^(org:|user:|role:|menu:|dict:|config:|theme:|audit:|permission:)'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu_rel rm WHERE rm.role_id = 4 AND rm.menu_id = m.id
  );

-- 5. 为 group_finance 分配所有业务菜单权限
INSERT IGNORE INTO sys_role_menu_rel (role_id, menu_id, create_time)
SELECT 6, m.id, NOW()
FROM sys_menu m
WHERE m.menu_type IN (1, 2, 3)
  AND m.permission IS NOT NULL
  AND m.permission != ''
  AND m.permission NOT REGEXP '^(org:|user:|role:|menu:|dict:|config:|theme:|audit:|permission:)'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu_rel rm WHERE rm.role_id = 6 AND rm.menu_id = m.id
  );

-- 6. 补录缺失的菜单记录
-- 6.1 设备授权管理（挂在中台管理下）
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_time)
VALUES (187, 1, '设备授权管理', 'device:auth:list', '/platform/deviceAuth', 'Lock', 16, 2, 1, NOW());

-- 6.2 OA办公父级目录
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_time)
VALUES (188, 0, 'OA办公', NULL, '/oa', 'Notebook', 4, 1, 1, NOW());

-- 6.3 OA子菜单
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_time)
VALUES
  (189, 188, '公告管理', 'oa:announcement:list', '/oa/announcement', 'Bell', 1, 2, 1, NOW()),
  (190, 188, '会议室管理', 'oa:meeting-room:list', '/oa/meetingRoom', 'Calendar', 2, 2, 1, NOW()),
  (191, 188, '打卡管理', 'oa:clock:list', '/oa/clock', 'Watch', 3, 2, 1, NOW()),
  (192, 188, '工作汇报', 'oa:work-report:list', '/oa/workReport', 'Files', 4, 2, 1, NOW()),
  (197, 188, '请假管理', 'oa:leave:list', '/oa/leave', 'Document', 5, 2, 1, NOW());

-- 6.4 OA按钮权限
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_time)
VALUES (202, 197, '新增申请', 'oa:leave:add', NULL, NULL, 1, 3, 0, NOW());

-- 6.5 会议室预约
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_time)
VALUES (198, 188, '会议室预约', 'oa:meeting:booking:list', '/oa/meetingBooking', 'Calendar', 6, 2, 1, NOW());

-- 6.6 物业费账单父级目录
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_time)
VALUES (193, 36, '物业费账单', NULL, '/property/feeBill', 'Money', 3, 1, 1, NOW());

-- 6.7 物业费账单列表
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_time)
VALUES (194, 193, '账单列表', 'property:feeBill:list', '/property/feeBill/list', 'Document', 1, 2, 1, NOW());

-- 6.8 未支付订单
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_time)
VALUES (195, 36, '未支付订单', 'property:unpaidBill:list', '/property/unpaidBill', 'Warning', 4, 2, 1, NOW());

-- 6.9 物业费缴费
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_time)
VALUES (199, 36, '缴费管理', 'property:feePay:add', '/property/feePay', 'Money', 5, 2, 1, NOW());

-- 6.10 水电缴费管理
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_time)
VALUES (196, 37, '缴费管理', 'waterElec:pay:list', '/property/meter/pay', 'Money', 3, 2, 1, NOW());

-- 6.11 水电缴费按钮权限
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_time)
VALUES
  (200, 196, '在线缴费', 'waterElec:pay:add', NULL, NULL, 1, 3, 0, NOW()),
  (201, 196, '退费处理', 'waterElec:pay:refund', NULL, NULL, 2, 3, 0, NOW());

-- 7. 验证结果
SELECT '角色创建验证：' AS step;
SELECT id, role_name, role_code FROM sys_role WHERE role_code = 'business_operator';

SELECT '用户角色关联验证：' AS step;
SELECT ur.user_id, u.username, r.role_name, r.role_code
FROM sys_user_role_rel ur
JOIN sys_user u ON u.id = ur.user_id
JOIN sys_role r ON r.id = ur.role_id
WHERE u.username = 'test';

SELECT '角色菜单权限数量验证：' AS step;
SELECT r.role_code, COUNT(rm.menu_id) AS menu_count
FROM sys_role r
LEFT JOIN sys_role_menu_rel rm ON rm.role_id = r.id
WHERE r.role_code IN ('super_admin','business_operator','sub_manager','group_finance','auditor','finance_admin')
GROUP BY r.role_code
ORDER BY r.role_code;

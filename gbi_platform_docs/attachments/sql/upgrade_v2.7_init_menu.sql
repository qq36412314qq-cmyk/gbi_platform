-- =====================================================
-- 升级脚本 v2.7：sys_menu 菜单数据（修复版）
-- 执行顺序：第8步（最后执行）
-- =====================================================

-- 1. 配置类菜单（挂载在「参数配置」下）
INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT (SELECT id FROM sys_menu WHERE permission = 'config:list'), '城市字典', 'hr:city:list', '/platform/city', 'Location', 1, 2, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:city:list');

INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT (SELECT id FROM sys_menu WHERE permission = 'config:list'), '险种字典', 'hr:insurance:list', '/platform/insuranceType', 'Tickets', 2, 2, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:insurance:list');

INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT (SELECT id FROM sys_menu WHERE permission = 'config:list'), '行业字典', 'hr:industry:list', '/platform/industry', 'Briefcase', 3, 2, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:industry:list');

INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT (SELECT id FROM sys_menu WHERE permission = 'config:list'), '社保参数配置', 'hr:social:param:list', '/platform/socialParam', 'Setting', 4, 2, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:social:param:list');

INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT (SELECT id FROM sys_menu WHERE permission = 'config:list'), '公积金参数配置', 'hr:housing:fund:list', '/platform/housingFundConfig', 'Coin', 5, 2, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:housing:fund:list');

-- 2. 业务类菜单（挂载在「社保公积金」下）
INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT (SELECT id FROM sys_menu WHERE permission = 'hr:social:list'), '社保核算明细', 'hr:social:calc:list', '/hr/socialCalc', 'Document', 1, 2, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:social:calc:list');

INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT (SELECT id FROM sys_menu WHERE permission = 'hr:social:list'), '年度基数重算', 'hr:recalc:trigger', '/hr/annualRecalc', 'Refresh', 2, 2, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:recalc:trigger');

-- 3. 城市字典按钮权限
INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '新增', 'hr:city:add', '', '', 1, 3, 1 FROM sys_menu WHERE permission = 'hr:city:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:city:add');

INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '编辑', 'hr:city:edit', '', '', 2, 3, 1 FROM sys_menu WHERE permission = 'hr:city:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:city:edit');

INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '删除', 'hr:city:delete', '', '', 3, 3, 1 FROM sys_menu WHERE permission = 'hr:city:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:city:delete');

-- 4. 险种字典按钮权限
INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '新增', 'hr:insurance:add', '', '', 1, 3, 1 FROM sys_menu WHERE permission = 'hr:insurance:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:insurance:add');

INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '编辑', 'hr:insurance:edit', '', '', 2, 3, 1 FROM sys_menu WHERE permission = 'hr:insurance:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:insurance:edit');

INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '删除', 'hr:insurance:delete', '', '', 3, 3, 1 FROM sys_menu WHERE permission = 'hr:insurance:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:insurance:delete');

-- 5. 行业字典按钮权限
INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '新增', 'hr:industry:add', '', '', 1, 3, 1 FROM sys_menu WHERE permission = 'hr:industry:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:industry:add');

INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '编辑', 'hr:industry:edit', '', '', 2, 3, 1 FROM sys_menu WHERE permission = 'hr:industry:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:industry:edit');

INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '删除', 'hr:industry:delete', '', '', 3, 3, 1 FROM sys_menu WHERE permission = 'hr:industry:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:industry:delete');

-- 6. 社保参数配置按钮权限
INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '新增', 'hr:social:param:add', '', '', 1, 3, 1 FROM sys_menu WHERE permission = 'hr:social:param:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:social:param:add');

INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '编辑', 'hr:social:param:edit', '', '', 2, 3, 1 FROM sys_menu WHERE permission = 'hr:social:param:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:social:param:edit');

INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '激活', 'hr:social:param:activate', '', '', 3, 3, 1 FROM sys_menu WHERE permission = 'hr:social:param:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:social:param:activate');

INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '停用', 'hr:social:param:deactivate', '', '', 4, 3, 1 FROM sys_menu WHERE permission = 'hr:social:param:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:social:param:deactivate');

INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '删除', 'hr:social:param:delete', '', '', 5, 3, 1 FROM sys_menu WHERE permission = 'hr:social:param:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:social:param:delete');

-- 7. 公积金参数配置按钮权限
INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '新增', 'hr:housing:fund:add', '', '', 1, 3, 1 FROM sys_menu WHERE permission = 'hr:housing:fund:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:housing:fund:add');

INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '编辑', 'hr:housing:fund:edit', '', '', 2, 3, 1 FROM sys_menu WHERE permission = 'hr:housing:fund:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:housing:fund:edit');

INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '激活', 'hr:housing:fund:activate', '', '', 3, 3, 1 FROM sys_menu WHERE permission = 'hr:housing:fund:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:housing:fund:activate');

INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '删除', 'hr:housing:fund:delete', '', '', 4, 3, 1 FROM sys_menu WHERE permission = 'hr:housing:fund:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:housing:fund:delete');

-- 8. 社保核算明细按钮权限
INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '导出', 'hr:social:calc:export', '', '', 1, 3, 1 FROM sys_menu WHERE permission = 'hr:social:calc:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:social:calc:export');

-- 9. 年度基数重算按钮权限
INSERT INTO sys_menu (parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible)
SELECT id, '执行重算', 'hr:recalc:execute', '', '', 1, 3, 1 FROM sys_menu WHERE permission = 'hr:recalc:trigger' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'hr:recalc:execute');

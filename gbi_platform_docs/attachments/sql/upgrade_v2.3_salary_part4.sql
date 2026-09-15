-- ============================================================
-- v2.3 薪酬体系升级 - 菜单权限 & 角色授权
-- 执行前提：
--   1) sys_menu AUTO_INCREMENT >= 135（如果已建库请先核对）
--   2) sys_role_menu_rel 允许 role_id=1 + menu_id 组合重复
-- ============================================================

-- 二级菜单（挂在 parent_id=112 薪酬管理目录下）
INSERT INTO sys_menu (id,parent_id,menu_name,permission,path,icon,sort_order,menu_type,visible,create_by,create_time,is_delete) VALUES
  (135,112,'薪酬级别','hr:salary:grade:list','/hr/salary/grade','Rank',6,2,1,1,NOW(),0),
  (136,112,'薪资模板','hr:salary:rule:list','/hr/salary/rule','EditPen',7,2,1,1,NOW(),0),
  (137,112,'薪资档案v2','hr:salary:archive:list','/hr/salary/archive','DocumentChecked',8,2,1,1,NOW(),0),
  (138,112,'批量调薪','hr:salary:batch:list','/hr/salary/batchAdjust','Sort',9,2,1,1,NOW(),0),
  (139,112,'年终奖管理','hr:salary:yearBonus:list','/hr/salary/yearBonus','Present',10,2,1,1,NOW(),0);

-- 三级按钮权限
INSERT INTO sys_menu (parent_id,menu_name,permission,path,icon,sort_order,menu_type,visible,create_by,create_time,is_delete) VALUES
  (135,'新增','hr:salary:grade:add',NULL,NULL,1,3,0,1,NOW(),0),
  (135,'编辑','hr:salary:grade:edit',NULL,NULL,2,3,0,1,NOW(),0),
  (135,'停用','hr:salary:grade:disable',NULL,NULL,3,3,0,1,NOW(),0),
  (136,'新增','hr:salary:rule:add',NULL,NULL,1,3,0,1,NOW(),0),
  (136,'编辑','hr:salary:rule:edit',NULL,NULL,2,3,0,1,NOW(),0),
  (136,'停用','hr:salary:rule:disable',NULL,NULL,3,3,0,1,NOW(),0),
  (136,'提交审批','hr:salary:rule:submit',NULL,NULL,4,3,0,1,NOW(),0),
  (138,'创建任务','hr:salary:batch:add',NULL,NULL,1,3,0,1,NOW(),0),
  (138,'提交审批','hr:salary:batch:submit',NULL,NULL,2,3,0,1,NOW(),0),
  (139,'新增','hr:salary:yearBonus:add',NULL,NULL,1,3,0,1,NOW(),0),
  (139,'编辑','hr:salary:yearBonus:edit',NULL,NULL,2,3,0,1,NOW(),0),
  (139,'提交审批','hr:salary:yearBonus:submit',NULL,NULL,3,3,0,1,NOW(),0);

-- 管理员授权 role_id=1（role_menu_rel 无唯一约束，用 INSERT IGNORE 防重复）
INSERT IGNORE INTO sys_role_menu_rel (role_id,menu_id,create_time) VALUES
  (1,135,NOW()),(1,136,NOW()),(1,137,NOW()),(1,138,NOW()),(1,139,NOW());
INSERT IGNORE INTO sys_role_menu_rel (role_id,menu_id,create_time)
SELECT 1,id,NOW() FROM sys_menu WHERE permission IN (
  'hr:salary:grade:add','hr:salary:grade:edit','hr:salary:grade:disable',
  'hr:salary:rule:add','hr:salary:rule:edit','hr:salary:rule:disable','hr:salary:rule:submit',
  'hr:salary:batch:add','hr:salary:batch:submit',
  'hr:salary:yearBonus:add','hr:salary:yearBonus:edit','hr:salary:yearBonus:submit'
) AND is_delete=0;

-- 缺失的审批角色补录（flow_definition 引用 sub_manager/finance_admin/group_finance，但初始数据未建）
-- 执行方式: mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < fix_leave_flow_roles.sql

-- 1. 插入缺失角色（幂等）
INSERT IGNORE INTO sys_role (company_id, role_name, role_code, remark, create_by)
VALUES
  (0, '子公司经理', 'sub_manager', '子公司经理审批角色，用于OA请假/合同等流程节点', 1),
  (0, '财务管理员', 'finance_admin', '财务审批角色，用于大额优惠/冲红等流程节点', 1),
  (0, '集团财务',   'group_finance', '集团财务复核角色，用于合同终止等流程节点', 1);

-- 2. 将超级管理员（user_id=1）加入这三个角色，确保开发/测试环境可审批
INSERT IGNORE INTO sys_user_role_rel (user_id, role_id, create_by)
SELECT 1, r.id, 1 FROM sys_role r WHERE r.role_code IN ('sub_manager', 'finance_admin', 'group_finance')
  AND NOT EXISTS (SELECT 1 FROM sys_user_role_rel ur WHERE ur.user_id=1 AND ur.role_id=r.id);

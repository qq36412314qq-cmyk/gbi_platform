-- =====================================================
-- 加班管理模块 flow_definition 初始化
-- 执行方式: mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < overtime_flow_init.sql
-- 说明: 加班申请审批流（单节点：子公司经理审批）
-- =====================================================

SET NAMES utf8mb4;

-- 加班申请流程定义
INSERT INTO flow_definition (company_id, def_name, def_code, biz_type, node_config_json, status, remark)
VALUES
  (0, '加班申请', 'hr_overtime_apply', 'hr_overtime_apply',
   '{"nodes":[{"nodeName":"主管审批","nodeMode":"single","handlerType":"role","handlerValue":"sub_manager","copyTo":[]}]}',
   1, '员工加班申请审批，由子公司经理审批');

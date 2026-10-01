-- =====================================================
-- sys_config 企微配置补录（v2.19）
-- 执行方式: mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v2.19_wecom_config.sql
-- 对比现状：
--   现有记录 15 条（key命名：wecom.agent.approval.id / wecom.sync.attendance.cron 等）
--   方案要求新增：corp_id、callback_url、web_url（INSERT IGNORE）
--   方案要求更新：4个 agent_id 空值 → 填入占位值
-- =====================================================

SET NAMES utf8mb4;

-- 1. 补录缺失的 3 条核心配置（INSERT IGNORE 幂等）
INSERT IGNORE INTO sys_config (config_key, config_value, config_name, remark, create_by, is_delete) VALUES
('wecom.corp_id',           'ww1234567890abcdef', '企业微信企业ID',           '企业微信企业管理后台获取', 1, 0),
('wecom.callback.url',      'https://your-domain.com/api/wecom/callback', '消息回调URL', '需公网可访问，HTTP+HTTPS均可', 1, 0),
('wecom.web_url',           'https://your-domain.com', '系统Web地址',            '用于消息中的跳转链接', 1, 0);

-- 2. 更新 4 个 AgentId 占位值（UPDATE 有值则跳过）
UPDATE sys_config SET
  config_value = CASE config_key
    WHEN 'wecom.agent.approval.id'  THEN '1000001'
    WHEN 'wecom.agent.attendance.id' THEN '1000002'
    WHEN 'wecom.agent.overtime.id'   THEN '1000003'
    WHEN 'wecom.agent.salary.id'     THEN '1000004'
  END
WHERE config_key IN ('wecom.agent.approval.id','wecom.agent.attendance.id','wecom.agent.overtime.id','wecom.agent.salary.id')
  AND (config_value IS NULL OR TRIM(config_value) = '');

-- 3. 验证输出
SELECT id, config_key, config_name, config_value FROM sys_config WHERE config_key LIKE 'wecom.%' ORDER BY config_key;

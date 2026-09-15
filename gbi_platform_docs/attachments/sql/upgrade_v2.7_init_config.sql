-- =====================================================
-- 升级脚本 v2.7：sys_config 全局默认参数
-- 执行顺序：第7步
-- =====================================================

-- 使用INSERT IGNORE忽略已存在的记录
INSERT IGNORE INTO sys_config (config_key, config_name, config_value, remark, create_time) VALUES
('hr.social.pension_rate_personal', '养老个人比例(%)', '8.00', '集团默认，可被城市配置覆盖', NOW()),
('hr.social.pension_rate_company', '养老单位比例(%)', '16.00', '集团默认', NOW()),
('hr.social.medical_rate_personal', '医疗个人比例(%)', '2.00', '集团默认', NOW()),
('hr.social.medical_rate_company', '医疗单位比例(%)', '4.50', '集团默认', NOW()),
('hr.social.unemployment_rate_personal', '失业个人比例(%)', '0.50', '集团默认', NOW()),
('hr.social.unemployment_rate_company', '失业单位比例(%)', '0.50', '集团默认', NOW()),
('hr.social.work_injury_rate_base', '工伤基准费率(%)', '0.20', '行业浮动基数', NOW()),
('hr.social.maternity_rate_company', '生育单位比例(%)', '0.50', '集团默认', NOW()),
('hr.social.long_care_enable', '长护险启用开关', '0', '0关1开，按城市单独控制', NOW()),
('hr.housing_fund.rate_default', '公积金默认比例(%)', '7.00', '模板为空时的默认值', NOW()),
('hr.salary.tax.standard_deduction', '个税起征点', '5000', '全国统一', NOW());

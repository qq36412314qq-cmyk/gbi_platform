-- =====================================================
-- 升级脚本 v2.7：险种字典初始数据
-- =====================================================
INSERT IGNORE INTO sys_insurance_type (insurance_code, insurance_name, insurance_type, personal_share, company_share, status) VALUES
('PENSION', '养老保险', 1, 1, 1, 1),
('MEDICAL', '医疗保险', 1, 1, 1, 1),
('UNEMPLOYMENT', '失业保险', 1, 1, 1, 1),
('WORK_INJURY', '工伤保险', 1, 0, 1, 1),
('MATERNITY', '生育保险', 1, 0, 1, 1),
('LONG_CARE', '长期护理险', 3, 1, 1, 1),
('SUPPLEMENT_MEDICAL', '补充医疗保险', 2, 1, 1, 1),
('ANNUITY', '企业年金', 2, 1, 1, 1);

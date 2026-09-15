INSERT INTO sys_config (company_id, config_key, config_name, config_value, remark) VALUES
(0, 'hr.salary.archive.bandwidth_warn_only', '带宽校验模式', '1', '1仅警告允许保存 0直接拦截'),
(0, 'hr.salary.batch_adjust.require_audit', '批量调薪是否必须审批', '1', '1是 0否'),
(0, 'hr.salary.rule.require_audit', '薪资模板变更是否必须审批', '1', '1是 0否')
ON DUPLICATE KEY UPDATE config_value=VALUES(config_value), config_name=VALUES(config_name);

INSERT IGNORE INTO flow_definition (company_id, def_name, def_code, biz_type, node_config_json, status, remark, create_by) VALUES
(0, '调薪审批', 'salary_archive_adjust', 'hr_salary_archive',
 '[{"nodeName":"部门负责人审批","nodeMode":"single","handlerType":"role","handlerValue":"dept_manager"},{"nodeName":"HR审批","nodeMode":"single","handlerType":"role","handlerValue":"hr_manager"}]',
 1, '单人调薪/晋升调级审批', 1),
(0, '年终奖审批', 'salary_year_bonus', 'hr_year_bonus',
 '[{"nodeName":"部门负责人审批","nodeMode":"single","handlerType":"role","handlerValue":"dept_manager"},{"nodeName":"财务复核","nodeMode":"single","handlerType":"role","handlerValue":"finance_manager"}]',
 1, '年终奖审批', 1);

INSERT IGNORE INTO hr_salary_grade (company_id, grade_code, grade_name, grade_level, band_min, band_mid, band_max, status, create_by) VALUES
(0, 'P1', '初级专员', 1, 3000, 3500, 4000, 1, 1),
(0, 'P2', '专员', 2, 4000, 5000, 6000, 1, 1),
(0, 'P3', '高级专员', 3, 6000, 7500, 9000, 1, 1),
(0, 'P4', '主管', 4, 9000, 11000, 13000, 1, 1),
(0, 'P5', '经理', 5, 13000, 16000, 19000, 1, 1),
(0, 'P6', '高级经理', 6, 19000, 23000, 28000, 1, 1),
(0, 'M1', '总经理', 7, 28000, 35000, 42000, 1, 1);

UPDATE hr_salary_archive SET
  version_no = COALESCE(version_no, 1),
  source_type = COALESCE(source_type, 1),
  is_current = COALESCE(is_current, 1);

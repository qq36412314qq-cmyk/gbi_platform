-- ============================================================
-- 考勤同步打卡系统升级 v2.15
-- 日期：2026-09-27
-- 关联方案：gbi_platform_docs/tmp/考勤同步打卡设计方案v2.0.md
-- ============================================================

-- 1. 休息日配置表
DROP TABLE IF EXISTS `sys_workweek_config`;
CREATE TABLE `sys_workweek_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL DEFAULT 0 COMMENT '所属公司ID，0=集团全局',
  `config_name` varchar(64) NOT NULL COMMENT '配置名称，如：双休、单休、做五休二',
  `workweek_type` tinyint NOT NULL COMMENT '休息日类型：1单休 2双休 3做五休二 4做六休一 5综合工时',
  `rest_day_pattern` varchar(32) DEFAULT NULL COMMENT '休息日模式，如：周六日、周日、周一',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `remark` varchar(255) DEFAULT NULL,
  `create_by` bigint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id` (`company_id` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='休息日配置表';

-- 初始化默认配置
INSERT INTO `sys_workweek_config` (`id`, `company_id`, `config_name`, `workweek_type`, `rest_day_pattern`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `is_delete`) VALUES
(1, 0, '双休（默认）', 2, '周六、周日', 1, '标准双休', 0, NOW(), NULL, NULL, 0),
(2, 0, '单休', 1, '周日', 1, '单休制', 0, NOW(), NULL, NULL, 0),
(3, 0, '做五休二', 3, '周五、周六', 1, '周五六休息', 0, NOW(), NULL, NULL, 0),
(4, 0, '做六休一', 4, '周日', 1, '周日休息', 0, NOW(), NULL, NULL, 0);

-- 2. 法定节假日配置表
DROP TABLE IF EXISTS `sys_holiday_config`;
CREATE TABLE `sys_holiday_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL DEFAULT 0 COMMENT '所属公司ID，0=集团全局',
  `holiday_date` date NOT NULL COMMENT '节假日日期',
  `holiday_name` varchar(64) NOT NULL COMMENT '节假日名称',
  `holiday_type` tinyint NOT NULL COMMENT '类型：1法定假日 2调休日 3补班日',
  `is_workday` tinyint NOT NULL DEFAULT 0 COMMENT '是否为工作日 0否 1是（补班日）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `remark` varchar(255) DEFAULT NULL,
  `create_by` bigint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_date` (`holiday_date` ASC, `company_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='法定节假日配置表';

-- 3. 员工班次配置表
DROP TABLE IF EXISTS `hr_employee_shift`;
CREATE TABLE `hr_employee_shift` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL DEFAULT 0 COMMENT '所属公司ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID（关联hr_employee.id）',
  `shift_type` tinyint NOT NULL COMMENT '班次类型 1标准工时 2早班 3晚班 4夜班 5综合工时',
  `shift_start_time` time DEFAULT NULL COMMENT '班次上班时间',
  `shift_end_time` time DEFAULT NULL COMMENT '班次下班时间',
  `start_date` date NOT NULL COMMENT '班次生效开始日期',
  `end_date` date DEFAULT NULL COMMENT '班次生效结束日期，NULL表示长期有效',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `remark` varchar(255) DEFAULT NULL,
  `create_by` bigint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id` (`employee_id` ASC) USING BTREE,
  INDEX `idx_date_range` (`start_date` ASC, `end_date` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工班次配置表';

-- 4. 考勤异常记录表
DROP TABLE IF EXISTS `hr_attendance_exception`;
CREATE TABLE `hr_attendance_exception` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL DEFAULT 0 COMMENT '所属公司ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `employee_name` varchar(64) NOT NULL COMMENT '员工姓名快照',
  `exception_type` tinyint NOT NULL COMMENT '异常类型：1连续缺卡 2月度迟到频繁 3旷工 4早退频繁',
  `exception_date` date NOT NULL COMMENT '异常发生日期',
  `detail_count` int DEFAULT NULL COMMENT '详情数量，如连续缺卡天数、迟到次数等',
  `detail_json` json DEFAULT NULL COMMENT '详情JSON，记录具体的异常日期列表',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '处理状态 0待处理 1已确认 2已豁免 3已忽略',
  `handle_by` bigint DEFAULT NULL COMMENT '处理人ID',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_remark` varchar(500) DEFAULT NULL COMMENT '处理备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id` (`employee_id` ASC) USING BTREE,
  INDEX `idx_exception_type` (`exception_type` ASC) USING BTREE,
  INDEX `idx_status` (`status` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤异常记录表';

-- 5. hr_employee 扩展字段
ALTER TABLE `hr_employee`
  ADD COLUMN `workweek_config_id` bigint DEFAULT NULL COMMENT '休息日配置ID（关联sys_workweek_config.id）' AFTER `org_id`,
  ADD COLUMN `special_schedule` JSON NULL COMMENT '特殊排班JSON（格式：{"2026-10-01":"休息","2026-10-02":"上班"}）' AFTER `workweek_config_id`,
  ADD COLUMN `exempt_attendance` tinyint NOT NULL DEFAULT 0 COMMENT '是否免考勤 0参与 1不参与（不参与考勤的员工同步时默认为满勤）' AFTER `special_schedule`;

-- 6. hr_post 扩展字段
ALTER TABLE `hr_post`
  ADD COLUMN `workweek_config_id` bigint DEFAULT NULL COMMENT '岗位默认休息日配置ID' AFTER `dept_id`,
  ADD COLUMN `shift_type` tinyint DEFAULT NULL COMMENT '班次类型 1标准工时 2早班 3晚班 4夜班 5综合工时' AFTER `workweek_config_id`;

-- 7. sys_org 扩展字段
ALTER TABLE `sys_org`
  ADD COLUMN `default_workweek_config_id` bigint DEFAULT 1 COMMENT '默认休息日配置ID' AFTER `org_type`;

-- 8. sys_config 新增考勤参数
INSERT INTO `sys_config` (`company_id`, `config_key`, `config_value`, `config_name`, `remark`, `create_by`, `create_time`, `is_delete`) VALUES
-- 迟到旷工阈值
(0, 'attendance.late_threshold', '30', '迟到认定阈值', '超过此分钟数认定为迟到，默认30分钟', 0, NOW(), 0),
(0, 'attendance.absent_threshold', '480', '旷工认定阈值', '迟到超过此分钟数（8小时）认定为旷工，默认480分钟', 0, NOW(), 0),
(0, 'attendance.early_threshold', '30', '早退认定阈值', '早退超过此分钟数认定为早退，默认30分钟', 0, NOW(), 0),
-- 正常打卡时间范围
(0, 'attendance.normal_clockin_start', '08:30', '正常打卡上班时间', '此时间前打卡为正常，默认08:30', 0, NOW(), 0),
(0, 'attendance.normal_clockout_end', '18:30', '正常打卡下班时间', '此时间后打卡为正常，默认18:30', 0, NOW(), 0),
-- 异常提醒阈值
(0, 'attendance.absent_reminder_threshold', '3', '连续缺卡提醒阈值', '连续缺卡超过此天数触发异常提醒，默认3天', 0, NOW(), 0),
(0, 'attendance.late_frequent_threshold', '5', '月度迟到频繁阈值', '月迟到超过此次数标记为频繁，默认5次', 0, NOW(), 0),
-- 班次时间配置
(0, 'attendance.shift_morning_start', '08:00', '早班上班时间', '早班正常打卡时间上限，默认08:00', 0, NOW(), 0),
(0, 'attendance.shift_morning_end', '17:00', '早班下班时间', '早班正常打卡时间下限，默认17:00', 0, NOW(), 0),
(0, 'attendance.shift_evening_start', '14:00', '晚班上班时间', '晚班正常打卡时间上限，默认14:00', 0, NOW(), 0),
(0, 'attendance.shift_evening_end', '23:00', '晚班下班时间', '晚班正常打卡时间下限，默认23:00', 0, NOW(), 0),
(0, 'attendance.shift_night_start', '22:00', '夜班上班时间', '夜班正常打卡时间上限，默认22:00', 0, NOW(), 0),
(0, 'attendance.shift_night_end', '07:00', '夜班下班时间', '夜班正常打卡时间下限（次日），默认07:00', 0, NOW(), 0),
-- 功能开关
(0, 'attendance.exempt_enabled', '1', '免考勤功能开关', '0关闭 1启用，控制是否允许设置免考勤员工', 0, NOW(), 0),
(0, 'attendance.reminder_enabled', '1', '考勤异常提醒开关', '0关闭 1启用', 0, NOW(), 0),
(0, 'attendance.reminder_method', 'system', '异常提醒方式', 'system=系统消息 wechat=企业微信 email=邮件', 0, NOW(), 0);

-- 9. 菜单权限初始化（对齐实际表结构）
-- 实际菜单树：
--   107 = 人力资源（目录）
--     111 = 考勤管理（菜单页面）
--       123 = 同步考勤（按钮，已存在）
--       124 = 导出（按钮，已存在）
--   最大菜单ID=222，新菜单从223开始
--   角色：7=业务操作员（主用），4=子公司经理

-- 9.1 新增考勤异常管理子菜单（挂载在考勤管理111下）
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `path`, `permission`, `menu_type`, `icon`, `sort_order`, `visible`, `create_by`, `create_time`, `is_delete`) VALUES
(111, '考勤异常管理', '/hr/attendance/exception', 'hr:attendance:exception:view', 2, 'Bell', 1, 1, 1, NOW(), 0),
(111, '考勤配置管理', '/hr/config/attendance', 'hr:config:workweek', 2, 'Setting', 2, 1, 1, NOW(), 0);

-- 9.2 新增按钮权限（挂载在考勤管理111下）
-- 注意：123=同步考勤、124=导出已存在，此处新增其他按钮
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `path`, `permission`, `menu_type`, `icon`, `sort_order`, `visible`, `create_by`, `create_time`, `is_delete`) VALUES
(111, '同步预览', NULL, 'hr:attendance:preview', 2, '', 3, 1, 1, NOW(), 0),
(111, '异常查看', NULL, 'hr:attendance:exception:view', 2, '', 4, 1, 1, NOW(), 0),
(111, '异常处理', NULL, 'hr:attendance:exception:handle', 2, '', 5, 1, 1, NOW(), 0);

-- 9.3 新增配置管理子菜单及按钮权限
-- config菜单id=225（上次INSERT返回的last_insert_id）
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `path`, `permission`, `menu_type`, `icon`, `sort_order`, `visible`, `create_by`, `create_time`, `is_delete`) VALUES
(107, '考勤参数配置', '/hr/config/attendance', 'hr:config:attendance', 2, 'Setting', 3, 1, 1, NOW(), 0);

-- 考勤参数配置下的按钮权限（假设上条INSERT返回id=226）
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `path`, `permission`, `menu_type`, `icon`, `sort_order`, `visible`, `create_by`, `create_time`, `is_delete`) VALUES
(226, '参数编辑', NULL, 'hr:config:attendance:edit', 2, '', 1, 1, 1, NOW(), 0),
(226, '班次配置', NULL, 'hr:config:shift', 2, '', 2, 1, 1, NOW(), 0),
(226, '休息日配置', NULL, 'hr:config:workweek', 2, '', 3, 1, 1, NOW(), 0),
(226, '节假日配置', NULL, 'hr:config:holiday', 2, '', 4, 1, 1, NOW(), 0);

-- 9.4 关联到HR角色（使用实际角色ID：7=业务操作员，4=子公司经理）
-- 获取刚插入的菜单ID后执行以下关联
-- 建议分步执行：先执行上面的INSERT，再执行 SELECT LAST_INSERT_ID(); 获取ID，然后关联
INSERT INTO `sys_role_menu_rel` (`role_id`, `menu_id`) VALUES
-- 业务操作员（role_id=7）关联所有考勤菜单
(7, 223), (7, 224), (7, 225), (7, 226),
(7, 227), (7, 228), (7, 229), (7, 230), (7, 231),
-- 子公司经理（role_id=4）关联查看权限
(4, 223), (4, 224), (4, 225), (4, 226),
(4, 227), (4, 228), (4, 229), (4, 230), (4, 231);

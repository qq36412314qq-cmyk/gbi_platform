# AI_EDIT_LOG

## 【2026-09-15 14:00:00】薪酬核算考勤联动规范文档创建
- 用户原始需求：将薪酬考勤联动计算方案整理为完整规范文档，写入 gbi_platform_docs
- 修改涉及文件：
  - gbi_platform_docs/薪酬核算考勤联动规范.md（新建）
  - gbi_platform_docs/attachments/sql/upgrade_v2.10_attendance_salary.sql（新建）
- 变更摘要：
  1. 创建《薪酬核算考勤联动规范 v1.0》，涵盖计算公式、员工豁免机制、最低工资保护、请假类型设计、风险点、部署注意事项
  2. 创建对应 SQL 升级脚本 upgrade_v2.10，4步依次扩展 hr_attendance_record、hr_salary_rule、hr_salary_month、sys_city
  3. 核心公式：日薪=basicSalary/21.75，旷工/迟到/早退/事假分项计算，新员工首月不满整月豁免最低工资保护
  4. 新增 hr_attendance_record.leave_type 枚举字段（0无薪事假~7调休假），leave_days_detail JSON字段
  5. 新增 hr_salary_rule.skip_attendance/latePenaltyRate/earlyPenaltyRate 字段
- 风险与注意事项：
  - 存量 hr_attendance_record 数据 leave_type 默认为0，需HR人工审核后批量修正
  - sys_city.min_wage 未配置时不对该城市执行最低工资保护
  - 本任务为文档+SQL设计，尚未涉及 Java/Vue 代码修改

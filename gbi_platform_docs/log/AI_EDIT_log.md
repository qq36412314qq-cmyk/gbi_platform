# AI_EDIT_LOG

## 【2026-09-17 11:30:00】完善组织架构升级方案文档（对齐实际数据库结构）

- 用户原始需求：更新方案文档，hr_employee 表已存在且字段与文档不符，生成修正后的 SQL 升级语句
- 修改涉及文件：
  - `gbi_platform_docs/tmp/组织架构岗位员工体系升级方案-v1.0.md`
- 变更摘要：
  1. 1.1节：员工档案状态改为"表已存在"，补充 id_card_no VARCHAR(64) 差异说明
  2. 1.2节：数据模型图修正，标注 user_id 已存在、employee_id 待补充
  3. 二节：升级目标第2条标记为已完成
  4. 三节 3.2：将"新建 hr_employee"改为"表已存在，无需重建"，输出实际字段对照表及现有索引列表
  5. 三节 3.3：sys_user.employee_id 位置修正为 AFTER status
  6. 附录SQL：移除 CREATE TABLE hr_employee，版本升级为 v1.1，仅保留 ALTER TABLE sys_user / hr_post / sys_org 升级语句
  7. 阶段一：标注 hr_employee 表无需操作
- 风险与注意事项：
  - hr_employee.user_id 已关联 sys_user，双向关联补全只需 ALTER sys_user 增加 employee_id
  - id_card_no 实际为 VARCHAR(64)，非方案中误写的 VARCHAR(18)

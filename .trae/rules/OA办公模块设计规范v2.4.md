# OA办公模块设计规范 v2.4

> 文档版本：v2.4
> 创建日期：2026-08-24
> 适用阶段：集团多业态一体化管控系统二期
> 关联文档：集团多业态一体化管控系统业务设计与全维度测试标准文档.md

---

## 1. 模块概述

OA 办公模块是集团多业态一体化管控系统的核心支撑模块，提供请假、会议室、公告、打卡、工作汇报等日常办公功能，统一复用审批流程引擎（flow_engine）实现单据的流转审批。

### 1.1 设计原则

| 原则 | 说明 |
|------|------|
| 多租户隔离 | 所有业务数据通过 company_id 字段实现子公司级数据隔离 |
| 审批引擎复用 | 请假、工作汇报等需审批单据统一走 flowEngineService.submit() 流程 |
| 快照字段 | 关键文本字段（姓名、名称等）在创建时固化，避免关联数据变更导致历史数据失真 |
| 逻辑删除 | 所有业务表采用 is_delete 软删除，保留完整审计轨迹 |
| 权限分级 | 操作类接口统一通过 @PreAuthorize + PermissionConst 权限标识控制 |

### 1.2 技术栈

| 层级 | 技术选型 |
|------|---------|
| 后端 | Java 21 + Spring Boot 3.5.3 + MyBatis-Plus |
| 前端 | Vue 3 + TypeScript + Element Plus + Vite |
| 数据库 | MySQL 8.0（utf8mb4 字符集） |
| 审批引擎 | 统一 flow_engine 服务（流程定义/实例/任务） |

### 1.3 模块边界

OA办公模块（oa_* 系列表）
  请假管理（oa_leave_apply）
  会议室管理（oa_meeting_room / oa_meeting_booking）
  公告管理（oa_announcement / oa_announcement_read）
  打卡管理（oa_clock_record）
  工作汇报（oa_work_report）

---

## 7. 升级脚本

### 7.1 初始安装

install.sql 中已包含所有 OA 表的建表语句。

### 7.2 增量升级

upgrade_v2.4_oa_module.sql（新建）包含完整建表语句，执行方式：
  mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v2.4_oa_module.sql

### 7.3 幂等性

所有建表语句均使用 DROP TABLE IF EXISTS + CREATE TABLE 模式，可重复执行。

---

## 8. 注意事项

1. **companyId 必传**：所有 OA 业务操作的请求参数中必须携带 companyId，由前端从 userStore.userInfo?.companyId 获取，不可从 URL 参数推测

2. **userStore 属性名**：用户信息存储在 userStore.userInfo（非 userStore.user），包含 id、companyId、username、realName 等字段

3. **时间冲突校验**：会议室预约必须在后端校验时间段冲突，防止并发重复预约（事务内查询 + 插入）

4. **打卡状态计算**：打卡记录的 status 字段由后端根据 clock_time 与标准工作时间（09:00 / 18:00）对比计算，前端不自行判断

5. **公告已读统计**：read_count 字段在用户浏览公告详情时自动累加，通过 oa_announcement_read 表去重统计（同一用户同一条公告只记录一次）

6. **快照字段原则**：userName、roomName、stallName 等展示字段在创建时写入快照，后续源数据变更不影响历史快照数据

7. **int 常量比较**：CommonConst 中定义的 int 类型常量（如 APPLY_STATUS_DRAFT = 0）不可使用 .equals() 方法比较，必须使用 Objects.equals() 或 == 运算符

8. **流程定义编码**：当前请假和工作汇报均复用 FLOW_DEF_CONTRACT，后续需在流程引擎管理界面创建专用流程定义并更新 CommonConst 常量

---

## 9. 文件清单

| 文件 | 路径 | 说明 |
|------|------|------|
| 数据库升级脚本 | gbi_platform_docs/attachments/sql/upgrade_v2.4_oa_module.sql | 7张OA表建表语句 |
| 后端实体 | gbi_platform_server/.../entity/Oa*.java（7个） | 请假/会议室/预约/公告/已读/打卡/工作汇报 |
| 后端Mapper | gbi_platform_server/.../mapper/Oa*.java（7个） | MyBatis-Plus Mapper 接口 |
| 后端VO | gbi_platform_server/.../vo/Oa*.java（6个） | 返回给前端的视图对象 |
| 后端Service | gbi_platform_server/.../service/OaService.java | 业务接口 |
| 后端ServiceImpl | gbi_platform_server/.../service/impl/OaServiceImpl.java | 业务实现 |
| 后端Controller | gbi_platform_server/.../controller/oa/OaController.java | REST 接口 |
| 前端API | gbi_platform_admin/src/api/oa.ts | 所有 OA 接口函数 |
| 前端请假页 | gbi_platform_admin/src/views/oa/leave.vue | 请假管理页面 |
| 前端公告页 | gbi_platform_admin/src/views/oa/announcement.vue | 公告管理页面 |
| 前端会议室页 | gbi_platform_admin/src/views/oa/meetingRoom.vue | 会议室管理页面 |
| 前端打卡页 | gbi_platform_admin/src/views/oa/clock.vue | 打卡管理页面 |
| 前端汇报页 | gbi_platform_admin/src/views/oa/workReport.vue | 工作汇报页面 |
| 路由配置 | gbi_platform_admin/src/router/index.ts | OA 模块路由已添加 |
| 本规范文档 | gbi_platform_docs/OA办公模块设计规范v2.4.md | 本文档 |
---

## 4. 前端页面规范

### 4.1 路由配置

| 路径 | 路由名称 | 页面标题 | 权限标识 | 组件路径 |
|------|---------|---------|---------|---------|
| /oa | — | OA办公 | — | @/components/layout/index.vue |
| /oa/leave | OaLeave | 请假管理 | oa:leave:list | @/views/oa/leave.vue |
| /oa/announcement | OaAnnouncement | 公告管理 | oa:announcement:list | @/views/oa/announcement.vue |
| /oa/meetingRoom | OaMeetingRoom | 会议室管理 | oa:meeting-room:list | @/views/oa/meetingRoom.vue |
| /oa/clock | OaClock | 打卡管理 | oa:clock:list | @/views/oa/clock.vue |
| /oa/workReport | OaWorkReport | 工作汇报 | oa:work-report:list | @/views/oa/workReport.vue |

### 4.2 公共组件使用规范

| 组件 | 用途 | 使用示例 |
|------|------|---------|
| TablePage | 表格分页容器 | <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData"> |
| SearchBar | 搜索筛选栏 | <SearchBar :model="query" @search="loadData" @reset="handleReset"> |
| AuthBtn | 权限按钮 | <AuthBtn permission="oa:leave:add" type="primary" @click="openDialog">申请请假</AuthBtn> |
| useTable | 分页查询 hook | const { query, records, total, loading, loadData, resetQuery } = useTable(apiFunc, initQuery) |
| useUserStore | 用户状态 | const userStore = useUserStore(); userStore.userInfo?.companyId |

### 4.3 API 模块规范

所有 OA 接口统一写在 src/api/oa.ts，按功能模块分组，每组包含接口函数和 TypeScript 类型定义。

类型定义命名规则：
  查询参数：XxxQueryDTO
  提交参数：SubmitXxxDTO
  返回数据：XxxVO

接口函数命名规则：
  列表查询：getXxxPageApi
  详情查询：getXxxApi
  新增：addXxxApi / submitXxxApi
  编辑：updateXxxApi
  删除：deleteXxxApi
  状态操作：publishXxxApi / cancelXxxApi / recallXxxApi

### 4.4 状态映射规范

前端展示状态时使用 text 后缀字段（由后端 VO 提供），避免前端硬编码枚举映射。如 row.applyStatusText、row.clockTypeText。

状态码对照表：
  请假/工作汇报：0=草稿 1=审批中 2=通过 3=驳回 4=作废
  公告：0=草稿 1=已发布 2=已撤回
  会议室预约：0=已预约 1=已取消 2=已完成
  打卡状态：1=正常 2=迟到 3=早退 4=缺卡
---

## 5. 审批流程集成规范

### 5.1 审批触发场景

| 业务 | 触发时机 | 流程定义编码 | 审批通过后 | 审批驳回后 |
|------|---------|------------|----------|----------|
| 请假申请 | submitLeave | FLOW_DEF_CONTRACT | applyStatus=2 | applyStatus=3 |
| 工作汇报 | submitWorkReport | FLOW_DEF_CONTRACT | reportStatus=2 | reportStatus=3 |

注：当前暂复用 FLOW_DEF_CONTRACT 流程定义，后续需在流程引擎管理界面创建专用流程定义（如 FLOW_DEF_LEAVE、FLOW_DEF_WORK_REPORT）。

### 5.2 流程集成代码示例

```java
Long instanceId = flowEngineService.submit(
    CommonConst.FLOW_DEF_CONTRACT,
    "leave_apply",
    String.valueOf(apply.getId()),
    apply.getApplyUserName() + " 申请" + getLeaveTypeText(apply.getLeaveType())
        + "假：" + apply.getStartDate() + " 至 " + apply.getEndDate()
);
apply.setFlowInstanceId(instanceId);
apply.setApplyStatus(CommonConst.APPLY_STATUS_AUDITING);
```

### 5.3 审批中心入口

| 页面 | 路径 | 组件 | 权限标识 |
|------|------|------|---------|
| 待办处理 | /flow/task | oa/flow/todoList.vue | flow:task:list |
| 我的申请 | /flow/apply | oa/flow/applyList.vue | flow:apply:list |
| 流程定义 | /flow/definition | oa/flow/defList.vue | flow:def:list |
| 流程实例 | /flow/instance | oa/flow/instanceList.vue | flow:instance:list |

---

## 6. 权限常量规范

在 PermissionConst.java 中统一声明 OA 模块权限常量：

```java
/* OA办公模块 */
String OA_LEAVE_LIST    = "oa:leave:list";
String OA_LEAVE_ADD     = "oa:leave:add";
String OA_LEAVE_CANCEL  = "oa:leave:cancel";
String OA_MEETING_ROOM_LIST    = "oa:meeting-room:list";
String OA_MEETING_ROOM_ADD     = "oa:meeting-room:add";
String OA_MEETING_ROOM_EDIT    = "oa:meeting-room:edit";
String OA_MEETING_ROOM_DELETE  = "oa:meeting-room:delete";
String OA_MEETING_BOOKING_LIST    = "oa:meeting-booking:list";
String OA_MEETING_BOOKING_ADD     = "oa:meeting-booking:add";
String OA_MEETING_BOOKING_CANCEL  = "oa:meeting-booking:cancel";
String OA_ANNOUNCEMENT_LIST     = "oa:announcement:list";
String OA_ANNOUNCEMENT_ADD      = "oa:announcement:add";
String OA_ANNOUNCEMENT_PUBLISH  = "oa:announcement:publish";
String OA_ANNOUNCEMENT_RECALL   = "oa:announcement:recall";
String OA_CLOCK_LIST = "oa:clock:list";
String OA_WORK_REPORT_LIST     = "oa:work-report:list";
String OA_WORK_REPORT_ADD      = "oa:work-report:add";
String OA_WORK_REPORT_CANCEL   = "oa:work-report:cancel";
```
---

## 3. 后端接口规范

### 3.1 基础约定

- 统一前缀：/oa
- 统一返回：Result<T>（code=200 表示成功）
- 统一分页：PageVO<T>（records / total / pageNum / pageSize / pages）
- 统一权限：@PreAuthorize("hasPermission(...)")
- 多租户过滤：通过 MyBatis-Plus 多租户拦截器自动注入 company_id 条件

### 3.2 请假管理

| 方法 | 路径 | 权限标识 | 说明 |
|------|------|---------|------|
| GET | /oa/leave/page | oa:leave:list | 分页查询 |
| GET | /oa/leave/{id} | oa:leave:list | 详情 |
| POST | /oa/leave/submit | oa:leave:add | 提交申请（自动发起审批） |
| POST | /oa/leave/cancel/{id} | oa:leave:cancel | 撤销申请 |

查询参数：pageNum、pageSize、leaveType（可选）、applyStatus（可选）、applyUserId（可选）

提交参数（SubmitLeaveDTO）：
  companyId, applyUserId, applyUserName, leaveType, startDate, endDate, leaveDays, reason, remark

业务规则：
  提交后状态变为审批中（1），自动调用 flowEngineService.submit() 发起审批
  仅草稿（0）或审批中（1）状态的申请可撤销
  撤销后状态变为作废（4）

### 3.3 会议室管理

| 方法 | 路径 | 权限标识 | 说明 |
|------|------|---------|------|
| GET | /oa/meeting/room/page | oa:meeting-room:list | 分页查询 |
| POST | /oa/meeting/room/add | oa:meeting-room:add | 添加会议室 |
| POST | /oa/meeting/room/update | oa:meeting-room:edit | 编辑会议室 |
| POST | /oa/meeting/room/delete/{id} | oa:meeting-room:delete | 删除会议室 |

### 3.4 会议室预约

| 方法 | 路径 | 权限标识 | 说明 |
|------|------|---------|------|
| GET | /oa/meeting/booking/page | oa:meeting-booking:list | 分页查询 |
| POST | /oa/meeting/booking/book | oa:meeting-booking:add | 预约会议室 |
| POST | /oa/meeting/booking/cancel/{id} | oa:meeting-booking:cancel | 取消预约 |

时间冲突校验规则：
  同一会议室、同一日期、时间段有重叠则拒绝预约
  重叠判断：新预约 startTime < 已有预约 endTime AND 新预约 endTime > 已有预约 startTime

### 3.5 公告管理

| 方法 | 路径 | 权限标识 | 说明 |
|------|------|---------|------|
| GET | /oa/announcement/page | oa:announcement:list | 分页查询 |
| GET | /oa/announcement/{id} | oa:announcement:list | 详情 |
| POST | /oa/announcement/create | oa:announcement:add | 创建公告（草稿状态） |
| POST | /oa/announcement/publish/{id} | oa:announcement:publish | 发布公告 |
| POST | /oa/announcement/recall/{id} | oa:announcement:recall | 撤回公告 |
| POST | /oa/announcement/read/{id} | 无需权限 | 标记已读 |
| GET | /oa/announcement/{id}/read-stats | oa:announcement:list | 已读统计 |

发布规则：
  仅草稿状态（0）的公告可以发布
  发布后状态变为已发布（1），记录 publish_time
  已发布状态的公告可以撤回，撤回后状态变为已撤回（2）
  全员公告（publish_type=1）：read_count 自动等于公司用户总数
  指定范围公告：read_count 等于目标范围内的已读人数

### 3.6 打卡管理

| 方法 | 路径 | 权限标识 | 说明 |
|------|------|---------|------|
| GET | /oa/clock/page | oa:clock:list | 分页查询 |
| POST | /oa/clock/clockIn | 无需权限（登录即可） | 打卡 |

查询参数：pageNum、pageSize、userId（可选）、clockType（可选）、startDate（可选）、endDate（可选）

打卡状态判断规则：
  上班打卡（clock_type=1）：早于 09:00 为正常（1），晚于 09:00 为迟到（2）
  下班打卡（clock_type=2）：晚于 18:00 为正常（1），早于 18:00 为早退（3）
  当日无打卡记录为缺卡（4）

### 3.7 工作汇报

| 方法 | 路径 | 权限标识 | 说明 |
|------|------|---------|------|
| GET | /oa/work-report/page | oa:work-report:list | 分页查询 |
| GET | /oa/work-report/{id} | oa:work-report:list | 详情 |
| POST | /oa/work-report/submit | oa:work-report:add | 提交汇报（自动发起审批） |
| POST | /oa/work-report/cancel/{id} | oa:work-report:cancel | 撤销汇报 |

业务规则：
  提交后状态变为审批中（1），自动调用 flowEngineService.submit() 发起审批
  仅草稿（0）或审批中（1）状态的汇报可撤销
  撤销后状态变为作废（4）

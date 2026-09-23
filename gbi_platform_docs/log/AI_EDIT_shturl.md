## 【2026-09-22 20:20:00】修复员工档案模块编译错误
- 用户原始需求：后端编译出错，要求只修复员工档案相关内容，不影响其他模块
- 修改涉及文件：
  - gbi_platform_server/src/main/java/com/gbi/platform/common/constant/CommonConst.java（追加HR相关常量）
  - gbi_platform_server/src/main/java/com/gbi/platform/common/constant/PermissionConst.java（追加HR/铺位/租户权限常量）
  - gbi_platform_server/src/main/java/com/gbi/platform/entity/SysUser.java（新增avatar/employeeId字段）
  - gbi_platform_server/src/main/java/com/gbi/platform/util/AuditLogUtil.java（修复setOperName→setOperUserName）
  - gbi_platform_server/src/main/java/com/gbi/platform/service/impl/hr/HrTransferServiceImpl.java（从git恢复原始版本）
- 变更摘要：
  1. CommonConst追加：OPER_TYPE_FILE_DELETE、OPER_TYPE_FILE_PREVIEW、OPER_TYPE_FILE_UPLOAD、MODULE_FILE
  2. PermissionConst追加：HR_ATTENDANCE_LIST/SYNC、HR_SALARY_MONTH_LIST、HR_SOCIAL_LIST、OA_ANNOUNCEMENT_*、LEASE_STALL_*、TENANT_*
  3. SysUser新增avatar（头像）和employeeId（关联员工ID）字段及getter/setter
  4. AuditLogUtil修复字段名不匹配
  5. HrTransferServiceImpl恢复到git原始版本（不含多余的rejected方法）
- 风险与注意事项：所有其他模块代码保持不变，仅补充了员工档案相关的缺失常量和字段

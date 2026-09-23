package com.gbi.platform.common.constant;

/**
 * 权限标识常量，与前端路由 meta.permission、sys_menu.permission 对齐
 * 禁止硬编码权限字符串，对齐《后端编码规范》九
 *
 * @author gbi
 */
public final class PermissionConst {

    private PermissionConst() {
    }

    /** 超级管理员通配权限 */
    public static final String ALL_PERMISSION = "*:*:*";

    /* ------------------------------ 组织 ------------------------------ */
    public static final String ORG_LIST = "org:list";
    public static final String ORG_ADD = "org:add";
    public static final String ORG_EDIT = "org:edit";
    public static final String ORG_DELETE = "org:delete";

    /* ------------------------------ 用户 ------------------------------ */
    public static final String USER_LIST = "user:list";
    public static final String USER_ADD = "user:add";
    public static final String USER_EDIT = "user:edit";
    public static final String USER_DELETE = "user:delete";
    public static final String USER_RESET_PWD = "user:resetPwd";
    public static final String USER_CHANGE_STATUS = "user:changeStatus";

    /* ------------------------------ 角色 ------------------------------ */
    public static final String ROLE_LIST = "role:list";
    public static final String ROLE_ADD = "role:add";
    public static final String ROLE_EDIT = "role:edit";
    public static final String ROLE_DELETE = "role:delete";
    public static final String ROLE_MENU = "role:menu";

    /* ------------------------------ 菜单 ------------------------------ */
    public static final String MENU_LIST = "menu:list";
    public static final String MENU_ADD = "menu:add";
    public static final String MENU_EDIT = "menu:edit";
    public static final String MENU_DELETE = "menu:delete";

    /* ------------------------------ 字典 ------------------------------ */
    public static final String DICT_LIST = "dict:list";
    public static final String DICT_ADD = "dict:add";
    public static final String DICT_EDIT = "dict:edit";
    public static final String DICT_DELETE = "dict:delete";

    /* ------------------------------ 参数配置 ------------------------------ */
    public static final String CONFIG_LIST = "config:list";
    public static final String CONFIG_ADD = "config:add";
    public static final String CONFIG_EDIT = "config:edit";
    public static final String CONFIG_DELETE = "config:delete";

    /* ------------------------------ 城市字典 ------------------------------ */
    public static final String HR_CITY_LIST = "hr:city:list";
    public static final String HR_CITY_ADD = "hr:city:add";
    public static final String HR_CITY_EDIT = "hr:city:edit";
    public static final String HR_CITY_DELETE = "hr:city:delete";

    /* ------------------------------ 险种字典 ------------------------------ */
    public static final String HR_INSURANCE_LIST = "hr:insurance:list";
    public static final String HR_INSURANCE_ADD = "hr:insurance:add";
    public static final String HR_INSURANCE_EDIT = "hr:insurance:edit";
    public static final String HR_INSURANCE_DELETE = "hr:insurance:delete";

    /* ------------------------------ 行业字典 ------------------------------ */
    public static final String HR_INDUSTRY_LIST = "hr:industry:list";
    public static final String HR_INDUSTRY_ADD = "hr:industry:add";
    public static final String HR_INDUSTRY_EDIT = "hr:industry:edit";
    public static final String HR_INDUSTRY_DELETE = "hr:industry:delete";

    /* ------------------------------ 社保参数配置 ------------------------------ */
    public static final String HR_SOCIAL_PARAM_LIST = "hr:social:param:list";
    public static final String HR_SOCIAL_PARAM_ADD = "hr:social:param:add";
    public static final String HR_SOCIAL_PARAM_EDIT = "hr:social:param:edit";
    public static final String HR_SOCIAL_PARAM_ACTIVATE = "hr:social:param:activate";
    public static final String HR_SOCIAL_PARAM_DEACTIVATE = "hr:social:param:deactivate";
    public static final String HR_SOCIAL_PARAM_DELETE = "hr:social:param:delete";

    /* ------------------------------ 公积金参数配置 ------------------------------ */
    public static final String HR_HOUSING_FUND_LIST = "hr:housing:fund:list";
    public static final String HR_HOUSING_FUND_ADD = "hr:housing:fund:add";
    public static final String HR_HOUSING_FUND_EDIT = "hr:housing:fund:edit";
    public static final String HR_HOUSING_FUND_ACTIVATE = "hr:housing:fund:activate";
    public static final String HR_HOUSING_FUND_DELETE = "hr:housing:fund:delete";

    /* ------------------------------ 社保核算明细 ------------------------------ */
    public static final String HR_SOCIAL_CALC_LIST = "hr:social:calc:list";
    public static final String HR_SOCIAL_CALC_EXPORT = "hr:social:calc:export";

    /* ------------------------------ 年度基数重算 ------------------------------ */
    public static final String HR_RECALC_TRIGGER = "hr:recalc:trigger";
    public static final String HR_RECALC_EXECUTE = "hr:recalc:execute";

    /* ------------------------------ UI主题 ------------------------------ */
    public static final String THEME_LIST = "theme:list";
    public static final String THEME_EDIT = "theme:edit";

    /* ------------------------------ 审计日志 ------------------------------ */
    public static final String AUDIT_LIST = "audit:list";
    public static final String AUDIT_EXPORT = "audit:export";

    /* ------------------------------ 权限二级复核 ------------------------------ */
    public static final String PERMISSION_AUDIT_LIST = "permission:audit:list";
    public static final String PERMISSION_AUDIT_AUDIT = "permission:audit:audit";

    /* ------------------------------ 水电物业-设备管理 ------------------------------ */
    public static final String WATER_ELEC_LIST = "waterElec:list";
    public static final String WATER_ELEC_ADD = "waterElec:add";
    public static final String WATER_ELEC_EDIT = "waterElec:edit";
    public static final String WATER_ELEC_DELETE = "waterElec:delete";
    public static final String WATER_ELEC_READ = "waterElec:read";
    public static final String WATER_ELEC_SWITCH = "waterElec:switch";

    /* ------------------------------ 水电物业-账单管理 ------------------------------ */
    public static final String WATER_ELEC_BILL_LIST = "waterElec:bill:list";
    public static final String WATER_ELEC_BILL_GENERATE = "waterElec:bill:generate";

    /* ------------------------------ 水电物业-缴费管理 ------------------------------ */
    public static final String WATER_ELEC_PAY_LIST = "waterElec:pay:list";
    public static final String WATER_ELEC_PAY_ADD = "waterElec:pay:add";
    public static final String WATER_ELEC_PAY_REFUND = "waterElec:pay:refund";

    /* ------------------------------ 市场管理 ------------------------------ */
    public static final String MARKET_LIST = "market:list";
    public static final String MARKET_ADD = "market:add";
    public static final String MARKET_EDIT = "market:edit";
    public static final String MARKET_DELETE = "market:delete";

    /* ------------------------------ 物业费-账单管理 ------------------------------ */
    public static final String PROPERTY_FEE_BILL_LIST = "property:feeBill:list";
    public static final String PROPERTY_FEE_BILL_GENERATE = "property:feeBill:generate";
    public static final String PROPERTY_FEE_BILL_GENERATE_SINGLE = "property:feeBill:generateSingle";
    public static final String PROPERTY_FEE_BILL_AUTO_GENERATE = "property:feeBill:autoGenerate";
    public static final String PROPERTY_FEE_BILL_EXPORT = "property:feeBill:export";

    /* ------------------------------ 未支付订单聚合查询 ------------------------------ */
    public static final String PROPERTY_UNPAID_BILL_LIST = "property:unpaidBill:list";

    /* ------------------------------ 物业费-缴费管理 ------------------------------ */
    public static final String PROPERTY_FEE_PAY_ADD = "property:feePay:add";
    public static final String UNIFIED_PAY_ADD = "property:unifiedPay:add";

    /* ------------------------------ 财务流水 ------------------------------ */
    public static final String FINANCE_FLOW_LIST = "finance:flow:list";
    public static final String FINANCE_FLOW_EXPORT = "finance:flow:export";
    public static final String FINANCE_REPORT_LIST = "finance:report:list";
    public static final String FINANCE_FLOW_APPROVE = "finance:flow:approve";
    public static final String FINANCE_FLOW_RED_FLUSH = "finance:flow:redFlush";
    public static final String FINANCE_FLOW_VOID = "finance:flow:void";

    /* ------------------------------ 缴费单 ------------------------------ */
    public static final String FINANCE_PAY_ORDER_LIST = "finance:payOrder:list";
    public static final String FINANCE_PAY_ORDER_VOID = "finance:payOrder:void";

    /* ------------------------------ 优惠申请 ------------------------------ */
    public static final String DISCOUNT_APPLY_LIST = "discount:apply:list";
    public static final String DISCOUNT_APPLY_ADD = "discount:apply:add";
    public static final String DISCOUNT_APPLY_AUDIT = "discount:apply:audit";

    /* ------------------------------ 优惠政策 ------------------------------ */
    public static final String DISCOUNT_POLICY_LIST = "discount:policy:list";
    public static final String DISCOUNT_POLICY_ADD = "discount:policy:add";
    public static final String DISCOUNT_POLICY_EDIT = "discount:policy:edit";
    public static final String DISCOUNT_POLICY_DELETE = "discount:policy:delete";

    /* ------------------------------ 租赁-类别管理 ------------------------------ */
    public static final String LEASE_CATEGORY_LIST = "lease:category:list";
    public static final String LEASE_CATEGORY_ADD = "lease:category:add";
    public static final String LEASE_CATEGORY_EDIT = "lease:category:edit";
    public static final String LEASE_CATEGORY_DELETE = "lease:category:delete";

    /* ------------------------------ 租赁合同 ------------------------------ */
    public static final String LEASE_CONTRACT_LIST = "lease:contract:list";
    public static final String LEASE_CONTRACT_ADD = "lease:contract:add";
    public static final String LEASE_CONTRACT_EDIT = "lease:contract:edit";
    public static final String LEASE_CONTRACT_DELETE = "lease:contract:delete";
    public static final String LEASE_CONTRACT_TERMINATE = "lease:contract:terminate";

    /* ------------------------------ 自定义收费类型 ------------------------------ */
    public static final String FEE_ITEM_LIST = "fee:item:list";
    public static final String FEE_ITEM_ADD = "fee:item:add";
    public static final String FEE_ITEM_EDIT = "fee:item:edit";
    public static final String FEE_ITEM_DELETE = "fee:item:delete";

    /* ------------------------------ 自定义收费规则 ------------------------------ */
    public static final String FEE_RULE_LIST = "fee:rule:list";
    public static final String FEE_RULE_ADD = "fee:rule:add";
    public static final String FEE_RULE_EDIT = "fee:rule:edit";

    /* ------------------------------ 统一审批工作流 ------------------------------ */
    public static final String FLOW_TASK_LIST = "flow:task:list";
    public static final String FLOW_TASK_HANDLE = "flow:task:handle";
    public static final String FLOW_TASK_URGE = "flow:task:urge";
    public static final String FLOW_APPLY_LIST = "flow:apply:list";
    public static final String FLOW_APPLY_CANCEL = "flow:apply:cancel";
    public static final String FLOW_DEF_LIST = "flow:def:list";
    public static final String FLOW_DEF_ADD = "flow:def:add";
    public static final String FLOW_DEF_EDIT = "flow:def:edit";
    public static final String FLOW_DEF_DELETE = "flow:def:delete";
    public static final String FLOW_INSTANCE_LIST = "flow:instance:list";

    /* ------------------------------ OA请假管理 ------------------------------ */
    public static final String OA_LEAVE_LIST = "oa:leave:list";
    public static final String OA_LEAVE_ADD = "oa:leave:add";
    public static final String OA_LEAVE_CANCEL = "oa:leave:cancel";

    /* ------------------------------ OA会议室管理 ------------------------------ */
    public static final String OA_MEETING_ROOM_LIST = "oa:meeting:room:list";
    public static final String OA_MEETING_ROOM_ADD = "oa:meeting:room:add";
    public static final String OA_MEETING_ROOM_EDIT = "oa:meeting:room:edit";
    public static final String OA_MEETING_ROOM_DELETE = "oa:meeting:room:delete";

    /* ------------------------------ OA会议室预约 ------------------------------ */
    public static final String OA_MEETING_BOOKING_LIST = "oa:meeting:booking:list";
    public static final String OA_MEETING_BOOKING_ADD = "oa:meeting:booking:add";
    public static final String OA_MEETING_BOOKING_CANCEL = "oa:meeting:booking:cancel";

    /* ------------------------------ 应收应付计划 ------------------------------ */
    public static final String PLAN_RECVPAY_LIST = "plan:recvpay:list";
    public static final String PLAN_RECVPAY_GENERATE = "plan:recvpay:generate";
    public static final String PLAN_RECVPAY_ADJUST = "plan:recvpay:adjust";
    public static final String PLAN_RECVPAY_TERMINATE = "plan:recvpay:terminate";
    public static final String PLAN_RECVPAY_RECONCILE = "plan:recvpay:reconcile";
    public static final String PLAN_RECVPAY_EXPORT = "plan:recvpay:export";

    /* ------------------------------ 客户端设备校验 ------------------------------ */
    public static final String DEVICE_AUTH_LIST = "device:auth:list";
    public static final String DEVICE_AUTH_ADD = "device:auth:add";
    public static final String DEVICE_AUTH_DELETE = "device:auth:delete";
    public static final String DEVICE_AUTH_TOGGLE = "device:auth:toggle";

    /* ------------------------------ 工作台 ------------------------------ */
    public static final String DASHBOARD_VIEW = "dashboard:view";

    /* ------------------------------ 文件上传存储 ------------------------------ */
    public static final String FILE_UPLOAD = "base:file:upload";
    public static final String FILE_PREVIEW = "base:file:preview";
    public static final String FILE_DELETE = "base:file:delete";
    public static final String FILE_LIST = "base:file:list";

    /* ------------------------------ 人力资源-入职申请 ------------------------------ */
    public static final String HR_ENTRY_LIST = "hr:entry:list";
    public static final String HR_ENTRY_ADD = "hr:entry:add";
    public static final String HR_ENTRY_REVOKE = "hr:entry:revoke";

    /* ------------------------------ 人力资源-员工档案 ------------------------------ */
    public static final String HR_EMPLOYEE_LIST = "hr:employee:list";
    public static final String HR_EMPLOYEE_ADD = "hr:employee:add";
    public static final String HR_EMPLOYEE_EDIT = "hr:employee:edit";
    public static final String HR_EMPLOYEE_DELETE = "hr:employee:delete";
    public static final String HR_EMPLOYEE_EXPORT = "hr:employee:export";

    /* ------------------------------ 人力资源-工作经历 ------------------------------ */
    public static final String HR_WORK_EXP_LIST = "hr:workExp:list";
    public static final String HR_WORK_EXP_ADD = "hr:workExp:add";
    public static final String HR_WORK_EXP_EDIT = "hr:workExp:edit";
    public static final String HR_WORK_EXP_DELETE = "hr:workExp:delete";

    /* ------------------------------ 人力资源-学业经历 ------------------------------ */
    public static final String HR_EDU_EXP_LIST = "hr:eduExp:list";
    public static final String HR_EDU_EXP_ADD = "hr:eduExp:add";
    public static final String HR_EDU_EXP_EDIT = "hr:eduExp:edit";
    public static final String HR_EDU_EXP_DELETE = "hr:eduExp:delete";

    /* ------------------------------ 人力资源-薪酬级别 ------------------------------ */
    public static final String HR_SALARY_GRADE_LIST = "hr:salary:grade:list";
    public static final String HR_SALARY_GRADE_ADD = "hr:salary:grade:add";
    public static final String HR_SALARY_GRADE_EDIT = "hr:salary:grade:edit";
    public static final String HR_SALARY_GRADE_DISABLE = "hr:salary:grade:disable";

    /* ------------------------------ 人力资源-批量调薪 ------------------------------ */
    public static final String HR_SALARY_BATCH_LIST = "hr:salary:batch:list";
    public static final String HR_SALARY_BATCH_SUBMIT = "hr:salary:batch:submit";

    /* ------------------------------ 人力资源-考勤管理 ------------------------------ */
    public static final String HR_ATTENDANCE_LIST = "hr:attendance:list";
    public static final String HR_ATTENDANCE_SYNC = "hr:attendance:sync";

    /* ------------------------------ 人力资源-薪酬档案 ------------------------------ */
    public static final String HR_SALARY_ARCHIVE_LIST = "hr:salary:archive:list";
    public static final String HR_SALARY_ARCHIVE_ADD = "hr:salary:archive:add";
    public static final String HR_SALARY_ARCHIVE_EDIT = "hr:salary:archive:edit";
    public static final String HR_SALARY_ARCHIVE_DELETE = "hr:salary:archive:delete";

    /* ------------------------------ 人力资源-薪酬月度 ------------------------------ */
    public static final String HR_SALARY_MONTH_LIST = "hr:salary:month:list";
    public static final String HR_SALARY_MONTH_GENERATE = "hr:salary:month:generate";
    public static final String HR_SALARY_MONTH_PAY = "hr:salary:month:pay";
    public static final String HR_SALARY_MONTH_EXPORT = "hr:salary:month:export";

    /* ------------------------------ 人力资源-社保管理 ------------------------------ */
    public static final String HR_SOCIAL_LIST = "hr:social:list";
    /* 薪酬体系升级 v2.3 */
    public static final String HR_SALARY_RULE_LIST = "hr:salary:rule:list";
    public static final String HR_SALARY_RULE_ADD = "hr:salary:rule:add";
    public static final String HR_SALARY_RULE_EDIT = "hr:salary:rule:edit";
    public static final String HR_SALARY_RULE_DISABLE = "hr:salary:rule:disable";
    public static final String HR_SALARY_RULE_SUBMIT = "hr:salary:rule:submit";
    public static final String HR_SALARY_BATCH_ADD = "hr:salary:batch:add";
    public static final String HR_SALARY_YEAR_BONUS_LIST = "hr:salary:yearBonus:list";
    public static final String HR_SALARY_YEAR_BONUS_ADD = "hr:salary:yearBonus:add";
    public static final String HR_SALARY_YEAR_BONUS_EDIT = "hr:salary:yearBonus:edit";
    public static final String HR_SALARY_YEAR_BONUS_SUBMIT = "hr:salary:yearBonus:submit";
    public static final String HR_SOCIAL_ADD = "hr:social:add";
    public static final String HR_SOCIAL_EDIT = "hr:social:edit";
    public static final String HR_SOCIAL_DELETE = "hr:social:delete";

    /* ------------------------------ OA公告管理 ------------------------------ */
    public static final String OA_ANNOUNCEMENT_LIST = "oa:announcement:list";
    public static final String OA_ANNOUNCEMENT_ADD = "oa:announcement:add";
    public static final String OA_ANNOUNCEMENT_EDIT = "oa:announcement:edit";
    public static final String OA_ANNOUNCEMENT_DELETE = "oa:announcement:delete";

    /* ------------------------------ 铺位管理 ------------------------------ */
    public static final String LEASE_STALL_LIST = "lease:stall:list";
    public static final String LEASE_STALL_ADD = "lease:stall:add";
    public static final String LEASE_STALL_EDIT = "lease:stall:edit";
    public static final String LEASE_STALL_DELETE = "lease:stall:delete";

    /* ------------------------------ 租户管理 ------------------------------ */
    public static final String TENANT_LIST = "tenant:list";
    public static final String TENANT_ADD = "tenant:add";

    /* ------------------------------ 人力资源模块 ------------------------------ */
    public static final String HR_ORG_LIST = "hr:org:list";
    public static final String HR_ORG_ADD = "hr:org:add";
    public static final String HR_ORG_EDIT = "hr:org:edit";
    public static final String HR_ORG_DELETE = "hr:org:delete";
    public static final String HR_POST_LIST = "hr:post:list";
    public static final String HR_POST_ADD = "hr:post:add";
    public static final String HR_POST_EDIT = "hr:post:edit";
    public static final String HR_POST_DELETE = "hr:post:delete";

    /* ------------------------------ 人力资源-组织岗位（前端兼容） ------------------------------ */
    public static final String HR_ORG_POST_LIST = "hr:org:post:list";
    public static final String HR_ORG_POST_ADD = "hr:org:post:add";
    public static final String HR_ORG_POST_EDIT = "hr:org:post:edit";
    public static final String HR_ORG_POST_DELETE = "hr:org:post:delete";

    /* ------------------------------ 人力资源-转正申请 ------------------------------ */
    public static final String HR_REGULAR_LIST = "hr:regular:list";
    public static final String HR_REGULAR_ADD = "hr:regular:add";
    public static final String HR_REGULAR_REVOKE = "hr:regular:revoke";

    /* ------------------------------ 人力资源-调岗申请 ------------------------------ */
    public static final String HR_TRANSFER_LIST = "hr:transfer:list";
    public static final String HR_TRANSFER_ADD = "hr:transfer:add";
    public static final String HR_TRANSFER_REVOKE = "hr:transfer:revoke";

    /* ------------------------------ 人力资源-离职申请 ------------------------------ */
    public static final String HR_RESIGN_LIST = "hr:resign:list";
    public static final String HR_RESIGN_ADD = "hr:resign:add";
    public static final String HR_RESIGN_REVOKE = "hr:resign:revoke";

    /* ------------------------------ 人力资源-考勤管理（导出） ------------------------------ */
    public static final String HR_ATTENDANCE_EXPORT = "hr:attendance:export";

}

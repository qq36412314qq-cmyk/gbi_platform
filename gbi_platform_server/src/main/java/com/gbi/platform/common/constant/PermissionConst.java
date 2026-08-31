package com.gbi.platform.common.constant;

/**
 * 权限标识常量��与前端路由 meta.permission、sys_menu.permission 对齐
 * 禁止硬编码权限字符串��对齐《后端编码规范》九��
 *
 * @author gbi
 */
public final class PermissionConst {

    private PermissionConst() {
    }

    /** 超级管理员通配权限 */
    public static final String ALL_PERMISSION = "*:*:*";

    /* ------------------------------ �� ------------------------------ */
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

    /* ------------------------------ UI�题 ------------------------------ */
    public static final String THEME_LIST = "theme:list";
    public static final String THEME_EDIT = "theme:edit";

    /* ------------------------------ 审计日� ------------------------------ */
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
    public static final String PROPERTY_FEE_BILL_EXPORT = "property:feeBill:export";

    /* ------------------------------ 未支付订单聚合查询 ------------------------------ */
    public static final String PROPERTY_UNPAID_BILL_LIST = "property:unpaidBill:list";

    /* ------------------------------ 物业费-缴费管理 ------------------------------ */
    public static final String PROPERTY_FEE_PAY_ADD = "property:feePay:add";
    public static final String UNIFIED_PAY_ADD = "property:unifiedPay:add";
    public static final String FINANCE_FLOW_LIST = "finance:flow:list";
    public static final String FINANCE_FLOW_EXPORT = "finance:flow:export";

    /* ------------------------------ 财务汇总 ------------------------------ */
    public static final String FINANCE_SUMMARY_LIST = "finance:summary:list";

    /* ------------------------------ 优惠申请 ------------------------------ */
    public static final String DISCOUNT_APPLY_LIST = "discount:apply:list";
    public static final String DISCOUNT_APPLY_ADD = "discount:apply:add";
    public static final String DISCOUNT_APPLY_AUDIT = "discount:apply:audit";

    /* ------------------------------ 优惠�策 ------------------------------ */
    public static final String DISCOUNT_POLICY_LIST = "discount:policy:list";
    public static final String DISCOUNT_POLICY_ADD = "discount:policy:add";
    public static final String DISCOUNT_POLICY_EDIT = "discount:policy:edit";
    public static final String DISCOUNT_POLICY_DELETE = "discount:policy:delete";

    /* ------------------------------ OA-公告 ------------------------------ */
    public static final String OA_ANNOUNCEMENT_LIST = "oa:announcement:list";
    public static final String OA_ANNOUNCEMENT_ADD = "oa:announcement:add";
    public static final String OA_ANNOUNCEMENT_EDIT = "oa:announcement:edit";
    public static final String OA_ANNOUNCEMENT_DELETE = "oa:announcement:delete";
    public static final String OA_ANNOUNCEMENT_PUBLISH = "oa:announcement:publish";

    /* ------------------------------ OA-打卡 ------------------------------ */
    public static final String OA_CLOCK_LIST = "oa:clock:list";
    public static final String OA_CLOCK_ADD = "oa:clock:add";
    public static final String OA_CLOCK_EDIT = "oa:clock:edit";
    public static final String OA_CLOCK_DELETE = "oa:clock:delete";

    /* ------------------------------ OA-工作日� ------------------------------ */
    public static final String OA_WORK_REPORT_LIST = "oa:work-report:list";
    public static final String OA_WORK_REPORT_ADD = "oa:work-report:add";
    public static final String OA_WORK_REPORT_CANCEL = "oa:work-report:cancel";

    /* ------------------------------ 人力资源模块 ------------------------------ */
    public static final String HR_ORG_LIST = "hr:org:list";
    public static final String HR_ORG_ADD = "hr:org:add";
    public static final String HR_ORG_EDIT = "hr:org:edit";
    public static final String HR_ORG_DELETE = "hr:org:delete";
    public static final String HR_POST_LIST = "hr:post:list";
    public static final String HR_POST_ADD = "hr:post:add";
    public static final String HR_POST_EDIT = "hr:post:edit";
    public static final String HR_POST_DELETE = "hr:post:delete";
    public static final String HR_EMPLOYEE_LIST = "hr:employee:list";
    public static final String HR_EMPLOYEE_ADD = "hr:employee:add";
    public static final String HR_EMPLOYEE_EDIT = "hr:employee:edit";
    public static final String HR_EMPLOYEE_DELETE = "hr:employee:delete";
    public static final String HR_EMPLOYEE_EXPORT = "hr:employee:export";
    public static final String HR_ENTRY_LIST = "hr:entry:list";
    public static final String HR_ENTRY_ADD = "hr:entry:add";
    public static final String HR_REGULAR_LIST = "hr:regular:list";
    public static final String HR_REGULAR_ADD = "hr:regular:add";
    public static final String HR_TRANSFER_LIST = "hr:transfer:list";
    public static final String HR_TRANSFER_ADD = "hr:transfer:add";
    public static final String HR_RESIGN_LIST = "hr:resign:list";
    public static final String HR_RESIGN_ADD = "hr:resign:add";
    public static final String HR_ATTENDANCE_LIST = "hr:attendance:list";
    public static final String HR_ATTENDANCE_SYNC = "hr:attendance:sync";
    public static final String HR_ATTENDANCE_EXPORT = "hr:attendance:export";
    public static final String HR_SALARY_ARCHIVE_LIST = "hr:salary:archive:list";
    public static final String HR_SALARY_ARCHIVE_ADD = "hr:salary:archive:add";
    public static final String HR_SALARY_ARCHIVE_EDIT = "hr:salary:archive:edit";
    public static final String HR_SALARY_MONTH_LIST = "hr:salary:month:list";
    public static final String HR_SALARY_MONTH_GENERATE = "hr:salary:month:generate";
    public static final String HR_SALARY_MONTH_PAY = "hr:salary:month:pay";
    public static final String HR_SALARY_MONTH_EXPORT = "hr:salary:month:export";
    public static final String HR_SOCIAL_LIST = "hr:social:list";
    public static final String HR_SOCIAL_ADD = "hr:social:add";
    public static final String HR_SOCIAL_EDIT = "hr:social:edit";
    public static final String HR_SOCIAL_DELETE = "hr:social:delete";

    /* ------------------------------ 租户管理 ------------------------------ */
    public static final String TENANT_LIST = "tenant:list";
    public static final String TENANT_ADD = "tenant:add";
    public static final String TENANT_EDIT = "tenant:edit";
    public static final String TENANT_DELETE = "tenant:delete";

    /* ------------------------------ 租赁摊位 ------------------------------ */
    public static final String LEASE_STALL_LIST = "lease:stall:list";
    public static final String LEASE_STALL_ADD = "lease:stall:add";
    public static final String LEASE_STALL_EDIT = "lease:stall:edit";
        public static final String LEASE_STALL_DELETE = "lease:stall:delete";

    /* ------------------------------ 租赁分� ------------------------------ */
    public static final String LEASE_CATEGORY_LIST = "lease:category:list";
    public static final String LEASE_CATEGORY_ADD = "lease:category:add";
    public static final String LEASE_CATEGORY_EDIT = "lease:category:edit";
    public static final String LEASE_CATEGORY_DELETE = "lease:category:delete";

    /* ------------------------------ 财务汇总 ------------------------------ */
    public static final String FINANCE_REPORT_LIST = "finance:report:list";
    public static final String FINANCE_FLOW_APPROVE = "finance:flow:approve";
    public static final String FINANCE_FLOW_RED_FLUSH = "finance:flow:redFlush";
    public static final String FINANCE_FLOW_VOID = "finance:flow:void";

    /* ------------------------------ 自定义收费�型 ------------------------------ */
    public static final String FEE_ITEM_LIST = "fee:item:list";
    public static final String FEE_ITEM_ADD = "fee:item:add";
    public static final String FEE_ITEM_EDIT = "fee:item:edit";
    public static final String FEE_ITEM_DELETE = "fee:item:delete";

    /* ------------------------------ 自定义收费规则 ------------------------------ */
    public static final String FEE_RULE_LIST = "fee:rule:list";
    public static final String FEE_RULE_ADD = "fee:rule:add";
    public static final String FEE_RULE_EDIT = "fee:rule:edit";


    /* ------------------------------ �一审批中僼�flow��------------------------------ */
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


    /* ------------------------------ 应收应�计划��plan��------------------------------ */
    public static final String PLAN_RECVPAY_LIST = "plan:recvpay:list";
    public static final String PLAN_RECVPAY_GENERATE = "plan:recvpay:generate";
    public static final String PLAN_RECVPAY_ADJUST = "plan:recvpay:adjust";
    public static final String PLAN_RECVPAY_TERMINATE = "plan:recvpay:terminate";
    public static final String PLAN_RECVPAY_RECONCILE = "plan:recvpay:reconcile";
    public static final String PLAN_RECVPAY_EXPORT = "plan:recvpay:export";

    /* ------------------------------ 租赁合同 ------------------------------ */
    public static final String LEASE_CONTRACT_LIST = "lease:contract:list";
    public static final String LEASE_CONTRACT_ADD = "lease:contract:add";
    public static final String LEASE_CONTRACT_TERMINATE = "lease:contract:terminate";
}





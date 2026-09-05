package com.gbi.platform.common.constant;

/**
 * 通用常量定义
 *
 * @author gbi
 */
public final class CommonConst {

    private CommonConst() {
    }

    /** 集团总公司 companyId（0 表示集团全局） */
    public static final long COMPANY_ROOT = 0L;

    /** 超级管理员角色编码 */
    public static final String ROLE_SUPER_ADMIN = "super_admin";

    /** 审计角色编码 */
    public static final String ROLE_AUDITOR = "auditor";

    /* ------------------------------ 状态 ------------------------------ */
    /** 启用/正常 */
    public static final int STATUS_ENABLED = 1;
    /** 禁用 */
    public static final int STATUS_DISABLED = 0;

    /* ------------------------------ 权限复核状态 ------------------------------ */
    /** 待审核 */
    public static final int AUDIT_STATUS_PENDING = 0;
    /** 审核通过 */
    public static final int AUDIT_STATUS_PASS = 1;
    /** 审核驳回 */
    public static final int AUDIT_STATUS_REJECT = 2;

    /* ------------------------------ 审计日志操作类型 ------------------------------ */
    public static final String OPER_TYPE_ADD = "新增";
    public static final String OPER_TYPE_UPDATE = "编辑";
    public static final String OPER_TYPE_DELETE = "删除";
    public static final String OPER_TYPE_BIND = "绑定";
    public static final String OPER_TYPE_EXPORT = "导出";
    public static final String OPER_TYPE_AUDIT = "审核";
    public static final String OPER_TYPE_LOGIN = "登录";
    public static final String OPER_TYPE_LOGOUT = "退出";
    public static final String OPER_TYPE_READ = "抄表";
    public static final String OPER_TYPE_SWITCH = "通断";
    public static final String OPER_TYPE_PAY = "缴费";
    public static final String OPER_TYPE_REFUND = "退费";
    public static final String OPER_TYPE_TERMINATE = "退租";
    public static final String OPER_TYPE_VOID = "作废";
    public static final String OPER_TYPE_SUBMIT = "提交";
    public static final String OPER_TYPE_ADJUST = "调账";
    public static final String OPER_TYPE_RED_REVERSAL = "红冲";
    public static final String OPER_TYPE_PAY_SALARY = "薪资发放";

    /* ------------------------------ 审计日志模块 ------------------------------ */
    public static final String MODULE_BASE = "base";
    public static final String MODULE_ORG = "org";
    public static final String MODULE_SYS = "sys";
    public static final String MODULE_WATER_ELEC = "waterElec";
    public static final String MODULE_PROPERTY_FEE = "propertyFee";
    public static final String MODULE_FINANCE = "finance";
    public static final String MODULE_TENANT = "stall_tenant";
    public static final String MODULE_LEASE_STALL = "stall_lease";
    public static final String MODULE_LEASE_CATEGORY = "stall_category";
    public static final String MODULE_LEASE_CONTRACT = "stall_contract";
    public static final String MODULE_MARKET = "market_info";
    public static final String MODULE_FEE_ITEM = "fee_item";
    public static final String MODULE_FEE_RULE = "fee_rule";
    public static final String MODULE_PLAN = "recv_pay_plan";
    public static final String MODULE_FLOW = "flow_engine";
    public static final String MODULE_DISCOUNT = "discount";
    public static final String MODULE_WRITEOFF = "writeoff";

    /* ------------------------------ 人力资源模块审计日志 ------------------------------ */
    public static final String MODULE_HR = "hr";
    public static final String MODULE_HR_ORG = "hr_org";
    public static final String MODULE_HR_EMPLOYEE = "hr_employee";
    public static final String MODULE_HR_TRANSFER = "hr_transfer";
    public static final String MODULE_HR_ATTENDANCE = "hr_attendance";
    public static final String MODULE_HR_SALARY = "hr_salary";
    public static final String MODULE_HR_SOCIAL = "hr_social";

    /* ------------------------------ 流程定义编码 ------------------------------ */
    public static final String FLOW_DEF_CONTRACT = "contract";
    public static final String FLOW_DEF_CONTRACT_DISCOUNT = "contract_discount";
    public static final String FLOW_DEF_CONTRACT_TERMINATE = "contract_terminate";
    public static final String FLOW_DEF_PLAN_ADJUST = "plan_adjust";
    public static final String FLOW_DEF_REIMBURSE = "reimburse";
    public static final String FLOW_DEF_REIMBURSE_LARGE = "reimburse_large";
    public static final String FLOW_DEF_PURCHASE = "purchase";
    public static final String FLOW_DEF_PURCHASE_LARGE = "purchase_large";
    public static final String FLOW_DEF_FINANCE_RED_FLUSH = "finance_red_flush";

    /* ------------------------------ 人力资源模块流程定义 ------------------------------ */
    public static final String FLOW_DEF_HR_ENTRY = "hr_entry";
    public static final String FLOW_DEF_HR_REGULAR = "hr_regular";
    public static final String FLOW_DEF_HR_TRANSFER = "hr_transfer";
    public static final String FLOW_DEF_HR_RESIGN = "hr_resign";

    /* ------------------------------ 申请状态 ------------------------------ */
    public static final int APPLY_STATUS_DRAFT = 0;
    public static final int APPLY_STATUS_AUDITING = 1;
    public static final int APPLY_STATUS_PASS = 2;
    public static final int APPLY_STATUS_REJECT = 3;
    public static final int APPLY_STATUS_VOID = 4;

    /* ------------------------------ 财务流水单号 ------------------------------ */
    public static final String FLOW_NO_PREFIX = "YO";

    /* ------------------------------ Redis key 前缀 ------------------------------ */
    public static final String REDIS_KEY_DICT = "gbi:dict:";

    /* ------------------------------ 账单缴费状态 ------------------------------ */
    public static final int BILL_PAY_STATUS_UNPAID = 0;
    public static final int BILL_PAY_STATUS_PART = 1;
    public static final int BILL_PAY_STATUS_PAID = 2;

    /* ------------------------------ 业务类型 ------------------------------ */
    public static final String BIZ_TYPE_RENT = "rent";
    public static final String BIZ_TYPE_PROPERTY_FEE = "property_fee";
    public static final String BIZ_TYPE_WATER_ELEC = "water_elec";
    public static final String BIZ_TYPE_DEPOSIT = "deposit";
    public static final String BIZ_TYPE_MARKETING = "marketing";

    /* ------------------------------ 收费类别 ------------------------------ */
    public static final int FEE_CATEGORY_RENT = 1;
    public static final int FEE_CATEGORY_PROPERTY = 2;
    public static final int FEE_CATEGORY_WATER = 3;
    public static final int FEE_CATEGORY_ELEC = 4;
    public static final int FEE_CATEGORY_DEPOSIT = 5;

    /* ------------------------------ 计算方式 ------------------------------ */
    public static final String CALC_MODE_AREA = "area";

    /* ------------------------------ 计量表状态 ------------------------------ */
    public static final int METER_STATUS_POWER_ON = 1;

    /* ------------------------------ 计量表类型 ------------------------------ */
    public static final int METER_TYPE_WATER = 1;
    public static final int METER_TYPE_ELEC = 2;

    /* ------------------------------ 缴费记录类型 ------------------------------ */
    public static final int PAY_RECORD_TYPE_PAY = 1;
    public static final int PAY_RECORD_TYPE_REFUND = 2;

    /* ------------------------------ 缴费方式 ------------------------------ */
    public static final int PAY_TYPE_CASH = 3;
    public static final int PAY_TYPE_WECHAT = 1;
    public static final int PAY_TYPE_ALIPAY = 2;

    /* ------------------------------ 付款方类型 ------------------------------ */
    public static final int PAYER_TYPE_PERSON = 1;
    public static final int PAYER_TYPE_COMPANY = 2;

    /* ------------------------------ 计划来源 ------------------------------ */
    public static final String PLAN_SOURCE_CONTRACT = "contract";
    public static final String PLAN_SOURCE_BILL = "bill";

    /* ------------------------------ 计划业务类型 ------------------------------ */
    public static final String PLAN_BIZ_RENT = "rent";
    public static final String PLAN_BIZ_PROPERTY = "property";
    public static final String PLAN_BIZ_WATER_ELEC = "water_elec";
    public static final String PLAN_BIZ_DEPOSIT = "deposit";
    public static final String PLAN_BIZ_FEE_BILL = "fee_bill";
    public static final String PLAN_BIZ_REIMBURSE = "reimburse";
    public static final String PLAN_BIZ_PURCHASE = "purchase";

    /* ------------------------------ 计划方向 ------------------------------ */
    public static final int PLAN_DIRECTION_RECEIVE = 1;

    /* ------------------------------ 计划周期 ------------------------------ */
    public static final int PLAN_PERIOD_ONCE = 4;
    public static final int PLAN_PERIOD_MONTH = 1;
    public static final int PLAN_PERIOD_ONCE_NO = 3;

    /* ------------------------------ 计划状态 ------------------------------ */
    public static final int PLAN_STATUS_PENDING = 0;
    public static final int PLAN_STATUS_DONE = 1;
    public static final int PLAN_STATUS_PART = 2;
    public static final int PLAN_STATUS_OVERDUE = 3;
    public static final int PLAN_STATUS_TERMINATED = 4;
    public static final int PLAN_STATUS_VOID = 5;

    /* ------------------------------ 计划冲红标志 ------------------------------ */
    public static final int PLAN_RED_FLAG_NORMAL = 0;
    public static final int PLAN_RED_FLAG_REVERSAL = 1;

    /* ------------------------------ 核销类型 ------------------------------ */
    public static final int WRITEOFF_TYPE_PAY = 1;
    public static final int WRITEOFF_TYPE_REFUND = 2;
    public static final int WRITEOFF_TYPE_RED_REVERSAL = 3;

    /* ------------------------------ 退款状态 ------------------------------ */
    public static final int REFUND_STATUS_NONE = 0;
    public static final int REFUND_STATUS_DONE = 1;

    /* ------------------------------ 租户类型 ------------------------------ */
    public static final int TENANT_TYPE_PERSONAL = 1;
    public static final int TENANT_TYPE_COMPANY = 2;
    public static final int TENANT_TYPE_INDIVIDUAL = 3;

    /* ------------------------------ 铺位状态 ------------------------------ */
    public static final int STALL_STATUS_EMPTY = 0;
    public static final int STALL_STATUS_RENTED = 1;
    public static final int STALL_STATUS_OVERDUE = 2;
    public static final int STALL_STATUS_EXPIRE_SOON = 3;

    /* ------------------------------ 合同状态 ------------------------------ */
    public static final int CONTRACT_STATUS_SIGNING = 0;    // 签约中（合同创建后、缴费完成前）
    public static final int CONTRACT_STATUS_EFFECTIVE = 1;  // 生效中（缴费完成后）
    public static final int CONTRACT_STATUS_TERMINATED = 2;
    public static final int CONTRACT_STATUS_EXPIRED = 3;

    /* ------------------------------ 优惠类型 ------------------------------ */
    public static final int DISCOUNT_TYPE_WAIVE = 1;
    public static final int DISCOUNT_TYPE_RATE = 2;
    public static final int DISCOUNT_TYPE_DEDUCT = 3;
    public static final int DISCOUNT_TYPE_COMBO = 4;

    /* ------------------------------ 优惠审批标识 ------------------------------ */
    public static final int APPLY_NEED_AUDIT_NO = 0;
    public static final int APPLY_NEED_AUDIT_YES = 1;

    /* ------------------------------ 流程动作 ------------------------------ */
    public static final String FLOW_ACTION_SUBMIT = "submit";
    public static final String FLOW_ACTION_PASS = "pass";
    public static final String FLOW_ACTION_REJECT = "reject";
    public static final String FLOW_ACTION_TRANSFER = "transfer";
    public static final String FLOW_ACTION_REVOKE = "revoke";
    public static final String FLOW_ACTION_TERMINATE = "terminate";
    public static final String FLOW_ACTION_URGE = "urge";
    public static final String FLOW_ACTION_CC = "cc";

    /* ------------------------------ 流程实例状态 ------------------------------ */
    public static final int FLOW_STATUS_RUNNING = 0;
    public static final int FLOW_STATUS_PASS = 1;
    public static final int FLOW_STATUS_REJECT = 2;
    public static final int FLOW_STATUS_REVOKE = 3;
    public static final int FLOW_STATUS_TERMINATE = 4;
    public static final int FLOW_STATUS_VOIDED = 5;
    public static final int FLOW_STATUS_NORMAL = 6;
    public static final int FLOW_STATUS_FLUSHING = 7;
    public static final int FLOW_STATUS_FLUSHED = 8;
    /* ------------------------------ 财务流水状态（biz_finance_flow.flow_status）------------------------------ */
    /** 正常 */
    public static final int FINANCE_FLOW_STATUS_NORMAL = 1;
    /** 冲红中 */
    public static final int FINANCE_FLOW_STATUS_FLUSHING = 2;
    /** 已冲红 */
    public static final int FINANCE_FLOW_STATUS_FLUSHED = 3;
    /** 已作废 */
    public static final int FINANCE_FLOW_STATUS_VOIDED = 4;

    /* ------------------------------ 流程类型 ------------------------------ */
    public static final int FLOW_TYPE_INCOME = 1;
    public static final int FLOW_TYPE_EXPENSE = 2;

    /* ------------------------------ 流程任务状态 ------------------------------ */
    public static final int TASK_STATUS_PENDING = 0;
    public static final int TASK_STATUS_DONE = 1;
    public static final int TASK_STATUS_CANCELED = 2;
    public static final int TASK_STATUS_TRANSFERRED = 3;

    /* ------------------------------ 覆盖标志 ------------------------------ */
    public static final int OVERRIDE_FLAG_NO = 0;

    /* ------------------------------ 配置阈值key ------------------------------ */
    public static final String CONFIG_DISCOUNT_WAIVE_MONTHS_LIMIT = "discount.waive_months_limit";
    public static final String CONFIG_DISCOUNT_MIN_RATE_LIMIT = "discount.min_rate_limit";
    public static final String CONFIG_DISCOUNT_MAX_DEDUCT_LIMIT = "discount.max_deduct_limit";
    public static final String CONFIG_DISCOUNT_CONTRACT_RATIO_LIMIT = "discount.contract_ratio_limit";
    public static final String CONFIG_PLAN_ADJUST_AMOUNT_LIMIT = "plan.adjust_amount_limit";
    public static final String CONFIG_PLAN_OVERDUE_REMIND_DAYS = "plan.overdue_remind_days";
    public static final String CONFIG_REIMBURSE_AMOUNT_LIMIT = "reimburse.amount_limit";
    public static final String CONFIG_PURCHASE_AMOUNT_LIMIT = "purchase.amount_limit";
}

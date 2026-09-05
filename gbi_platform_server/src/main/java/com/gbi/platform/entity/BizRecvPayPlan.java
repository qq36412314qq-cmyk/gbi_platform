package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 应收应付计划主表实体：biz_recv_pay_plan
 * 全系统唯一应收应付台账，账单/核销/对账统一依赖本表；
 * 金额公式 plan_amount = original_amount - discount_amount + adjust_amount；
 * 红冲计划 red_flag=1、金额为负、orig_plan_id 溯源被冲销计划
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_recv_pay_plan")
public class BizRecvPayPlan extends BaseEntity {

    /** 所属子公司ID */
    private Long companyId;

    /** 计划编号 AR-RENT-公司-日期-序号 / AP-REIMB-公司-日期-序号 */
    private String planNo;

    /** 方向 1应收 2应付（二期报销采购） */
    private Integer direction;

    /** 业务类型 rent/deposit/property/fee_bill/reimburse/purchase */
    private String bizType;

    /** 来源单据类型 contract/reimburse/purchase/bill */
    private String sourceType;

    /** 来源单据ID */
    private String sourceId;

    /** 市场ID */
    private Long marketId;

    /** 摊位ID */
    private Long stallId;

    /** 租户ID */
    private Long tenantId;

    /** 商户ID */
    private Long merchantId;

    /** 期次标识 月度yyyy-MM 季度yyyy-Qn 年度yyyy 一次性once */
    private String periodNo;

    /** 周期类型 1按月 2按季 3按年 4一次性 */
    private Integer periodType;

    /** 应收/应付日期 */
    private LocalDate dueDate;

    /** 原应收金额（不含优惠） */
    private BigDecimal originalAmount;

    /** 优惠金额（来自优惠申请快照） */
    private BigDecimal discountAmount;

    /** 人工调账金额（正负均可，0=未调账） */
    private BigDecimal adjustAmount;

    /** 计划应收 = 原价 - 优惠 + 调账 */
    private BigDecimal planAmount;

    /** 已收/已付金额（核销累加） */
    private BigDecimal paidAmount;

    /** 未收/未付金额 = plan_amount - paid_amount */
    private BigDecimal unpaidAmount;

    /** 计划状态 0待执行 1部分核销 2完成 3逾期 4作废 5终止 */
    private Integer planStatus;

    /** 红冲标记 0正常计划 1反向冲销计划（金额为负） */
    private Integer redFlag;

    /** 溯源计划ID（红冲计划指向被冲销计划；单据变更重建新旧互指） */
    private Long origPlanId;

    /** 逾期天数（定时任务刷新） */
    private Integer overdueDays;

    /** 关联审批实例ID（终止/作废/大额调账审批） */
    private Long flowInstanceId;

    /** 关联优惠申请ID */
    private Long discountApplyId;

    /** 备注（单据变更/红冲原因留痕） */
    private String remark;
}
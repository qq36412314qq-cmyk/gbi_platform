package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 统一收费账单实体：finance_fee_pay_bill
 * 聚合物业费/水电费/租赁费/幼儿园费等所有费用类型，biz_type 区分类型
 * 新增费用类型只需写入对应 biz_type 记录，无需改表结构
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_fee_pay_bill")
public class BizFeeBill extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 所属子公司ID */
    private Long companyId;

    /** 业务类型：property_fee=物业费 water_elec=水电费 rent=租赁费 kindergarten=幼儿园费 */
    private String bizType;

    /** 铺位ID */
    private Long stallId;

    /** 商户ID */
    private Long merchantId;

    /** 账单周期标识 月yyyy-MM / 季yyyy-Qn / 年yyyy / 一次性once */
    private String billMonth;

    /** 生成账单的规则ID（finance_fee_rule，锁定后仅记录不回溯） */
    private Long ruleId;

    /** 关联应收应付计划ID（可空） */
    private Long planId;

    /** 账单周期 0不使用 1按年 2按月 3按日 */
    private Integer periodType;

    /** 应收原价合计 */
    private BigDecimal originalAmount;

    /** 优惠减免金额合计 */
    private BigDecimal discountAmount;

    /** 人工调账金额（正负均可，0=未调账） */
    private BigDecimal adjustAmount;

    /** 实际应收 = 原价 - 优惠 + 调账 */
    private BigDecimal realAmount;

    /** 缴费状态 0待缴 1部分缴费 2已缴 3已退费 4已冲红 5已作废 */
    private Integer payStatus;

    /** 缴费完成时间 */
    private LocalDateTime payTime;

    /** 账单锁定 1锁定（生成即锁定，规则变更不回溯） */
    private Integer lockedFlag;

    /** 账单备注 */
    private String remark;

    /** 源账单ID（property_water_elec_bill.id / property_fee_bill.id，用于幂等和追溯） */
    private Long sourceBillId;

    /** 关联优惠策略ID */
    private Long policyId;

    /** 关联优惠申请ID */
    private Long applyId;

    /** 优惠类型快照 */
    private Integer discountType;
}
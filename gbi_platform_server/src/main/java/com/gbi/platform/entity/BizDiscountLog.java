package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 优惠执行日志表实体：finance_discount_log
 * 记录优惠计算执行的明细，支持账单优惠溯源
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_discount_log")
public class BizDiscountLog extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 所属子公司ID */
    private Long companyId;

    /** 关联优惠申请ID */
    private Long applyId;

    /** 关联优惠策略ID */
    private Long policyId;

    /** 关联账单ID */
    private Long billId;

    /** 源账单ID（property_fee_bill.id 等） */
    private Long sourceBillId;

    /** 业务类型 */
    private String bizType;

    /** 铺位ID */
    private Long stallId;

    /** 账单月份 */
    private String billMonth;

    /** 优惠类型 */
    private Integer discountType;

    /** 原价金额 */
    private BigDecimal originalAmount;

    /** 优惠金额 */
    private BigDecimal discountAmount;

    /** 实收金额 */
    private BigDecimal realAmount;

    /** 计算规则说明JSON */
    private String calcRule;
}
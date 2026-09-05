package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 缴费单主表实体：finance_pay_order
 * 聚合支付载体，关联各业务类型账单
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_pay_order")
public class BizPayOrder extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 所属子公司ID */
    private Long companyId;

    /** 缴费单编号 */
    private String payBillNo;

    /** 来源类型 fee_bill/water_elec */
    private String sourceType;

    /** 源账单ID（关联 biz_fee_bill.id 等） */
    private Long sourceId;

    /** 铺位ID */
    private Long stallId;

    /** 商户ID */
    private Long merchantId;

    /** 应收总额 */
    private BigDecimal totalAmount;

    /** 已缴金额 */
    private BigDecimal paidAmount;

    /** 未缴金额 */
    private BigDecimal unpaidAmount;

    /** 缴费状态 0待缴 1部分缴费 2已缴 3已退费 4已冲红 5已作废 */
    private Integer payStatus;

    /** 缴费完成时间 */
    private LocalDateTime payTime;

    /** 备注 */
    private String remark;
}

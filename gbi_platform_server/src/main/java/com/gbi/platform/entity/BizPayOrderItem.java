package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 缴费单明细实体：finance_pay_order_item
 * 快照固化字段，记录缴费单各收费项明细
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_pay_order_item")
public class BizPayOrderItem extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 缴费单ID（finance_pay_order.id） */
    private Long payBillId;

    /** 源账单ID（water_elec_bill.id / property_fee_bill.id） */
    private Long billId;

    /** 业务类型 water_elec/property_fee */
    private String bizType;

    /** 收费规则名称快照 */
    private String ruleName;

    /** 收费项名称快照（数据库列 fee_item_name） */
    @TableField("fee_item_name")
    private String feeItemType;

    /** 账期标识 yyyy-MM */
    private String billMonth;

    /** 应收金额 */
    private BigDecimal amount;

    /** 优惠抵扣金额 */
    private BigDecimal discountAmount;

    /** 已缴金额 */
    private BigDecimal paidAmount;

    /** 未缴金额 */
    private BigDecimal unpaidAmount;
}
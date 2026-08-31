package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 收付款核销分摊明细表实体：biz_finance_writeoff
 * 资金主流水与应收应付计划精确对账关系，自动对账引擎权威源；
 * writeoff_amount 正=核销入账，负=退款/红冲冲减
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_finance_writeoff")
public class BizFinanceWriteoff extends BaseEntity {

    /** 所属子公司ID */
    private Long companyId;

    /** 资金主流水ID（biz_finance_flow） */
    private Long financeFlowId;

    /** 应收应付计划ID（biz_recv_pay_plan） */
    private Long planId;

    /** 关联账单类型（water_elec/fee_bill，可空=直接核销计划） */
    private String billType;

    /** 关联账单ID（可空） */
    private Long billId;

    /** 本次分摊核销金额（正=核销入账，负=退款/红冲冲减） */
    private BigDecimal writeoffAmount;

    /** 核销类型 1缴费核销 2退款冲减 3红冲冲销 */
    private Integer writeoffType;

    /** 备注说明 */
    private String remark;
}
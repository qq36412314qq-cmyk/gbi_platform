package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 账单-应收应付计划关联中间表实体：bill_plan_rel
 * 支持一账单多计划、多计划合并账单；账单总金额 = 各 rel 分摊合计（可含未挂计划独立金额）
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_pay_plan_rel")
public class BillPlanRel extends BaseEntity {

    /** 所属子公司ID */
    private Long companyId;

    /** 账单类型 water_elec/fee_bill */
    private String billType;

    /** 账单ID */
    private Long billId;

    /** 应收应付计划ID */
    private Long planId;

    /** 该计划在本账单的分摊金额 */
    private BigDecimal splitAmount;
}
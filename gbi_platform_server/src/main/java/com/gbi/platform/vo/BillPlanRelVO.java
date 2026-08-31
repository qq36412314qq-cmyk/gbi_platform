package com.gbi.platform.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 账单-计划关联视图（一账单多计划、多计划合并账单）
 *
 * @author gbi
 */
@Data
public class BillPlanRelVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String billType;

    private Long billId;

    private Long planId;

    private BigDecimal splitAmount;
}
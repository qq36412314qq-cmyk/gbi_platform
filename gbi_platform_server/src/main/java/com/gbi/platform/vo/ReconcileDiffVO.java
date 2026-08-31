package com.gbi.platform.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 自动对账差异视图（以计划为权威源：计划剩余 = plan总金额 − sum(有效核销分摊金额)）
 *
 * @author gbi
 */
@Data
public class ReconcileDiffVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long planId;

    private String planNo;

    private Long stallId;

    private String periodNo;

    private BigDecimal planAmount;

    private BigDecimal paidAmount;

    private BigDecimal writeoffSum;

    /** 账面未收 = planAmount - paidAmount */
    private BigDecimal unpaidAmount;

    /** 计算剩余 = planAmount - writeoffSum */
    private BigDecimal computedRemaining;

    /** 差异 = computedRemaining - unpaidAmount（0 正常，非 0 对账异常） */
    private BigDecimal diff;
}
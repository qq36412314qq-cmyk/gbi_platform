package com.gbi.platform.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 优惠策略视图
 *
 * @author gbi
 */
@Data
public class DiscountPolicyVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long companyId;

    private String policyName;

    private Integer discountType;

    private String discountTypeText;

    private Integer waiveMonths;

    private BigDecimal discountRate;

    private BigDecimal deductAmount;

    private Integer scopeType;

    private LocalDate startTime;

    private LocalDate endTime;

    private Integer status;

    private String statusText;

    private String remark;
}
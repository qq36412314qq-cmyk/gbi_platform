package com.gbi.platform.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 应收应付计划视图
 *
 * @author gbi
 */
@Data
public class RecvPayPlanVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long companyId;

    private String planNo;

    private Integer direction;

    private String directionText;

    private String bizType;

    private String bizTypeText;

    private String sourceType;

    private String sourceId;

    private Long marketId;

    private Long stallId;

    private String stallNumber;

    private Long tenantId;

    private String tenantName;

    private Long merchantId;

    private String periodNo;

    private Integer periodType;

    private LocalDate dueDate;

    private BigDecimal originalAmount;

    private BigDecimal discountAmount;

    private BigDecimal adjustAmount;

    private BigDecimal planAmount;

    private BigDecimal paidAmount;

    private BigDecimal unpaidAmount;

    private Integer planStatus;

    private String planStatusText;

    private Integer redFlag;

    private String redFlagText;

    private Long origPlanId;

    private Integer overdueDays;

    private Long flowInstanceId;

    private Long discountApplyId;

    private String remark;

    private LocalDateTime createTime;
}
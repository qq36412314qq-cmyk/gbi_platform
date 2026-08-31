package com.gbi.platform.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠申请视图
 *
 * @author gbi
 */
@Data
public class DiscountApplyVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long companyId;

    private String applyNo;

    private Long policyId;

    private String policyName;

    private String contractNo;

    private Long stallId;

    private Long tenantId;

    private Integer waiveMonths;

    private BigDecimal discountRate;

    private BigDecimal deductAmount;

    private BigDecimal discountAmount;

    private Integer needAudit;

    private String needAuditText;

    private Long flowInstanceId;

    private Integer applyStatus;

    private String applyStatusText;

    private String applyUserName;

    private String remark;

    private LocalDateTime auditTime;

    private LocalDateTime createTime;
}
package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠申请单表实体：finance_discount_apply
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_discount_apply")
public class BizDiscountApply extends BaseEntity {

    private Long companyId;
    private String applyNo;
    private String bizType;
    private Long policyId;
    private String policySnapshot;
    private String sourceType;
    private String sourceId;
    private String sourceNo;
    private String contractNo;
    private Long stallId;
    private Long tenantId;
    private String startMonth;
    private String endMonth;
    private Integer totalMonths;
    private Integer waiveMonths;
    private BigDecimal discountRate;
    private BigDecimal deductAmount;
    private BigDecimal fixedAmount;
    private String tierConfig;
    private Integer discountType;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal realAmount;
    private Integer needAudit;
    private Long flowInstanceId;
    private Integer applyStatus;
    private Long applyUserId;
    private LocalDateTime auditTime;
    private String auditRemark;
    private String remark;
}
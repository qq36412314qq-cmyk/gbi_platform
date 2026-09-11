package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠申请视图（扩展）
 *
 * @author gbi
 */
@Data
@Schema(description = "优惠申请视图")
public class DiscountApplyVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "申请ID")
    private Long id;

    @Schema(description = "所属子公司ID")
    private Long companyId;

    @Schema(description = "申请编号")
    private String applyNo;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务类型文本")
    private String bizTypeText;

    @Schema(description = "优惠策略ID")
    private Long policyId;

    @Schema(description = "策略名称")
    private String policyName;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "来源单据ID")
    private String sourceId;

    @Schema(description = "来源单据编号")
    private String sourceNo;

    @Schema(description = "合同编号")
    private String contractNo;

    @Schema(description = "铺位ID")
    private Long stallId;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "优惠起始月份")
    private String startMonth;

    @Schema(description = "优惠结束月份")
    private String endMonth;

    @Schema(description = "优惠总月数")
    private Integer totalMonths;

    @Schema(description = "免租期月数")
    private Integer waiveMonths;

    @Schema(description = "折扣率%")
    private BigDecimal discountRate;

    @Schema(description = "减免金额")
    private BigDecimal deductAmount;

    @Schema(description = "定额优惠金额")
    private BigDecimal fixedAmount;

    @Schema(description = "阶梯配置JSON")
    private String tierConfig;

    @Schema(description = "优惠基数金额")
    private BigDecimal originalAmount;

    @Schema(description = "优惠金额")
    private BigDecimal discountAmount;

    @Schema(description = "实际应收金额")
    private BigDecimal realAmount;

    @Schema(description = "是否需审批")
    private Integer needAudit;

    @Schema(description = "是否需审批文本")
    private String needAuditText;

    @Schema(description = "关联审批实例ID")
    private Long flowInstanceId;

    @Schema(description = "申请状态 0草稿 1审批中 2通过 3驳回 4作废")
    private Integer applyStatus;

    @Schema(description = "申请状态文本")
    private String applyStatusText;

    @Schema(description = "申请人用户ID")
    private Long applyUserId;

    @Schema(description = "审批意见")
    private String auditRemark;

    @Schema(description = "审批完成时间")
    private LocalDateTime auditTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人用户ID")
    private Long createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
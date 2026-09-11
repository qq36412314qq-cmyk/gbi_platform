package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 优惠策略视图（扩展）
 *
 * @author gbi
 */
@Data
@Schema(description = "优惠策略视图")
public class DiscountPolicyVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "策略ID")
    private Long id;

    @Schema(description = "所属子公司ID")
    private Long companyId;

    @Schema(description = "策略名称")
    private String policyName;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务类型文本")
    private String bizTypeText;

    @Schema(description = "优惠类型")
    private Integer discountType;

    @Schema(description = "优惠类型文本")
    private String discountTypeText;

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

    @Schema(description = "适用范围")
    private Integer scopeType;

    @Schema(description = "适用范围文本")
    private String scopeTypeText;

    @Schema(description = "适用范围ID列表JSON")
    private String scopeIds;

    @Schema(description = "策略生效时间")
    private LocalDate startTime;

    @Schema(description = "策略失效时间，NULL永久")
    private LocalDate endTime;

    @Schema(description = "最多申请月数限制")
    private Integer maxApplyMonths;

    @Schema(description = "自动审批")
    private Integer autoApprove;

    @Schema(description = "自动审批文本")
    private String autoApproveText;

    @Schema(description = "状态 0停用 1启用")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "排序权重")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人用户ID")
    private Long createBy;

    @Schema(description = "创建时间")
    private java.time.LocalDateTime createTime;

    @Schema(description = "更新人用户ID")
    private Long updateBy;

    @Schema(description = "更新时间")
    private java.time.LocalDateTime updateTime;
}
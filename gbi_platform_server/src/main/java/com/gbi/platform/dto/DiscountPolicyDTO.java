package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 优惠策略新增/编辑 DTO
 *
 * @author gbi
 */
@Data
@Schema(description = "优惠策略新增/编辑入参")
public class DiscountPolicyDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "策略ID（编辑时必填）")
    private Long id;

    @NotBlank(message = "策略名称不能为空")
    @Schema(description = "策略名称")
    private String policyName;

    @NotNull(message = "业务类型不能为空")
    @Schema(description = "业务类型 rent/property_fee/water_elec/kindergarten")
    private String bizType;

    @NotNull(message = "优惠类型不能为空")
    @Schema(description = "优惠类型 1免租期 2折扣率 3减免金额 4组合 5定额 6阶梯")
    private Integer discountType;

    @Schema(description = "免租期月数（type=1/4）")
    private Integer waiveMonths;

    @Schema(description = "折扣率%（100=无折扣，type=2/4）")
    private BigDecimal discountRate;

    @Schema(description = "减免金额（type=3/4）")
    private BigDecimal deductAmount;

    @Schema(description = "定额优惠金额（type=5）")
    private BigDecimal fixedAmount;

    @Schema(description = "阶梯配置JSON（type=6）")
    private String tierConfig;

    @NotNull(message = "适用范围不能为空")
    @Schema(description = "适用范围 1按合同 2按铺位 3按租户 4按市场 5按分类")
    private Integer scopeType;

    @Schema(description = "适用范围ID列表JSON")
    private String scopeIds;

    @Schema(description = "策略生效时间")
    private LocalDate startTime;

    @Schema(description = "策略失效时间，NULL永久")
    private LocalDate endTime;

    @Schema(description = "最多申请月数限制，NULL不限")
    private Integer maxApplyMonths;

    @Schema(description = "自动审批 0需审批 1自动生效")
    private Integer autoApprove;

    @Schema(description = "排序权重")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;
}
package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 优惠申请新增 DTO
 *
 * @author gbi
 */
@Data
@Schema(description = "优惠申请新增入参")
public class DiscountApplyDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "业务类型不能为空")
    @Schema(description = "业务类型 rent/property_fee/water_elec/kindergarten")
    private String bizType;

    @NotNull(message = "来源类型不能为空")
    @Schema(description = "来源类型 contract/property_bill/water_elec_bill/other")
    private String sourceType;

    @NotNull(message = "来源单据ID不能为空")
    @Schema(description = "来源单据ID（合同ID/账单ID）")
    private String sourceId;

    @Schema(description = "来源单据编号")
    private String sourceNo;

    @Schema(description = "优惠策略ID，为空则手动填写参数")
    private Long policyId;

    @NotNull(message = "铺位ID不能为空")
    @Schema(description = "铺位ID")
    private Long stallId;

    @Schema(description = "租户ID")
    private Long tenantId;

    @NotBlank(message = "优惠起始月份不能为空")
    @Schema(description = "优惠起始月份 yyyy-MM")
    private String startMonth;

    @NotBlank(message = "优惠结束月份不能为空")
    @Schema(description = "优惠结束月份 yyyy-MM")
    private String endMonth;

    @Schema(description = "免租期月数（type=1/4）")
    private Integer waiveMonths;

    @Schema(description = "折扣率%（type=2/4）")
    private BigDecimal discountRate;

    @Schema(description = "减免金额（type=3/4）")
    private BigDecimal deductAmount;

    @Schema(description = "定额优惠金额（type=5）")
    private BigDecimal fixedAmount;

    @Schema(description = "阶梯配置JSON（type=6）")
    private String tierConfig;

    @Schema(description = "优惠类型（type=1-6）")
    private Integer discountType;

    @Schema(description = "优惠基数金额")
    private BigDecimal originalAmount;

    @Schema(description = "备注")
    private String remark;
}
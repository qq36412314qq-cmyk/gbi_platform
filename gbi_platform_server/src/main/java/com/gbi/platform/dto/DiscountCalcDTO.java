package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 优惠计算预览 DTO
 *
 * @author gbi
 */
@Data
@Schema(description = "优惠计算预览入参")
public class DiscountCalcDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "业务类型不能为空")
    @Schema(description = "业务类型")
    private String bizType;

    @NotNull(message = "原价金额不能为空")
    @Schema(description = "优惠基数金额")
    private BigDecimal originalAmount;

    @NotNull(message = "优惠类型不能为空")
    @Schema(description = "优惠类型 1免租期 2折扣率 3减免金额 4组合 5定额 6阶梯")
    private Integer discountType;

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

    @Schema(description = "总月数（免租期折算用）")
    private Integer totalMonths;
}
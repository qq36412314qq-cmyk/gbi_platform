package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 优惠计算预览视图
 *
 * @author gbi
 */
@Data
@Schema(description = "优惠计算预览视图")
public class DiscountCalcVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "原价金额")
    private BigDecimal originalAmount;

    @Schema(description = "优惠金额")
    private BigDecimal discountAmount;

    @Schema(description = "实收金额")
    private BigDecimal realAmount;

    @Schema(description = "计算规则说明JSON")
    private String calcRule;
}
package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 收费规则新增/编辑入参
 * 规则调用收费类型（feeItemId）；收费方式 1定额 2按面积；
 * 收费周期 1按年 2按月 3按日；滞纳金百分比
 *
 * @author gbi
 */
@Data
@Schema(description = "收费规则新增/编辑入参")
public class FeeRuleDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "规则ID（编辑时必填）")
    private Long id;

    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "规则名称不能为空")
    @Size(max = 128, message = "规则名称不能超过128字符")
    private String ruleName;

    @Schema(description = "关联收费类型ID（biz_fee_item）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择收费类型")
    private Long feeItemId;

    @Schema(description = "收费方式 1定额 2按面积", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择收费方式")
    @Min(value = 1, message = "收费方式参数非法")
    @Max(value = 2, message = "收费方式参数非法")
    private Integer calcMode;

    @Schema(description = "单价（定额=固定金额/周期；按面积=每平米单价）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.00", message = "单价不能为负数")
    @Digits(integer = 10, fraction = 2, message = "单价最多两位小数")
    private BigDecimal price;

    @Schema(description = "收费周期 0不使用 1按年 2按月 3按日（水费/电费/押金等类型可配置0不使用周期）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择收费周期")
    @Min(value = 0, message = "收费周期参数非法")
    @Max(value = 3, message = "收费周期参数非法")
    private Integer periodType;

    @Schema(description = "滞纳金百分比（逾期加收比例，0=不收）")
    @DecimalMin(value = "0.00", message = "滞纳金百分比不能为负数")
    @DecimalMax(value = "100.00", message = "滞纳金百分比不能超过100")
    @Digits(integer = 3, fraction = 2, message = "滞纳金百分比最多两位小数")
    private BigDecimal overdueRate;

    @Schema(description = "规则状态 0停用 1启用")
    private Integer status;

    @Schema(description = "规则备注")
    @Size(max = 500, message = "备注不能超过500字符")
    private String remark;
}
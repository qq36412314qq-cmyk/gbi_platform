package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 计划人工调账入参（敏感操作：二次确认 + 强制审计；超阈值走 plan_adjust 审批）
 *
 * @author gbi
 */
@Data
@Schema(description = "计划人工调账入参")
public class PlanAdjustDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "计划ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划不能为空")
    private Long planId;

    @Schema(description = "调账金额（正负均可）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "调账金额不能为空")
    @Digits(integer = 10, fraction = 2, message = "调账金额最多两位小数")
    private BigDecimal adjustAmount;

    @Schema(description = "调账原因")
    @Size(max = 500, message = "调账原因不能超过500字符")
    private String remark;
}
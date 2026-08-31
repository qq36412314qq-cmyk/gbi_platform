package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 计划作废/终止入参（高危：强制审批 + 红冲链）
 *
 * @author gbi
 */
@Data
@Schema(description = "计划作废/终止入参")
public class PlanTerminateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "计划ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划不能为空")
    private Long planId;

    @Schema(description = "作废/终止原因")
    @Size(max = 500, message = "原因不能超过500字符")
    private String remark;
}
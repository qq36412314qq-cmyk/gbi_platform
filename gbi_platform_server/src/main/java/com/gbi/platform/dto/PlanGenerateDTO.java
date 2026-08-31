package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 按合同手动补生成计划入参（幂等：同来源同周期已存在则跳过）
 *
 * @author gbi
 */
@Data
@Schema(description = "按合同补生成计划入参")
public class PlanGenerateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "合同ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "合同不能为空")
    private Long contractId;
}
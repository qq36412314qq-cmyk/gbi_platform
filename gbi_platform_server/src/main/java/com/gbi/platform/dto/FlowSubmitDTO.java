package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一审批流程提交入参（对齐规范 6.2 /flow 接口）
 *
 * @author gbi
 */
@Data
@Schema(description = "流程提交入参")
public class FlowSubmitDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "流程定义编码 contract/contract_discount/contract_terminate/plan_adjust等", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "流程定义编码不能为空")
    private String defCode;

    @Schema(description = "来源单据类型 contract/reimburse/purchase/plan")
    private String sourceType;

    @Schema(description = "来源单据ID")
    @NotBlank(message = "来源单据ID不能为空")
    private String sourceId;

    @Schema(description = "审批标题（单据摘要）")
    private String title;
}
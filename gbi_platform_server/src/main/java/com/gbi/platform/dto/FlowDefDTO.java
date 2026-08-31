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
 * 流程定义新增/编辑入参（集团专属配置）
 *
 * @author gbi
 */
@Data
@Schema(description = "流程定义新增/编辑入参")
public class FlowDefDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "流程定义ID（编辑必填）")
    private Long id;

    @Schema(description = "流程名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "流程名称不能为空")
    private String defName;

    @Schema(description = "流程编码（唯一）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "流程编码不能为空")
    private String defCode;

    @Schema(description = "适用业务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    @Schema(description = "节点配置JSON（前端表单生成）")
    private String nodeConfigJson;

    @Schema(description = "状态 0停用 1启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
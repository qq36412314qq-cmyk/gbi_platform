package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 审批处理入参（对齐规范 6.2 flow/task/handle：pass/reject/transfer）
 *
 * @author gbi
 */
@Data
@Schema(description = "审批处理入参")
public class FlowHandleDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "审批动作 pass通过 reject驳回 transfer转交", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "审批动作不能为空")
    private String action;

    @Schema(description = "审批任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "审批任务ID不能为空")
    private Long taskId;

    @Schema(description = "审批结果 1通过 0驳回（action=pass/reject 时必填）")
    private Integer approveResult;

    @Schema(description = "审批意见")
    private String opinion;

    @Schema(description = "转交目标用户ID（action=transfer 时必填）")
    private Long transferHandlerId;
}
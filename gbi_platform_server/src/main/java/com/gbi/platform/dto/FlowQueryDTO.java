package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 审批中心通用分页查询入参（待办/申请/实例/定义共用）
 *
 * @author gbi
 */
@Data
@Schema(description = "审批中心分页查询入参")
public class FlowQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum;

    @Schema(description = "每页条数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private Integer pageSize;

    @Schema(description = "业务类型 contract/contract_discount等")
    private String bizType;

    @Schema(description = "实例状态 0审批中 1通过 2驳回 3撤回 4终止")
    private Integer instanceStatus;

    @Schema(description = "来源单据ID")
    private String sourceId;
}
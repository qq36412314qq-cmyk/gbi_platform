package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 租赁合同分页查询入参
 *
 * @author gbi
 */
@Data
@Schema(description = "租赁合同分页查询入参")
public class ContractQueryDTO implements Serializable {

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

    @Schema(description = "合同编号（模糊）")
    private String contractNo;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "铺位ID")
    private Long stallId;

    @Schema(description = "合同状态 1生效中 2已退租 3已到期")
    private Integer contractStatus;
}
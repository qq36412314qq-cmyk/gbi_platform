package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 权限二级复核分页查询入参（对齐前端 getPermissionAuditPageApi）
 *
 * @author gbi
 */
@Data
@Schema(description = "权限复核查询入参")
public class PermissionAuditQueryDTO implements Serializable {

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

    @Schema(description = "审核状态 0待审核 1通过 2驳回")
    private Integer auditStatus;

    @Schema(description = "申请人姓名（模糊）")
    private String applyUserName;

    @Schema(description = "权限类型 ROLE/USER/INHERIT（一期固定 ROLE，字段保留）")
    private String permissionType;
}

package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 权限复核处理入参（对齐前端 auditPermissionApi）
 *
 * @author gbi
 */
@Data
@Schema(description = "权限复核处理入参")
public class PermissionAuditDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "复核记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "复核记录ID不能为空")
    private Long id;

    @Schema(description = "审核结果 1通过 2驳回", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "审核结果不能为空")
    @Min(value = 1, message = "审核结果非法")
    @Max(value = 2, message = "审核结果非法")
    private Integer auditStatus;

    @Schema(description = "审批意见")
    @Size(max = 500, message = "审批意见不能超过500字符")
    private String auditComment;

    @Schema(description = "变更说明（保存申请时使用）")
    private String changeDesc;
}

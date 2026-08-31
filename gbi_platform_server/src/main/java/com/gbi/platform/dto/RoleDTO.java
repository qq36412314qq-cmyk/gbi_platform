package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色新增/编辑入参（对齐前端 RoleDTO，id 为空表示新增）
 *
 * @author gbi
 */
@Data
@Schema(description = "角色入参")
public class RoleDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "角色ID（编辑时必填）")
    private Long id;

    @Schema(description = "所属子公司ID，0集团全局角色模板")
    @NotNull(message = "所属公司不能为空")
    private Long companyId;

    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 64, message = "角色名称不能超过64字符")
    private String roleName;

    @Schema(description = "角色编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "角色编码须为小写字母/数字/下划线")
    @Size(max = 64, message = "角色编码不能超过64字符")
    private String roleCode;

    @Schema(description = "角色备注")
    @Size(max = 500, message = "备注不能超过500字符")
    private String remark;
}

package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 保存角色菜单授权入参（对齐前端 saveRoleMenusApi）
 *
 * @author gbi
 */
@Data
@Schema(description = "角色菜单授权入参")
public class RoleMenuSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "角色ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    @Schema(description = "菜单ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "菜单ID集合不能为空")
    private List<Long> menuIds;
}

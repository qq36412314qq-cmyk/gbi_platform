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
 * 菜单新增/编辑入参（对齐前端 MenuDTO，id 为空表示新增）
 *
 * @author gbi
 */
@Data
@Schema(description = "菜单入参")
public class MenuDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID（编辑时必填）")
    private Long id;

    @Schema(description = "父菜单ID，0顶级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "父菜单不能为空")
    @Min(value = 0, message = "父菜单ID非法")
    private Long parentId;

    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 64, message = "菜单名称不能超过64字符")
    private String menuName;

    @Schema(description = "权限标识 模块:操作")
    @Size(max = 128, message = "权限标识不能超过128字符")
    private String permission;

    @Schema(description = "前端路由地址")
    @Size(max = 256, message = "路由地址不能超过256字符")
    private String path;

    @Schema(description = "菜单图标")
    @Size(max = 128, message = "图标名称不能超过128字符")
    private String icon;

    @Schema(description = "排序号")
    @Min(value = 0, message = "排序号最小为0")
    @Max(value = 9999, message = "排序号最大为9999")
    private Integer sortOrder;

    @Schema(description = "菜单类型 1目录 2菜单页面 3按钮", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "菜单类型不能为空")
    @Min(value = 1, message = "菜单类型非法")
    @Max(value = 3, message = "菜单类型非法")
    private Integer menuType;

    @Schema(description = "是否显示 0隐藏 1显示", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否显示不能为空")
    private Integer visible;
}

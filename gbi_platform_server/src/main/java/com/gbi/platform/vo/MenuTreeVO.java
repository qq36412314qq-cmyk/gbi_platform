package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树返回（对齐前端 MenuVO）
 *
 * @author gbi
 */
@Data
@Schema(description = "菜单树节点")
public class MenuTreeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID")
    private Long id;

    @Schema(description = "父菜单ID")
    private Long parentId;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "权限标识 模块:操作")
    private String permission;

    @Schema(description = "前端路由地址")
    private String path;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "菜单类型 1目录 2菜单页面 3按钮")
    private Integer menuType;

    @Schema(description = "是否显示 0隐藏 1显示")
    private Integer visible;

    @Schema(description = "子节点")
    private List<MenuTreeVO> children = new ArrayList<>();
}

package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限菜单实体：sys_menu（集团全局）
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    /** 父菜单ID，0顶级 */
    private Long parentId;

    /** 菜单名称 */
    private String menuName;

    /** 权限标识 模块:操作 */
    private String permission;

    /** 前端路由地址 */
    private String path;

    /** 菜单图标 */
    private String icon;

    /** 排序号 */
    private Integer sortOrder;

    /** 菜单类型 1目录 2菜单页面 3按钮 */
    private Integer menuType;

    /** 是否显示 0隐藏 1显示 */
    private Integer visible;
}

package com.gbi.platform.service;

import com.gbi.platform.dto.MenuDTO;
import com.gbi.platform.vo.MenuTreeVO;

import java.util.List;

/**
 * 菜单服务：sys_menu 树形维护（集团全局）
 *
 * @author gbi
 */
public interface MenuService {

    /**
     * 菜单树
     */
    List<MenuTreeVO> tree();

    /**
     * 获取当前登录用户授权的菜单树（用于动态路由生成）
     * 超级管理员返回全量树，普通用户只返回其角色有权访问的页面级菜单
     */
    List<MenuTreeVO> getAuthorizedTree();

    /**
     * 新增菜单
     */
    void add(MenuDTO dto);

    /**
     * 编辑菜单
     */
    void update(MenuDTO dto);

    /**
     * 删除菜单（有子菜单或已授权角色不允许删除）
     */
    void delete(Long id);
}

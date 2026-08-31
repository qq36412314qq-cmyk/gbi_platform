package com.gbi.platform.service;

import com.gbi.platform.dto.RoleDTO;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.RoleVO;

import java.util.List;

/**
 * 角色服务：sys_role 维护 + 菜单授权
 *
 * @author gbi
 */
public interface RoleService {

    /**
     * 角色列表（不分页，供下拉/分配）
     */
    List<RoleVO> list(Long companyId);

    /**
     * 角色分页
     */
    PageVO<RoleVO> page(Integer pageNum, Integer pageSize, String roleName);

    /**
     * 新增角色
     */
    void add(RoleDTO dto);

    /**
     * 编辑角色
     */
    void update(RoleDTO dto);

    /**
     * 删除角色（已分配给用户不允许删除）
     */
    void delete(Long id);

    /**
     * 查询角色已分配菜单ID
     */
    List<Long> getRoleMenus(Long roleId);

    /**
     * 保存角色菜单授权（先删后插）
     */
    void saveRoleMenus(Long roleId, List<Long> menuIds);
}

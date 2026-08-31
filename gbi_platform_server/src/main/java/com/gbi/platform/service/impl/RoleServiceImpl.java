package com.gbi.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.RoleDTO;
import com.gbi.platform.entity.SysRole;
import com.gbi.platform.entity.SysRoleMenuRel;
import com.gbi.platform.entity.SysUserRoleRel;
import com.gbi.platform.mapper.SysRoleMapper;
import com.gbi.platform.mapper.SysRoleMenuRelMapper;
import com.gbi.platform.mapper.SysUserRoleRelMapper;
import com.gbi.platform.service.RoleService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * 角色服务实现：sys_role 维护 + 菜单授权
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper roleMapper;

    private final SysUserRoleRelMapper userRoleRelMapper;

    private final SysRoleMenuRelMapper roleMenuRelMapper;

    private final AuditLogUtil auditLogUtil;

    @Override
    public List<RoleVO> list(Long companyId) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .eq(companyId != null, SysRole::getCompanyId, companyId)
                .orderByAsc(SysRole::getId);
        return roleMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public PageVO<RoleVO> page(Integer pageNum, Integer pageSize, String roleName) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .like(StringUtils.hasText(roleName), SysRole::getRoleName, roleName)
                .orderByDesc(SysRole::getId);
        Page<SysRole> result = roleMapper.selectPage(page, wrapper);
        List<RoleVO> voList = result.getRecords().stream().map(this::toVO).toList();
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(RoleDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        Long companyId = loginUser.isSuperAdmin() ? dto.getCompanyId() : loginUser.getCompanyId();
        Long exist = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getCompanyId, companyId)
                .eq(SysRole::getRoleCode, dto.getRoleCode()));
        if (exist != null && exist > 0) {
            throw new BizException("角色编码已存在：" + dto.getRoleCode());
        }
        SysRole role = new SysRole();
        role.setCompanyId(companyId);
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setRemark(dto.getRemark());
        roleMapper.insert(role);
        auditLogUtil.record(CommonConst.MODULE_ORG, CommonConst.OPER_TYPE_ADD,
                String.valueOf(role.getId()), null, role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RoleDTO dto) {
        SysRole role = roleMapper.selectById(dto.getId());
        if (role == null) {
            throw new BizException("角色不存在");
        }
        Long exist = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getCompanyId, role.getCompanyId())
                .eq(SysRole::getRoleCode, dto.getRoleCode())
                .ne(SysRole::getId, dto.getId()));
        if (exist != null && exist > 0) {
            throw new BizException("角色编码已存在：" + dto.getRoleCode());
        }
        SysRole before = new SysRole();
        BeanUtil.copyProperties(role, before);
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setRemark(dto.getRemark());
        roleMapper.updateById(role);
        auditLogUtil.record(CommonConst.MODULE_ORG, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(role.getId()), before, role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        Long userCount = userRoleRelMapper.selectCount(new LambdaQueryWrapper<SysUserRoleRel>()
                .eq(SysUserRoleRel::getRoleId, id));
        if (userCount != null && userCount > 0) {
            throw new BizException("角色已分配给用户，不允许删除");
        }
        roleMapper.deleteById(id);
        roleMenuRelMapper.delete(new LambdaQueryWrapper<SysRoleMenuRel>().eq(SysRoleMenuRel::getRoleId, id));
        auditLogUtil.record(CommonConst.MODULE_ORG, CommonConst.OPER_TYPE_DELETE, String.valueOf(id), role, null);
    }

    @Override
    public List<Long> getRoleMenus(Long roleId) {
        return roleMenuRelMapper.selectList(new LambdaQueryWrapper<SysRoleMenuRel>()
                        .eq(SysRoleMenuRel::getRoleId, roleId))
                .stream().map(SysRoleMenuRel::getMenuId).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRoleMenus(Long roleId, List<Long> menuIds) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        List<Long> before = getRoleMenus(roleId);
        roleMenuRelMapper.delete(new LambdaQueryWrapper<SysRoleMenuRel>().eq(SysRoleMenuRel::getRoleId, roleId));
        if (menuIds != null) {
            for (Long menuId : menuIds) {
                SysRoleMenuRel rel = new SysRoleMenuRel();
                rel.setRoleId(roleId);
                rel.setMenuId(menuId);
                roleMenuRelMapper.insert(rel);
            }
        }
        auditLogUtil.record(CommonConst.MODULE_ORG, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(roleId), before, menuIds);
    }

    /**
     * 实体转 VO
     */
    private RoleVO toVO(SysRole role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setCompanyId(role.getCompanyId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleCode(role.getRoleCode());
        vo.setRemark(role.getRemark());
        vo.setCreateTime(role.getCreateTime());
        return vo;
    }
}
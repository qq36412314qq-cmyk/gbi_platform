package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.dto.MenuDTO;
import com.gbi.platform.entity.SysMenu;
import cn.hutool.core.bean.BeanUtil;
import com.gbi.platform.entity.SysRoleMenuRel;
import cn.hutool.core.bean.BeanUtil;
import com.gbi.platform.mapper.SysMenuMapper;
import com.gbi.platform.mapper.SysRoleMenuRelMapper;
import com.gbi.platform.service.MenuService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.MenuTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务实现：sys_menu 集团全局树形维护
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final SysMenuMapper menuMapper;

    private final SysRoleMenuRelMapper roleMenuRelMapper;

    private final AuditLogUtil auditLogUtil;

    @Override
    public List<MenuTreeVO> tree() {
        List<SysMenu> all = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSortOrder)
                .orderByAsc(SysMenu::getId));
        Map<Long, MenuTreeVO> map = all.stream().collect(Collectors.toMap(SysMenu::getId, this::toVO));
        List<MenuTreeVO> roots = new ArrayList<>();
        for (SysMenu menu : all) {
            MenuTreeVO vo = map.get(menu.getId());
            if (menu.getParentId() == null || menu.getParentId() == 0L) {
                roots.add(vo);
            } else {
                MenuTreeVO parent = map.get(menu.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    roots.add(vo);
                }
            }
        }
        return roots;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(MenuDTO dto) {
        checkPermissionUnique(dto.getPermission(), null);
        SysMenu menu = new SysMenu();
        menu.setParentId(dto.getParentId());
        menu.setMenuName(dto.getMenuName());
        menu.setPermission(dto.getPermission());
        menu.setPath(dto.getPath());
        menu.setIcon(dto.getIcon());
        menu.setSortOrder(dto.getSortOrder());
        menu.setMenuType(dto.getMenuType());
        menu.setVisible(dto.getVisible());
        menuMapper.insert(menu);
        auditLogUtil.record(CommonConst.MODULE_ORG, CommonConst.OPER_TYPE_ADD,
                String.valueOf(menu.getId()), null, menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MenuDTO dto) {
        SysMenu menu = menuMapper.selectById(dto.getId());
        if (menu == null) {
            throw new BizException("菜单不存在");
        }
        // 不能把父级设为自己或子孙
        if (dto.getParentId() != null && !dto.getParentId().equals(0L) && !dto.getParentId().equals(menu.getParentId())) {
            if (dto.getParentId().equals(menu.getId()) || isDescendant(menu.getId(), dto.getParentId())) {
                throw new BizException("父级菜单不能设置为自身或下级菜单");
            }
        }
        checkPermissionUnique(dto.getPermission(), dto.getId());
        SysMenu before = new SysMenu();
        cn.hutool.core.bean.BeanUtil.copyProperties(menu, before);
        menu.setParentId(dto.getParentId());
        menu.setMenuName(dto.getMenuName());
        menu.setPermission(dto.getPermission());
        menu.setPath(dto.getPath());
        menu.setIcon(dto.getIcon());
        menu.setSortOrder(dto.getSortOrder());
        menu.setMenuType(dto.getMenuType());
        menu.setVisible(dto.getVisible());
        menuMapper.updateById(menu);
        auditLogUtil.record(CommonConst.MODULE_ORG, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(menu.getId()), before, menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BizException("菜单不存在");
        }
        Long children = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id));
        if (children != null && children > 0) {
            throw new BizException("存在下级菜单，不允许删除");
        }
        Long roleCount = roleMenuRelMapper.selectCount(new LambdaQueryWrapper<SysRoleMenuRel>()
                .eq(SysRoleMenuRel::getMenuId, id));
        if (roleCount != null && roleCount > 0) {
            throw new BizException("菜单已授权给角色，不允许删除");
        }
        menuMapper.deleteById(id);
        auditLogUtil.record(CommonConst.MODULE_ORG, CommonConst.OPER_TYPE_DELETE, String.valueOf(id), menu, null);
    }

    /**
     * 权限标识唯一校验（权限标识非空时）
     */
    private void checkPermissionUnique(String permission, Long excludeId) {
        if (!StringUtils.hasText(permission)) {
            return;
        }
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getPermission, permission);
        if (excludeId != null) {
            wrapper.ne(SysMenu::getId, excludeId);
        }
        Long exist = menuMapper.selectCount(wrapper);
        if (exist != null && exist > 0) {
            throw new BizException("权限标识已存在：" + permission);
        }
    }

    /**
     * 判断 candidateId 是否为 menuId 的子孙节点
     */
    private boolean isDescendant(Long menuId, Long candidateId) {
        List<Long> children = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getParentId, menuId))
                .stream().map(SysMenu::getId).toList();
        if (children.isEmpty()) {
            return false;
        }
        if (children.contains(candidateId)) {
            return true;
        }
        for (Long child : children) {
            if (isDescendant(child, candidateId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 实体转树节点
     */
    private MenuTreeVO toVO(SysMenu menu) {
        MenuTreeVO vo = new MenuTreeVO();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setMenuName(menu.getMenuName());
        vo.setPermission(menu.getPermission());
        vo.setPath(menu.getPath());
        vo.setIcon(menu.getIcon());
        vo.setSortOrder(menu.getSortOrder());
        vo.setMenuType(menu.getMenuType());
        vo.setVisible(menu.getVisible());
        vo.setChildren(new ArrayList<>());
        return vo;
    }
}
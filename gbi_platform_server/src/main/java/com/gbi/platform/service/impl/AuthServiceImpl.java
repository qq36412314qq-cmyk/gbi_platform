package com.gbi.platform.service.impl;

import cn.hutool.core.util.DesensitizedUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.JwtUtil;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.LoginDTO;
import com.gbi.platform.entity.SysMenu;
import com.gbi.platform.entity.SysRole;
import com.gbi.platform.entity.SysRoleMenuRel;
import com.gbi.platform.entity.SysUser;
import com.gbi.platform.entity.SysUserRoleRel;
import com.gbi.platform.mapper.SysMenuMapper;
import com.gbi.platform.mapper.SysRoleMapper;
import com.gbi.platform.mapper.SysRoleMenuRelMapper;
import com.gbi.platform.mapper.SysUserMapper;
import com.gbi.platform.mapper.SysUserRoleRelMapper;
import com.gbi.platform.service.AuthService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.LoginVO;
import com.gbi.platform.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证服务实现：登录 / 登出 / 当前用户信息 / 权限组装
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;

    private final SysRoleMapper roleMapper;

    private final SysUserRoleRelMapper userRoleRelMapper;

    private final SysMenuMapper menuMapper;

    private final SysRoleMenuRelMapper roleMenuRelMapper;

    private final JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder;

    private final AuditLogUtil auditLogUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        // 查询用户（无登录上下文，多租户拦截器自动忽略 company_id 过滤）
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername())
                .last("LIMIT 1"));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException("账号或密码错误");
        }
        if (!Objects.equals(user.getStatus(), CommonConst.STATUS_ENABLED)) {
            throw new BizException("账号已禁用，请联系管理员");
        }
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.createToken(user.getId()));
        // 登录审计
        auditLogUtil.record(CommonConst.MODULE_BASE, CommonConst.OPER_TYPE_LOGIN,
                String.valueOf(user.getId()), null, null);
        return vo;
    }

    @Override
    public void logout() {
        LoginUser loginUser = UserContext.getLoginUserOrNull();
        if (loginUser != null) {
            auditLogUtil.record(CommonConst.MODULE_BASE, CommonConst.OPER_TYPE_LOGOUT,
                    String.valueOf(loginUser.getUserId()), null, null);
        }
    }

    @Override
    public UserInfoVO getUserInfo() {
        LoginUser loginUser = UserContext.getLoginUser();
        UserInfoVO vo = new UserInfoVO();
        vo.setId(loginUser.getUserId());
        vo.setCompanyId(loginUser.getCompanyId());
        vo.setUsername(loginUser.getUsername());
        vo.setRealName(loginUser.getRealName());
        vo.setPermissions(loginUser.getPermissions());
        // 用户类型：集团 1 / 子公司 2（一期由 companyId 推导）
        vo.setUserType(loginUser.getCompanyId() == 0L ? 1 : 2);
        // 手机号脱敏（密文存储，脱敏展示）
        SysUser user = userMapper.selectById(loginUser.getUserId());
        if (user != null) {
            if (user.getPhone() != null && !user.getPhone().isBlank()) {
                vo.setPhone(DesensitizedUtil.mobilePhone(user.getPhone()));
            }
            vo.setAvatar(user.getAvatar());
        }
        return vo;
    }

    @Override
    public LoginUser buildLoginUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || !Objects.equals(user.getStatus(), CommonConst.STATUS_ENABLED)) {
            return null;
        }
        // 角色编码
        List<Long> roleIds = userRoleRelMapper.selectList(
                        new LambdaQueryWrapper<SysUserRoleRel>().eq(SysUserRoleRel::getUserId, userId))
                .stream().map(SysUserRoleRel::getRoleId).toList();
        List<String> roleCodes = new ArrayList<>();
        if (!roleIds.isEmpty()) {
            roleCodes = roleMapper.selectBatchIds(roleIds).stream()
                    .map(SysRole::getRoleCode).collect(Collectors.toList());
        }
        // 权限标识
        List<String> permissions = new ArrayList<>();
        boolean superAdmin = roleCodes.contains(CommonConst.ROLE_SUPER_ADMIN);
        if (superAdmin) {
            permissions.add(PermissionConst.ALL_PERMISSION);
        } else if (!roleIds.isEmpty()) {
            List<Long> menuIds = roleMenuRelMapper.selectList(
                            new LambdaQueryWrapper<SysRoleMenuRel>().in(SysRoleMenuRel::getRoleId, roleIds))
                    .stream().map(SysRoleMenuRel::getMenuId).distinct().toList();
            if (!menuIds.isEmpty()) {
                Set<String> perms = menuMapper.selectBatchIds(menuIds).stream()
                        .map(SysMenu::getPermission)
                        .filter(p -> p != null && !p.isBlank())
                        .collect(Collectors.toSet());
                permissions.addAll(perms);
            }
        }
        return new LoginUser(user.getId(), user.getCompanyId(), user.getUsername(),
                user.getRealName(), permissions, roleCodes);
    }
}

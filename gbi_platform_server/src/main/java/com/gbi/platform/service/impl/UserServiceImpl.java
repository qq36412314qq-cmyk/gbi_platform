package com.gbi.platform.service.impl;

import cn.hutool.core.util.DesensitizedUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.UserAddDTO;
import com.gbi.platform.dto.UserQueryDTO;
import com.gbi.platform.dto.UserUpdateDTO;
import com.gbi.platform.entity.SysRole;
import cn.hutool.core.bean.BeanUtil;
import com.gbi.platform.entity.SysUser;
import com.gbi.platform.entity.SysUserRoleRel;
import com.gbi.platform.mapper.SysRoleMapper;
import com.gbi.platform.mapper.SysUserMapper;
import com.gbi.platform.mapper.SysUserRoleRelMapper;
import com.gbi.platform.service.AuthService;
import com.gbi.platform.service.UserService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.UserListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户服务实现：sys_user 维护 + 角色关联
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;

    private final SysUserRoleRelMapper userRoleRelMapper;

    private final SysRoleMapper roleMapper;

    private final PasswordEncoder passwordEncoder;

    private final AuthService authService;

    private final AuditLogUtil auditLogUtil;

    @Override
    public PageVO<UserListVO> page(UserQueryDTO dto) {
        Page<SysUser> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .like(StringUtils.hasText(dto.getUsername()), SysUser::getUsername, dto.getUsername())
                .like(StringUtils.hasText(dto.getRealName()), SysUser::getRealName, dto.getRealName())
                .like(StringUtils.hasText(dto.getPhone()), SysUser::getPhone, dto.getPhone())
                .eq(dto.getStatus() != null, SysUser::getStatus, dto.getStatus())
                .orderByDesc(SysUser::getId);
        Page<SysUser> result = userMapper.selectPage(page, wrapper);

        List<UserListVO> voList = new ArrayList<>();
        if (!result.getRecords().isEmpty()) {
            List<Long> userIds = result.getRecords().stream().map(SysUser::getId).toList();
            Map<Long, List<String>> roleNameMap = buildRoleNameMap(userIds);
            for (SysUser user : result.getRecords()) {
                UserListVO vo = new UserListVO();
                vo.setId(user.getId());
                vo.setCompanyId(user.getCompanyId());
                vo.setUsername(user.getUsername());
                vo.setRealName(user.getRealName());
                vo.setPhone(StringUtils.hasText(user.getPhone()) ? DesensitizedUtil.mobilePhone(user.getPhone()) : null);
                vo.setEmail(user.getEmail());
                vo.setAvatar(user.getAvatar());
                vo.setStatus(user.getStatus());
                vo.setRoleNames(roleNameMap.getOrDefault(user.getId(), Collections.emptyList()));
                vo.setCreateTime(user.getCreateTime());
                voList.add(vo);
            }
        }
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(UserAddDTO dto) {
        // 唯一性校验：登录账号
        Long exist = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (exist != null && exist > 0) {
            throw new BizException("登录账号已存在：" + dto.getUsername());
        }
        // 手机号/邮箱格式校验（可选字段，有值时才校验）
        validatePhoneEmail(dto.getPhone(), dto.getEmail());
        // 数据隔离：子公司账号强制绑定登录人公司，集团管理员可指定
        LoginUser loginUser = UserContext.getLoginUser();
        Long companyId = loginUser.isSuperAdmin() ? dto.getCompanyId() : loginUser.getCompanyId();

        SysUser user = new SysUser();
        user.setCompanyId(companyId);
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setAvatar(dto.getAvatar());
        user.setStatus(dto.getStatus());
        userMapper.insert(user);
        // 角色关联
        saveUserRoles(user.getId(), dto.getRoleIds());
        auditLogUtil.record(CommonConst.MODULE_ORG, CommonConst.OPER_TYPE_ADD,
                String.valueOf(user.getId()), null, user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserUpdateDTO dto) {
        SysUser user = userMapper.selectById(dto.getId());
        if (user == null) {
            throw new BizException("用户不存在");
        }
        // 唯一性校验（排除自身）
        Long exist = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername())
                .ne(SysUser::getId, dto.getId()));
        if (exist != null && exist > 0) {
            throw new BizException("登录账号已存在：" + dto.getUsername());
        }
        // 手机号/邮箱格式校验（可选字段，有值时才校验）
        validatePhoneEmail(dto.getPhone(), dto.getEmail());
        SysUser before = new SysUser();
        BeanUtil.copyProperties(user, before);
        user.setUsername(dto.getUsername());
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setAvatar(dto.getAvatar());
        user.setStatus(dto.getStatus());
        userMapper.updateById(user);
        // 角色关联：先删后插
        userRoleRelMapper.delete(new LambdaQueryWrapper<SysUserRoleRel>().eq(SysUserRoleRel::getUserId, user.getId()));
        saveUserRoles(user.getId(), dto.getRoleIds());
        auditLogUtil.record(CommonConst.MODULE_ORG, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(user.getId()), before, user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        LoginUser loginUser = UserContext.getLoginUser();
        if (Objects.equals(id, loginUser.getUserId())) {
            throw new BizException("不能删除当前登录账号");
        }
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        userMapper.deleteById(id);
        userRoleRelMapper.delete(new LambdaQueryWrapper<SysUserRoleRel>().eq(SysUserRoleRel::getUserId, id));
        auditLogUtil.record(CommonConst.MODULE_ORG, CommonConst.OPER_TYPE_DELETE, String.valueOf(id), user, null);
    }

    @Override
    public void resetPwd(Long id, String password) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        SysUser before = new SysUser();
        BeanUtil.copyProperties(user, before);
        SysUser update = new SysUser();
        update.setId(id);
        update.setPassword(passwordEncoder.encode(password));
        userMapper.updateById(update);
        auditLogUtil.record(CommonConst.MODULE_ORG, "重置密码", String.valueOf(id), before, update);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        LoginUser loginUser = UserContext.getLoginUser();
        if (Objects.equals(id, loginUser.getUserId()) && Objects.equals(status, CommonConst.STATUS_DISABLED)) {
            throw new BizException("不能禁用当前登录账号");
        }
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        SysUser before = new SysUser();
        BeanUtil.copyProperties(user, before);
        SysUser update = new SysUser();
        update.setId(id);
        update.setStatus(status);
        userMapper.updateById(update);
        auditLogUtil.record(CommonConst.MODULE_ORG, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(id), before, update);
    }

    @Override
    public LoginUser buildLoginUser(Long userId) {
        return authService.buildLoginUser(userId);
    }

    @Override
    public Map<Long, String> mapRealNameByIds(java.util.Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> distinctIds = userIds.stream().distinct().toList();
        Map<Long, String> nameMap = userMapper.selectBatchIds(distinctIds).stream()
                .collect(Collectors.toMap(SysUser::getId,
                        u -> u.getRealName() == null || u.getRealName().isBlank() ? u.getUsername() : u.getRealName(),
                        (a, b) -> a));
        return distinctIds.stream()
                .collect(Collectors.toMap(Function.identity(),
                        id -> nameMap.getOrDefault(id, "用户" + id), (a, b) -> a));
    }

    /**
     * 批量查询用户的角色名称集合
     */
    private Map<Long, List<String>> buildRoleNameMap(List<Long> userIds) {
        List<SysUserRoleRel> rels = userRoleRelMapper.selectList(
                new LambdaQueryWrapper<SysUserRoleRel>().in(SysUserRoleRel::getUserId, userIds));
        if (rels.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> roleIds = rels.stream().map(SysUserRoleRel::getRoleId).distinct().toList();
        Map<Long, String> roleNameMap = roleMapper.selectBatchIds(roleIds).stream()
                .collect(Collectors.toMap(SysRole::getId, SysRole::getRoleName, (a, b) -> a));
        return rels.stream()
                .collect(Collectors.groupingBy(SysUserRoleRel::getUserId,
                        Collectors.mapping(rel -> roleNameMap.getOrDefault(rel.getRoleId(), "-"), Collectors.toList())));
    }

    /**
     * 保存用户角色关联
     */
    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            SysUserRoleRel rel = new SysUserRoleRel();
            rel.setUserId(userId);
            rel.setRoleId(roleId);
            userRoleRelMapper.insert(rel);
        }
    }

    /**
     * 校验手机号/邮箱格式（可选字段，有值时才校验）
     */
    private void validatePhoneEmail(String phone, String email) {
        if (StringUtils.hasText(phone)) {
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                throw new BizException("手机号码格式不正确");
            }
        }
        if (StringUtils.hasText(email)) {
            if (!email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
                throw new BizException("邮箱格式不正确");
            }
        }
    }
}

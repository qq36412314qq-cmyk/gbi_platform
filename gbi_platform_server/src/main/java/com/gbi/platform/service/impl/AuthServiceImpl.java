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
import com.gbi.platform.entity.SysOrg;
import com.gbi.platform.entity.SysRole;
import com.gbi.platform.entity.SysRoleMenuRel;
import com.gbi.platform.entity.SysUser;
import com.gbi.platform.entity.SysUserRoleRel;
import com.gbi.platform.entity.hr.HrEmployee;
import com.gbi.platform.mapper.SysMenuMapper;
import com.gbi.platform.mapper.SysRoleMapper;
import com.gbi.platform.mapper.SysRoleMenuRelMapper;
import com.gbi.platform.mapper.SysUserMapper;
import com.gbi.platform.mapper.SysUserRoleRelMapper;
import com.gbi.platform.service.AuthService;
import com.gbi.platform.service.ConfigService;
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

    private final com.gbi.platform.mapper.hr.HrEmployeeMapper employeeMapper;

    /** 无角色用户默认业务操作员权限（不含系统管理、审计、权限复核等敏感模块） */
    private static final List<String> DEFAULT_BUSINESS_PERMISSIONS = List.of(
            // 工作台
            PermissionConst.DASHBOARD_VIEW,
            // 租赁铺位
            PermissionConst.LEASE_STALL_LIST, PermissionConst.LEASE_STALL_ADD, PermissionConst.LEASE_STALL_EDIT,
            PermissionConst.LEASE_STALL_DELETE,
            PermissionConst.LEASE_CATEGORY_LIST, PermissionConst.LEASE_CATEGORY_ADD,
            PermissionConst.LEASE_CONTRACT_LIST, PermissionConst.LEASE_CONTRACT_ADD,
            PermissionConst.LEASE_CONTRACT_TERMINATE,
            // 市场管理
            PermissionConst.MARKET_LIST, PermissionConst.MARKET_ADD, PermissionConst.MARKET_EDIT,
            // 租户管理
            PermissionConst.TENANT_LIST, PermissionConst.TENANT_ADD,
            // 水电设备
            PermissionConst.WATER_ELEC_LIST, PermissionConst.WATER_ELEC_ADD, PermissionConst.WATER_ELEC_EDIT,
            PermissionConst.WATER_ELEC_BILL_LIST,
            PermissionConst.WATER_ELEC_PAY_LIST, PermissionConst.WATER_ELEC_PAY_ADD,
            // 物业费
            PermissionConst.PROPERTY_FEE_BILL_LIST, PermissionConst.PROPERTY_FEE_PAY_ADD,
            PermissionConst.PROPERTY_UNPAID_BILL_LIST,
            // 财务流水
            PermissionConst.FINANCE_FLOW_LIST,
            // 应收应付计划
            PermissionConst.PLAN_RECVPAY_LIST,
            // 收费项/规则
            PermissionConst.FEE_ITEM_LIST, PermissionConst.FEE_ITEM_ADD, PermissionConst.FEE_ITEM_EDIT,
            PermissionConst.FEE_RULE_LIST, PermissionConst.FEE_RULE_ADD, PermissionConst.FEE_RULE_EDIT,
            // 工作流
            PermissionConst.FLOW_TASK_LIST, PermissionConst.FLOW_TASK_HANDLE,
            PermissionConst.FLOW_APPLY_LIST,
            // OA
            PermissionConst.OA_LEAVE_LIST, PermissionConst.OA_LEAVE_ADD,
            PermissionConst.OA_MEETING_ROOM_LIST, PermissionConst.OA_MEETING_BOOKING_LIST,
            PermissionConst.OA_ANNOUNCEMENT_LIST,
            // HR
            PermissionConst.HR_EMPLOYEE_LIST, PermissionConst.HR_EMPLOYEE_ADD, PermissionConst.HR_EMPLOYEE_EDIT,
            PermissionConst.HR_ATTENDANCE_LIST,
            PermissionConst.HR_ENTRY_LIST, PermissionConst.HR_ENTRY_ADD,
            PermissionConst.HR_SALARY_MONTH_LIST,
            PermissionConst.HR_SOCIAL_LIST
    );


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
        } else {
            // 兜底：无角色用户自动赋予业务操作员默认权限（避免完全无权限导致全页面403）
            permissions.addAll(DEFAULT_BUSINESS_PERMISSIONS);
            log.debug("用户[{}]无角色分配，自动赋予业务操作员默认权限", userId);
        }
        // 关联员工档案，注入员工上下文
        LoginUser loginUser = new LoginUser(user.getId(), user.getCompanyId(), user.getUsername(),
                user.getRealName(), permissions, roleCodes, null, null, null);
        if (user.getEmployeeId() != null) {
            HrEmployee employee = employeeMapper.selectById(user.getEmployeeId());
            if (employee != null) {
                loginUser.setEmployeeId(employee.getId());
                loginUser.setOrgId(employee.getOrgId());
                loginUser.setPostId(employee.getPostId());
            }
        }
        return loginUser;
    }
}

package com.gbi.platform.common.security;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 登录用户信息（存入 SecurityContext，随请求传递）
 *
 * @author gbi
 */
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 所属子公司ID，0 集团 */
    private Long companyId;

    /** 登录账号 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 权限标识集合 */
    private List<String> permissions;

    /** 角色编码集合 */
    private List<String> roleCodes;

    /** 关联员工ID */
    private Long employeeId;

    /** 所属组织ID */
    private Long orgId;

    /** 岗位ID */
    private Long postId;

    public LoginUser() {
    }

    public LoginUser(Long userId, Long companyId, String username, String realName,
                     List<String> permissions, List<String> roleCodes,
                     Long employeeId, Long orgId, Long postId) {
        this.userId = userId;
        this.companyId = companyId;
        this.username = username;
        this.realName = realName;
        this.permissions = permissions;
        this.roleCodes = roleCodes;
        this.employeeId = employeeId;
        this.orgId = orgId;
        this.postId = postId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    public List<String> getRoleCodes() { return roleCodes; }
    public void setRoleCodes(List<String> roleCodes) { this.roleCodes = roleCodes; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    /**
     * 是否超级管理员（集团全局）
     */
    public boolean isSuperAdmin() {
        return permissions != null && permissions.contains(com.gbi.platform.common.constant.PermissionConst.ALL_PERMISSION);
    }

    /**
     * 是否拥有指定权限（支持 *:*:* 通配）
     */
    public boolean hasPermission(String permission) {
        if (permission == null || permission.isBlank()) {
            return true;
        }
        if (permissions == null) {
            return false;
        }
        if (permissions.contains(com.gbi.platform.common.constant.PermissionConst.ALL_PERMISSION)) {
            return true;
        }
        return permissions.contains(permission);
    }
}

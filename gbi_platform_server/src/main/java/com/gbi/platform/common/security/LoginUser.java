package com.gbi.platform.common.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 登录用户信息（存入 SecurityContext，随请求传递）
 *
 * @author gbi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    /**
     * 是否超级管理员（集团全局）
     */
    public boolean isSuperAdmin() {
        return permissions != null && permissions.contains(com.gbi.platform.common.constant.PermissionConst.ALL_PERMISSION);
    }

    /**
     * 是否拥有指定权限（支持 *:*:* 通配）
     *
     * @param permission 权限标识，如 org:list
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

package com.gbi.platform.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 权限校验器：配合 @PreAuthorize 使用
 * 兼容两种写法：
 *   1. hasPermission('org:list','')       —— 双参形式，targetDomainObject 即权限标识
 *   2. hasPermission('org:list','','')    —— 三参形式，permission 即权限标识
 * 支持 *:*:* 超级管理员通配（见 {@link LoginUser#hasPermission(String)}）
 *
 * @author gbi
 */
@Component
@RequiredArgsConstructor
public class MyPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // 双参写法 hasPermission('perm','')：第一个参数是权限标识
        return doCheck(authentication, targetDomainObject);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        // 三参写法 hasPermission('perm','','')：第三个参数是权限标识
        return doCheck(authentication, permission);
    }

    /**
     * 统一权限校验：取权限标识字符串并比对
     */
    private boolean doCheck(Authentication authentication, Object permission) {
        if (!(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            return false;
        }
        if (!(permission instanceof String permissionStr)) {
            return false;
        }
        return loginUser.hasPermission(permissionStr);
    }
}
package com.gbi.platform.common.security;

import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.result.ResultCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 登录用户上下文工具：从 SecurityContext 获取当前登录用户
 * 多租户拦截器、审计日志、业务层统一从此读取
 *
 * @author gbi
 */
public final class UserContext {

    private UserContext() {
    }

    /**
     * 获取当前登录用户（未登录抛出 401）
     */
    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        throw new BizException(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMsg());
    }

    /**
     * 获取当前登录用户（未登录返回 null，用于登录等无上下文场景）
     */
    public static LoginUser getLoginUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    /**
     * 获取当前用户ID（未登录返回 0）
     */
    public static Long getUserIdOrZero() {
        LoginUser user = getLoginUserOrNull();
        return user == null ? 0L : user.getUserId();
    }

    /**
     * 获取当前用户所属公司ID（未登录返回 0）
     */
    public static Long getCompanyIdOrZero() {
        LoginUser user = getLoginUserOrNull();
        return user == null ? 0L : user.getCompanyId();
    }
}

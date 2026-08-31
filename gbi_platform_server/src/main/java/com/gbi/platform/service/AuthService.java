package com.gbi.platform.service;

import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.dto.LoginDTO;
import com.gbi.platform.vo.LoginVO;
import com.gbi.platform.vo.UserInfoVO;

/**
 * 认证服务：登录 / 登出 / 当前用户信息 / 登录用户权限组装
 *
 * @author gbi
 */
public interface AuthService {

    /**
     * 登录：校验账号密码，签发 JWT Token
     *
     * @param dto 登录入参
     * @return Token
     */
    LoginVO login(LoginDTO dto);

    /**
     * 退出登录（记录审计）
     */
    void logout();

    /**
     * 获取当前登录用户信息与权限集合
     */
    UserInfoVO getUserInfo();

    /**
     * 组装登录用户（用户 + 角色 + 权限），JWT 过滤器与登录流程共用
     *
     * @param userId 用户ID
     * @return 登录用户，用户不存在或已禁用返回 null
     */
    LoginUser buildLoginUser(Long userId);
}

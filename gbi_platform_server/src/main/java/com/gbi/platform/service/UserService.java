package com.gbi.platform.service;

import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.dto.UserAddDTO;
import com.gbi.platform.dto.UserQueryDTO;
import com.gbi.platform.dto.UserUpdateDTO;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.UserListVO;

/**
 * 用户服务：sys_user 维护 + 角色关联
 *
 * @author gbi
 */
public interface UserService {

    /**
     * 用户分页查询（返回角色名称）
     */
    PageVO<UserListVO> page(UserQueryDTO dto);

    /**
     * 新增用户（密码 BCrypt 加密，保存角色关联）
     */
    void add(UserAddDTO dto);

    /**
     * 编辑用户（更新角色关联）
     */
    void update(UserUpdateDTO dto);

    /**
     * 删除用户（逻辑删除 + 清理角色关联；禁止删除自己）
     */
    void delete(Long id);

    /**
     * 重置密码
     */
    void resetPwd(Long id, String password);

    /**
     * 启用/禁用账号（禁止禁用自己）
     */
    void changeStatus(Long id, Integer status);

    /**
     * 组装登录用户（用户 + 角色 + 权限集合），供 JWT 过滤器使用
     */
    LoginUser buildLoginUser(Long userId);

    /**
     * 批量查询用户真实姓名（跨模块展示操作人姓名使用）
     *
     * @param userIds 用户ID集合
     * @return userId -> realName（查不到的用户返回 "用户"+id）
     */
    java.util.Map<Long, String> mapRealNameByIds(java.util.Collection<Long> userIds);
}

package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户实体：sys_user
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /** 所属子公司ID，0集团 */
    private Long companyId;

    /** 登录账号 */
    private String username;

    /** BCrypt 加密密码 */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号码（密文存储） */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像OSS地址 */
    private String avatar;

    /** 账号状态 0禁用 1正常 */
    private Integer status;
}

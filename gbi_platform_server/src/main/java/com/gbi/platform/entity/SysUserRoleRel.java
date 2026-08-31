package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体：sys_user_role_rel
 *
 * @author gbi
 */
@Data
@TableName("sys_user_role_rel")
public class SysUserRoleRel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 系统用户ID */
    private Long userId;

    /** 角色ID */
    private Long roleId;

    /** 创建人用户ID */
    private Long createBy;

    /** 创建时间 */
    private LocalDateTime createTime;
}

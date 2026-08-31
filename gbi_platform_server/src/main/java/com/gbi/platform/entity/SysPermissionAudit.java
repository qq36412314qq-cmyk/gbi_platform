package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 敏感权限二级复核申请表实体：sys_permission_audit
 * 敏感权限变更不即时生效，复核通过后才生效（对齐《后端编码规范》十三）
 *
 * @author gbi
 */
@Data
@TableName("sys_permission_audit")
public class SysPermissionAudit implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属子公司ID */
    private Long companyId;

    /** 被授权用户ID */
    private Long targetUserId;

    /** 申请人ID（集团运维） */
    private Long applyUserId;

    /** 复核审批人ID（超级管理员） */
    private Long auditUserId;

    /** 待变更权限标识集合JSON */
    private String permissionList;

    /** 申请变更原因 */
    private String applyReason;

    /** 审核状态 0待审核 1通过 2驳回 */
    private Integer auditStatus;

    /** 审批意见 */
    private String auditComment;

    /** 申请时间 */
    private LocalDateTime applyTime;

    /** 审批完成时间 */
    private LocalDateTime auditTime;
}

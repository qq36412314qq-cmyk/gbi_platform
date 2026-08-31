package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 业务审计日志实体：sys_audit_log
 * 禁止删除、禁止逻辑删除，永久归档（实体不含 is_delete 字段自动豁免逻辑删除）
 *
 * @author gbi
 */
@Data
@TableName("sys_audit_log")
public class SysAuditLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人所属子公司ID */
    private Long companyId;

    /** 操作人用户ID */
    private Long operUserId;

    /** 操作人姓名 */
    private String operUserName;

    /** 操作客户端IP地址 */
    private String operIp;

    /** 操作模块 market_map/finance/org等 */
    private String operModule;

    /** 操作类型：新增/编辑/删除/导出/审核 */
    private String operType;

    /** 关联业务单据ID */
    private String bizId;

    /** 操作前数据快照JSON */
    private String beforeJson;

    /** 操作后数据快照JSON */
    private String afterJson;

    /** 二级复核审核人ID */
    private Long auditOperId;

    /** 操作时间 */
    private LocalDateTime createTime;
}

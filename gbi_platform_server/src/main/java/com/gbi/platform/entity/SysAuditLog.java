package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 业务审计日志实体：sys_audit_log
 * 禁止删除、禁止逻辑删除，永久归档
 */
@TableName("sys_audit_log")
public class SysAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long companyId;
    private Long operUserId;
    private String operUserName;
    private String operIp;
    private String operModule;
    private String operType;
    private String bizId;
    private String beforeJson;
    private String afterJson;
    private Long auditOperId;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public Long getOperUserId() { return operUserId; }
    public void setOperUserId(Long operUserId) { this.operUserId = operUserId; }
    public String getOperUserName() { return operUserName; }
    public void setOperUserName(String operUserName) { this.operUserName = operUserName; }
    public String getOperIp() { return operIp; }
    public void setOperIp(String operIp) { this.operIp = operIp; }
    public String getOperModule() { return operModule; }
    public void setOperModule(String operModule) { this.operModule = operModule; }
    public String getOperType() { return operType; }
    public void setOperType(String operType) { this.operType = operType; }
    public String getBizId() { return bizId; }
    public void setBizId(String bizId) { this.bizId = bizId; }
    public String getBeforeJson() { return beforeJson; }
    public void setBeforeJson(String beforeJson) { this.beforeJson = beforeJson; }
    public String getAfterJson() { return afterJson; }
    public void setAfterJson(String afterJson) { this.afterJson = afterJson; }
    public Long getAuditOperId() { return auditOperId; }
    public void setAuditOperId(Long auditOperId) { this.auditOperId = auditOperId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计日志返回（对齐前端 AuditLogVO）
 */
@Schema(description = "审计日志")
public class AuditLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "操作人所属子公司ID")
    private Long companyId;

    @Schema(description = "操作人用户ID")
    private Long operUserId;

    @Schema(description = "操作人姓名")
    private String operUserName;

    @Schema(description = "操作客户端IP地址")
    private String operIp;

    @Schema(description = "操作模块")
    private String operModule;

    @Schema(description = "操作类型：新增/编辑/删除/导出/审核")
    private String operType;

    @Schema(description = "关联业务单据ID")
    private String bizId;

    @Schema(description = "操作前数据快照JSON")
    private String beforeJson;

    @Schema(description = "操作后数据快照JSON")
    private String afterJson;

    @Schema(description = "二级复核审核人ID")
    private Long auditOperId;

    @Schema(description = "操作时间")
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

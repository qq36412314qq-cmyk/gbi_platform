package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 审计日志查询参数
 */
@Schema(description = "审计日志查询参数")
public class AuditLogQueryDTO {

    @Schema(description = "操作模块")
    private String operModule;

    @Schema(description = "操作类型")
    private String operType;

    @Schema(description = "操作人姓名（模糊）")
    private String operUserName;

    @Schema(description = "开始时间 yyyy-MM-dd HH:mm:ss")
    private String startTime;

    @Schema(description = "结束时间 yyyy-MM-dd HH:mm:ss")
    private String endTime;

    @Schema(description = "当前页码", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize = 10L;

    public String getOperModule() { return operModule; }
    public void setOperModule(String operModule) { this.operModule = operModule; }
    public String getOperType() { return operType; }
    public void setOperType(String operType) { this.operType = operType; }
    public String getOperUserName() { return operUserName; }
    public void setOperUserName(String operUserName) { this.operUserName = operUserName; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Long getPageNum() { return pageNum; }
    public void setPageNum(Long pageNum) { this.pageNum = pageNum; }
    public Long getPageSize() { return pageSize; }
    public void setPageSize(Long pageSize) { this.pageSize = pageSize; }
}

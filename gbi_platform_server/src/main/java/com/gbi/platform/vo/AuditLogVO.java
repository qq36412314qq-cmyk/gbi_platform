package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计日志返回（对齐前端 AuditLogVO）
 *
 * @author gbi
 */
@Data
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
}

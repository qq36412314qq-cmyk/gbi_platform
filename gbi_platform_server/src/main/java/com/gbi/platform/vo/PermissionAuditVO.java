package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限二级复核返回（对齐前端 PermissionAuditVO）
 *
 * @author gbi
 */
@Data
@Schema(description = "权限二级复核记录")
public class PermissionAuditVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "所属子公司ID")
    private Long companyId;

    @Schema(description = "被授权用户ID")
    private Long targetUserId;

    @Schema(description = "申请人ID")
    private Long applyUserId;

    @Schema(description = "申请人姓名")
    private String applyUserName;

    @Schema(description = "权限类型：ROLE/USER/INHERIT（一期默认 ROLE，二期扩展表字段）")
    private String permissionType;

    @Schema(description = "变更对象名称（被授权用户姓名）")
    private String targetName;

    @Schema(description = "变更说明")
    private String changeDesc;

    @Schema(description = "待变更权限标识集合JSON")
    private String permissionList;

    @Schema(description = "申请变更原因")
    private String applyReason;

    @Schema(description = "审核状态 0待审核 1通过 2驳回")
    private Integer auditStatus;

    @Schema(description = "审批意见")
    private String auditComment;

    @Schema(description = "复核人姓名")
    private String auditUserName;

    @Schema(description = "申请时间")
    private LocalDateTime applyTime;

    @Schema(description = "审批完成时间")
    private LocalDateTime auditTime;
}

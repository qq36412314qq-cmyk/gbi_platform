package com.gbi.platform.hr.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial; import java.io.Serializable;
import java.time.LocalDate; import java.time.LocalDateTime;
@Schema(description="调岗申请视图")
@Data public class HrTransferApplyVO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description="主键ID") private Long id;
    @Schema(description="公司ID") private Long companyId;
    @Schema(description="员工ID") private Long employeeId;
    @Schema(description="员工姓名") private String employeeName;
    @Schema(description="原组织ID") private Long oldOrgId;
    @Schema(description="原岗位ID") private Long oldPostId;
    @Schema(description="新组织ID") private Long newOrgId;
    @Schema(description="新岗位ID") private Long newPostId;
    @Schema(description="新岗位职级") private String newPostLevel;
    @Schema(description="调岗生效日期") private LocalDate transferDate;
    @Schema(description="调岗原因") private String reason;
    @Schema(description="流程实例ID") private Long flowInstanceId;
    @Schema(description="状态") private Integer status;
    @Schema(description="状态文本") private String statusText;
}

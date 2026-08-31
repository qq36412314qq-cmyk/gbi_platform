package com.gbi.platform.hr.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serial; import java.io.Serializable;
import java.time.LocalDate;
@Schema(description="调岗申请提交入参")
@Data public class TransferApplyDTO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description="员工ID",requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="员工ID不能为空") private Long employeeId;
    @Schema(description="新组织ID",requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="新组织ID不能为空") private Long newOrgId;
    @Schema(description="新岗位ID",requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="新岗位ID不能为空") private Long newPostId;
    @Schema(description="调岗生效日期",requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="调岗日期不能为空") private LocalDate transferDate;
    @Schema(description="调岗原因") private String reason;
}

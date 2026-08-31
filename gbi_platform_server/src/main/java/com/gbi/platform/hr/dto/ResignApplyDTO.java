package com.gbi.platform.hr.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.io.Serial; import java.io.Serializable;
import java.time.LocalDate;
@Schema(description="离职申请提交入参")
@Data public class ResignApplyDTO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description="员工ID",requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="员工ID不能为空") private Long employeeId;
    @Schema(description="计划离职日期",requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="离职日期不能为空") private LocalDate resignDate;
    @Schema(description="离职类型 1主动辞职 2合同到期 3辞退 4终止合同",requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="离职类型不能为空") private Integer resignType;
    @Schema(description="离职原因") @Size(max=500) private String reason;
    @Schema(description="交接备注") @Size(max=500) private String handoverRemark;
}

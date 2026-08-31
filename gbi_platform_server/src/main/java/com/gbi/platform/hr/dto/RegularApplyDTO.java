package com.gbi.platform.hr.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serial; import java.io.Serializable;
import java.time.LocalDate;
@Schema(description="转正申请提交入参")
@Data public class RegularApplyDTO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description="员工ID",requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="员工ID不能为空") private Long employeeId;
    @Schema(description="计划转正日期",requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="转正日期不能为空") private LocalDate regularDate;
    @Schema(description="备注") private String remark;
}

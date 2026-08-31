package com.gbi.platform.hr.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serial; import java.io.Serializable;
import java.util.List;
@Schema(description="月度薪资核算生成入参")
@Data public class SalaryMonthDTO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description="员工ID列表",requiredMode=Schema.RequiredMode.REQUIRED)
    private List<Long> employeeIds;
    @Schema(description="核算月份 yyyy-MM",requiredMode=Schema.RequiredMode.REQUIRED)
    @NotBlank(message="核算月份不能为空") private String salaryMonth;
}

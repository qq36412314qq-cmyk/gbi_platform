package com.gbi.platform.hr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial; import java.io.Serializable;
import java.math.BigDecimal;

@Schema(description="薪资档案入参")
@Data public class SalaryArchiveDTO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description="薪资档案ID（编辑时必填）") private Long id;
    @Schema(description="员工ID",requiredMode=Schema.RequiredMode.REQUIRED)
    private Long employeeId;
    @Schema(description="基本工资") private BigDecimal basicSalary;
    @Schema(description="绩效工资") private BigDecimal performanceSalary;
    @Schema(description="岗位津贴") private BigDecimal positionAllowance;
    @Schema(description="其他补贴") private BigDecimal otherAllowance;
    @Schema(description="社保个人扣款") private BigDecimal socialSecurityPersonal;
    @Schema(description="公积金个人扣款") private BigDecimal housingFundPersonal;
    @Schema(description="备注") private String remark;
}

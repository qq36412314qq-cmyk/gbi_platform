package com.gbi.platform.hr.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial; import java.io.Serializable;
import java.math.BigDecimal; import java.time.LocalDateTime;
@Schema(description="薪资档案视图")
@Data public class HrSalaryArchiveVO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description="主键ID") private Long id;
    @Schema(description="公司ID") private Long companyId;
    @Schema(description="员工ID") private Long employeeId;
    @Schema(description="员工姓名") private String employeeName;
    @Schema(description="基本工资") private BigDecimal basicSalary;
    @Schema(description="绩效工资") private BigDecimal performanceSalary;
    @Schema(description="岗位津贴") private BigDecimal positionAllowance;
    @Schema(description="其他补贴") private BigDecimal otherAllowance;
    @Schema(description="社保个人扣款") private BigDecimal socialSecurityPersonal;
    @Schema(description="公积金个人扣款") private BigDecimal housingFundPersonal;
    @Schema(description="备注") private String remark;
    @Schema(description="创建时间") private LocalDateTime createTime;
}

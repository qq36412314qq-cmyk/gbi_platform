package com.gbi.platform.dto.hr;

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
    @Schema(description="薪酬级别编码") private String gradeCode;
    @Schema(description="薪酬级别名称快照") private String gradeName;
    @Schema(description="薪资规则ID") private Long ruleId;
    @Schema(description="薪资规则名称快照") private String ruleName;
    @Schema(description="版本号") private Integer versionNo;
    @Schema(description="来源类型 1模板生成 2人工录入 3批量调薪 4晋升调级") private Integer sourceType;
    @Schema(description="来源ID") private Long sourceId;
    @Schema(description="生效日期") private java.time.LocalDate effectiveDate;
    @Schema(description="是否当前版本 1是 0否") private Integer isCurrent;
    @Schema(description="员工姓名") private String employeeName;
}

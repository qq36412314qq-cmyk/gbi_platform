package com.gbi.platform.entity.hr;

import com.gbi.platform.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 员工薪资档案实体（版本化结构）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_salary_archive")
public class HrSalaryArchive extends BaseEntity {
    private Long companyId;
    private Integer versionNo;
    private Integer sourceType;
    private Long sourceId;
    private Long employeeId;
    private String employeeName;
    private String gradeCode;
    private String gradeName;
    private Long ruleId;
    private String ruleName;
    private LocalDate effectiveDate;
    private Integer isCurrent;
    private BigDecimal basicSalary;
    private BigDecimal performanceSalary;
    private BigDecimal positionAllowance;
    private BigDecimal otherAllowance;
    private BigDecimal socialSecurityPersonal;
    private BigDecimal housingFundPersonal;
    private String remark;
}

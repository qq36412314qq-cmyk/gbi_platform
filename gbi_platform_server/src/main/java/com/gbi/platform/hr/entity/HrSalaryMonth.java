package com.gbi.platform.hr.entity;

import com.gbi.platform.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 月度薪资核算单实体
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_salary_month")
public class HrSalaryMonth extends BaseEntity {
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    private String salaryMonth;
    private java.math.BigDecimal basicSalary;
    private java.math.BigDecimal performanceSalary;
    private java.math.BigDecimal allowanceAmount;
    private java.math.BigDecimal socialSecurity;
    private java.math.BigDecimal housingFund;
    private java.math.BigDecimal taxAmount;
    private java.math.BigDecimal deductionAmount;
    private java.math.BigDecimal grossAmount;
    private java.math.BigDecimal netAmount;
    private Integer payStatus;
    private java.time.LocalDateTime payTime;
    private Long flowInstanceId;
    private Long planId;
    private String remark;
}

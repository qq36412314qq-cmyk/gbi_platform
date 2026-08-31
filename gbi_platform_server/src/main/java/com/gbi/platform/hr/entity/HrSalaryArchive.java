package com.gbi.platform.hr.entity;

import com.gbi.platform.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工薪资档案实体
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_salary_archive")
public class HrSalaryArchive extends BaseEntity {
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    private java.math.BigDecimal basicSalary;
    private java.math.BigDecimal performanceSalary;
    private java.math.BigDecimal positionAllowance;
    private java.math.BigDecimal otherAllowance;
    private java.math.BigDecimal socialSecurityPersonal;
    private java.math.BigDecimal housingFundPersonal;
    private String remark;
}

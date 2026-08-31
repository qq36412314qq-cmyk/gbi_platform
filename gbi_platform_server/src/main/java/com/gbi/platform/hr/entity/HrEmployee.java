package com.gbi.platform.hr.entity;

import com.gbi.platform.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工主档案实体
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_employee")
public class HrEmployee extends BaseEntity {
    private Long companyId;
    private Long userId;
    private String employeeNo;
    private String name;
    private String idCardNo;
    private String phone;
    private String email;
    private Integer gender;
    private java.time.LocalDate birthdate;
    private java.time.LocalDate entryDate;
    private java.time.LocalDate regularDate;
    private java.time.LocalDate resignDate;
    private Integer employmentType;
    private Integer employeeStatus;
    private Long orgId;
    private Long postId;
    private String postLevel;
    private String orgName;
    private Long supervisorId;
    private String bankAccount;
    private java.math.BigDecimal socialSecurityBase;
    private java.math.BigDecimal basicSalary;
    private String remark;
}

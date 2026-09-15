package com.gbi.platform.entity.hr;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gbi.platform.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 员工学业经历实体
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_employee_edu_exp")
public class HrEmployeeEduExp extends BaseEntity {
    private Long companyId;
    private Long employeeId;
    private String schoolName;
    private String degree;
    private String major;
    private String educationLevel;
    private LocalDate startDate;
    private LocalDate graduationDate;
    private Integer isGraduated;
    private String certificateNo;
    private String remark;
}

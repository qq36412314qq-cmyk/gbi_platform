package com.gbi.platform.entity.hr;

import com.gbi.platform.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 薪酬级别变更流水
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_employee_grade_log")
public class HrEmployeeGradeLog extends BaseEntity {
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    private String fromGradeCode;
    private String toGradeCode;
    private Integer changeType;
    private String changeReason;
    private Long flowInstanceId;
    private java.time.LocalDate effectiveDate;
    private java.math.BigDecimal basicSalaryBefore;
    private java.math.BigDecimal basicSalaryAfter;
}

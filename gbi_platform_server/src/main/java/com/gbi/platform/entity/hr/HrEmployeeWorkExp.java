package com.gbi.platform.entity.hr;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gbi.platform.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 员工工作经历实体
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_employee_work_exp")
public class HrEmployeeWorkExp extends BaseEntity {
    private Long companyId;
    private Long employeeId;
    private String companyName;
    private String position;
    private String department;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer isCurrent;
    private String reasonForLeaving;
    private String remark;
}

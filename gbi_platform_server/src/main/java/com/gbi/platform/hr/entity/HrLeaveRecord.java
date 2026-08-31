package com.gbi.platform.hr.entity;

import com.gbi.platform.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * HR请假记录实体
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_leave_record")
public class HrLeaveRecord extends BaseEntity {
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    private Integer leaveType;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    private java.math.BigDecimal days;
    private Integer leaveTypeHr;
    private Long oaLeaveId;
    private Long flowInstanceId;
    private Integer status;
    private String remark;
}

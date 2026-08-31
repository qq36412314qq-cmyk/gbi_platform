package com.gbi.platform.hr.entity;

import com.gbi.platform.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_attendance_record")
public class HrAttendanceRecord extends BaseEntity {
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    private String attendanceMonth;
    private java.time.LocalDate attendanceDay;
    private java.time.LocalDateTime clockInTime;
    private java.time.LocalDateTime clockOutTime;
    private Integer clockType;
    private Integer lateMinutes;
    private Integer earlyMinutes;
    private Integer absent;
    private String leaveDays;
    private String workDays;
    private String actualDays;
    private String remark;
}

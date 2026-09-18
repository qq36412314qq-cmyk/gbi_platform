package com.gbi.platform.entity.hr;

import com.gbi.platform.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_attendance_record")
public class HrAttendanceRecord extends BaseEntity {
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    /** 考勤月份 yyyy-MM，加反引号避免 JsqlParser 将 month 识别为 MySQL 函数 */
    @TableField("`attendance_month`")
    private String attendanceMonth;
    /** 考勤日期，加反引号避免 JsqlParser 将 day 识别为 MySQL 函数 */
    @TableField("`attendance_day`")
    private java.time.LocalDate attendanceDay;
    /** 上班打卡时间，加反引号避免 JsqlParser 将 time 识别为 MySQL 函数 */
    @TableField("`clock_in_time`")
    private java.time.LocalDateTime clockInTime;
    /** 下班打卡时间，加反引号避免 JsqlParser 将 time 识别为 MySQL 函数 */
    @TableField("`clock_out_time`")
    private java.time.LocalDateTime clockOutTime;
    /** 打卡状态 1正常 2迟到 3早退 4缺卡，加反引号避免 JsqlParser 将 type 识别为关键字 */
    @TableField("`clock_type`")
    private Integer clockType;
    private Integer lateMinutes;
    private Integer earlyMinutes;
    private Integer absent;
    /** 请假天数，数据库 decimal(4,1) */
    private BigDecimal leaveDays;
    /** 请假类型 0无薪事假 1有薪年假 2婚假 3产假 4病假 5工伤假 6公差 7调休假 */
    private Integer leaveType;
    /** 请假明细JSON */
    private String leaveDaysDetail;
    /** 应出勤天数，数据库 int(11) */
    private Integer workDays;
    /** 实际出勤天数，数据库 int(11) */
    private Integer actualDays;
    private String remark;
}

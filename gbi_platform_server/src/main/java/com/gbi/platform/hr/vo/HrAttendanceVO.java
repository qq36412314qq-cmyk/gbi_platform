package com.gbi.platform.hr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description="考勤记录视图")
@Data
public class HrAttendanceVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description="主键ID")
    private Long id;

    @Schema(description="公司ID")
    private Long companyId;

    @Schema(description="员工ID")
    private Long employeeId;

    @Schema(description="员工姓名")
    private String employeeName;

    @Schema(description="考勤月份 yyyy-MM")
    private String attendanceMonth;

    @Schema(description="考勤日期")
    private LocalDate attendanceDay;

    @Schema(description="上班打卡时间")
    private LocalDateTime clockInTime;

    @Schema(description="下班打卡时间")
    private LocalDateTime clockOutTime;

    @Schema(description="打卡状态 1正常 2迟到 3早退 4缺卡")
    private Integer clockType;

    @Schema(description="打卡状态文本")
    private String clockTypeText;

    @Schema(description="迟到分钟数")
    private Integer lateMinutes;

    @Schema(description="早退分钟数")
    private Integer earlyMinutes;

    @Schema(description="是否旷工")
    private Integer absent;

    @Schema(description="请假扣减天数")
    private String leaveDays;

    @Schema(description="应出勤天数")
    private String workDays;

    @Schema(description="实际出勤天数")
    private String actualDays;

    @Schema(description="备注")
    private String remark;

    @Schema(description="创建时间")
    private LocalDateTime createTime;
}

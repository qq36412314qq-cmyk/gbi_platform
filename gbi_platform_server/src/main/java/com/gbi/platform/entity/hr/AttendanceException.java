package com.gbi.platform.entity.hr;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gbi.platform.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤异常记录实体：hr_attendance_exception
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_attendance_exception")
public class AttendanceException extends BaseEntity {

    /** 所属公司ID */
    private Long companyId;

    /** 员工ID */
    private Long employeeId;

    /** 员工姓名快照 */
    private String employeeName;

    /** 异常类型 1连续缺卡 2月度迟到频繁 3旷工 4早退频繁 */
    private Integer exceptionType;

    /** 异常发生日期 */
    private LocalDate exceptionDate;

    /** 详情数量，如连续缺卡天数、迟到次数等 */
    private Integer detailCount;

    /** 详情JSON，记录具体的异常日期列表 */
    private String detailJson;

    /** 处理状态 0待处理 1已确认 2已豁免 3已忽略 */
    private Integer status;

    /** 处理人ID */
    private Long handleBy;

    /** 处理时间 */
    private LocalDateTime handleTime;

    /** 处理备注 */
    private String handleRemark;
}

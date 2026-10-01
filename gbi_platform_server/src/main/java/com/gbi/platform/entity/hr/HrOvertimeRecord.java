package com.gbi.platform.entity.hr;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 加班记录实体，对应表 hr_overtime_record
 * （追加型记录表，无 update 字段，审计字段由 MyBatis-Plus MetaObjectHandler 自动填充）
 *
 * @author gbi
 */
@Data
@TableName("hr_overtime_record")
public class HrOvertimeRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属公司ID */
    private Long companyId;

    /** 员工ID */
    private Long employeeId;

    /** 员工姓名快照 */
    private String employeeName;

    /** 加班日期 */
    private LocalDate overtimeDate;

    /** 加班开始时间 */
    private LocalTime startTime;

    /** 加班结束时间 */
    private LocalTime endTime;

    /** 加班时长（小时） */
    private BigDecimal overtimeHours;

    /** 加班类型 1工作日 2休息日 3法定节假日 */
    private Integer overtimeType;

    /** 来源类型 1手动申请 2自动识别 */
    private Integer sourceType;

    /** 关联申请ID */
    private Long applyId;

    /** 关联考勤记录ID */
    private Long attendRecordId;

    /** 确认状态 0待确认 1已确认 2已驳回 */
    private Integer confirmStatus;

    /** 确认时间 */
    private LocalDateTime confirmTime;

    /** 状态 1有效 2已抵扣 3已作废 */
    private Integer status;

    /** 补偿状态 0未补偿 1已调休 2已发放加班费 */
    private Integer compensateStatus;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 逻辑删除 0正常 1删除 */
    @TableLogic
    private Integer isDelete;
}

package com.gbi.platform.entity.hr;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gbi.platform.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 加班申请表实体，对应表 hr_overtime_apply
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_overtime_apply")
public class HrOvertimeApply extends BaseEntity {

    /** 所属公司ID（多租户隔离） */
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

    /** 预计加班时长（小时） */
    private BigDecimal expectedHours;

    /** 加班类型 1工作日 2休息日 3法定节假日 */
    private Integer overtimeType;

    /** 加班事由 */
    private String reason;

    /** 状态 0待审批 1已通过 2已驳回 3已撤回 4已取消 */
    private Integer status;

    /** 关联流程实例ID */
    private Long flowInstanceId;

    /** 当前流程任务ID */
    private Long flowTaskId;
}

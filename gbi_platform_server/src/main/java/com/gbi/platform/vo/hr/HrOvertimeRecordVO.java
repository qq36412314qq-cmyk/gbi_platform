package com.gbi.platform.vo.hr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 加班记录 VO
 *
 * @author gbi
 */
@Data
@Schema(description = "加班记录")
public class HrOvertimeRecordVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID")
    private Long id;
    @Schema(description = "所属公司ID")
    private Long companyId;
    @Schema(description = "员工ID")
    private Long employeeId;
    @Schema(description = "员工姓名")
    private String employeeName;
    @Schema(description = "加班日期")
    private LocalDate overtimeDate;
    @Schema(description = "开始时间")
    private LocalTime startTime;
    @Schema(description = "结束时间")
    private LocalTime endTime;
    @Schema(description = "加班时长（小时）")
    private BigDecimal overtimeHours;
    /** 加班类型 1工作日 2休息日 3法定节假日 */
    @Schema(description = "加班类型")
    private Integer overtimeType;
    /** 来源类型 1手动申请 2自动识别 */
    @Schema(description = "来源类型")
    private Integer sourceType;
    @Schema(description = "关联申请ID")
    private Long applyId;
    @Schema(description = "关联考勤记录ID")
    private Long attendRecordId;
    /** 确认状态 0待确认 1已确认 2已驳回 */
    @Schema(description = "确认状态")
    private Integer confirmStatus;
    @Schema(description = "确认时间")
    private LocalDateTime confirmTime;
    /** 状态 1有效 2已抵扣 3已作废 */
    @Schema(description = "状态")
    private Integer status;
    /** 补偿状态 0未补偿 1已调休 2已发放加班费 */
    @Schema(description = "补偿状态")
    private Integer compensateStatus;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

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
 * 加班申请 VO
 *
 * @author gbi
 */
@Data
@Schema(description = "加班申请")
public class HrOvertimeApplyVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "申请ID")
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
    @Schema(description = "预计加班时长（小时）")
    private BigDecimal expectedHours;
    /** 加班类型 1工作日 2休息日 3法定节假日 */
    @Schema(description = "加班类型")
    private Integer overtimeType;
    @Schema(description = "加班事由")
    private String reason;
    /** 状态 0待审批 1已通过 2已驳回 3已撤回 4已取消 */
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "流程实例ID")
    private Long flowInstanceId;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

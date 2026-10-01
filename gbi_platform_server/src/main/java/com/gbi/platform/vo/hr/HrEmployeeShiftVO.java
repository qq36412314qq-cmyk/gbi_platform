package com.gbi.platform.vo.hr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 员工班次配置视图
 *
 * @author gbi
 */
@Schema(description = "员工班次配置视图")
@Data
public class HrEmployeeShiftVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "班次类型 1标准工时 2早班 3晚班 4夜班 5综合工时")
    private Integer shiftType;

    @Schema(description = "班次类型文本")
    private String shiftTypeText;

    @Schema(description = "班次上班时间")
    private LocalTime shiftStartTime;

    @Schema(description = "班次下班时间")
    private LocalTime shiftEndTime;

    @Schema(description = "班次生效开始日期")
    private LocalDate startDate;

    @Schema(description = "班次生效结束日期")
    private LocalDate endDate;

    @Schema(description = "状态 0禁用 1启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

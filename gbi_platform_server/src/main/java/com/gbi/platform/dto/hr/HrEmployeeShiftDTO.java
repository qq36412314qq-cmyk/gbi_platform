package com.gbi.platform.dto.hr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 员工班次配置DTO
 *
 * @author gbi
 */
@Schema(description = "员工班次配置DTO")
@Data
public class HrEmployeeShiftDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID（更新时必填）")
    private Long id;

    @Schema(description = "所属公司ID")
    private Long companyId;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "班次类型 1标准工时 2早班 3晚班 4夜班 5综合工时")
    private Integer shiftType;

    @Schema(description = "班次上班时间")
    private LocalTime shiftStartTime;

    @Schema(description = "班次下班时间")
    private LocalTime shiftEndTime;

    @Schema(description = "班次生效开始日期")
    private LocalDate startDate;

    @Schema(description = "班次生效结束日期，null表示长期有效")
    private LocalDate endDate;

    @Schema(description = "状态 0禁用 1启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}

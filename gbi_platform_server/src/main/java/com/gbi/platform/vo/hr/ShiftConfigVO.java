package com.gbi.platform.vo.hr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;

/**
 * 班次配置视图
 *
 * @author gbi
 */
@Schema(description = "班次配置视图")
@Data
public class ShiftConfigVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "班次类型 1标准工时 2早班 3晚班 4夜班 5综合工时")
    private Integer shiftType;

    @Schema(description = "班次类型文本")
    private String shiftTypeText;

    @Schema(description = "班次上班时间")
    private LocalTime shiftStartTime;

    @Schema(description = "班次下班时间")
    private LocalTime shiftEndTime;
}

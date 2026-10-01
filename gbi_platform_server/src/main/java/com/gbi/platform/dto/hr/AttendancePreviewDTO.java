package com.gbi.platform.dto.hr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 考勤同步预览入参DTO
 *
 * @author gbi
 */
@Schema(description = "考勤同步预览入参")
@Data
public class AttendancePreviewDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "考勤月份 yyyy-MM")
    private String month;

    @Schema(description = "员工范围 0=全部 1=指定")
    private Integer employeeScope;

    @Schema(description = "指定员工ID列表")
    private List<Long> employeeIds;

    @Schema(description = "同步类型 1=首次同步 2=重新同步")
    private Integer syncType;
}

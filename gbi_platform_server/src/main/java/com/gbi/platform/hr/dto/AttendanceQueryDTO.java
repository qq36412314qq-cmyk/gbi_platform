package com.gbi.platform.hr.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial; import java.io.Serializable;
@Schema(description="考勤查询入参")
@Data public class AttendanceQueryDTO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description="页码") private Long pageNum;
    @Schema(description="每页条数") private Long pageSize;
    @Schema(description="员工ID") private Long employeeId;
    @Schema(description="考勤月份 yyyy-MM") private String attendanceMonth;
}

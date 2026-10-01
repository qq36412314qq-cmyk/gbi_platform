package com.gbi.platform.vo.hr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤异常记录视图
 *
 * @author gbi
 */
@Schema(description = "考勤异常记录视图")
@Data
public class AttendanceExceptionVO implements Serializable {
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

    @Schema(description = "异常类型 1连续缺卡 2月度迟到频繁 3旷工 4早退频繁")
    private Integer exceptionType;

    @Schema(description = "异常类型文本")
    private String exceptionTypeText;

    @Schema(description = "异常发生日期")
    private LocalDate exceptionDate;

    @Schema(description = "详情数量")
    private Integer detailCount;

    @Schema(description = "详情JSON")
    private String detailJson;

    @Schema(description = "处理状态 0待处理 1已确认 2已豁免 3已忽略")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "处理人ID")
    private Long handleBy;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "处理备注")
    private String handleRemark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

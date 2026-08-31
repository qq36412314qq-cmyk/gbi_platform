package com.gbi.platform.hr.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial; import java.io.Serializable;
import java.math.BigDecimal; import java.time.LocalDate; import java.time.LocalDateTime;
@Schema(description="HR请假记录视图")
@Data public class HrLeaveRecordVO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description="主键ID") private Long id;
    @Schema(description="公司ID") private Long companyId;
    @Schema(description="员工ID") private Long employeeId;
    @Schema(description="员工姓名") private String employeeName;
    @Schema(description="请假类型 1事假 2病假 3年假 4调休 5产假 6婚假 7陪产假 8其他") private Integer leaveType;
    @Schema(description="请假类型文本") private String leaveTypeText;
    @Schema(description="开始日期") private LocalDate startDate;
    @Schema(description="结束日期") private LocalDate endDate;
    @Schema(description="请假天数") private BigDecimal days;
    @Schema(description="HR考勤类型 1计入出勤 2不计入出勤") private Integer leaveTypeHr;
    @Schema(description="关联oa_leave_apply.id") private Long oaLeaveId;
    @Schema(description="流程实例ID") private Long flowInstanceId;
    @Schema(description="状态") private Integer status;
    @Schema(description="状态文本") private String statusText;
    @Schema(description="备注") private String remark;
    @Schema(description="创建时间") private LocalDateTime createTime;
}

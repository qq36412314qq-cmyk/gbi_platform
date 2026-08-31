package com.gbi.platform.hr.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial; import java.io.Serializable;
import java.time.LocalDate; import java.time.LocalDateTime;
@Schema(description="转正申请视图")
@Data public class HrRegularApplyVO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description="主键ID") private Long id;
    @Schema(description="公司ID") private Long companyId;
    @Schema(description="员工ID") private Long employeeId;
    @Schema(description="员工姓名") private String employeeName;
    @Schema(description="计划转正日期") private LocalDate regularDate;
    @Schema(description="备注") private String remark;
    @Schema(description="流程实例ID") private Long flowInstanceId;
    @Schema(description="状态") private Integer status;
    @Schema(description="状态文本") private String statusText;
    @Schema(description="创建时间") private LocalDateTime createTime;
}

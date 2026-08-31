package com.gbi.platform.hr.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial; import java.io.Serializable;
import java.time.LocalDate; import java.time.LocalDateTime;
@Schema(description="离职申请视图")
@Data public class HrResignApplyVO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description="主键ID") private Long id;
    @Schema(description="公司ID") private Long companyId;
    @Schema(description="员工ID") private Long employeeId;
    @Schema(description="员工姓名") private String employeeName;
    @Schema(description="计划离职日期") private LocalDate resignDate;
    @Schema(description="离职类型 1主动辞职 2合同到期 3辞退 4终止合同") private Integer resignType;
    @Schema(description="离职类型文本") private String resignTypeText;
    @Schema(description="离职原因") private String reason;
    @Schema(description="交接备注") private String handoverRemark;
    @Schema(description="流程实例ID") private Long flowInstanceId;
    @Schema(description="状态") private Integer status;
    @Schema(description="状态文本") private String statusText;
    @Schema(description="创建时间") private LocalDateTime createTime;
}

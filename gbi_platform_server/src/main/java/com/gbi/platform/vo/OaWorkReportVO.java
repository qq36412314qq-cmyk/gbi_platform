package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "OA工作汇报返回")
public class OaWorkReportVO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description = "主键ID") private Long id;
    @Schema(description = "所属子公司ID") private Long companyId;
    @Schema(description = "报告类型 1日报 2周报 3月报") private Integer reportType;
    @Schema(description = "报告类型文本") private String reportTypeText;
    @Schema(description = "报告周期") private String reportPeriod;
    @Schema(description = "提交人用户ID") private Long submitUserId;
    @Schema(description = "提交人姓名") private String submitUserName;
    @Schema(description = "报告内容") private String reportContent;
    @Schema(description = "关联审批实例ID") private Long flowInstanceId;
    @Schema(description = "报告状态 0草稿 1审批中 2通过 3驳回 4作废") private Integer reportStatus;
    @Schema(description = "报告状态文本") private String reportStatusText;
    @Schema(description = "备注") private String remark;
    @Schema(description = "创建时间") private LocalDateTime createTime;
}

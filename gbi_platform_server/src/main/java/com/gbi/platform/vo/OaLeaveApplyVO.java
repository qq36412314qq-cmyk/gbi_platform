package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "OA请假申请返回")
public class OaLeaveApplyVO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID") private Long id;
    @Schema(description = "申请单号") private String applyNo;
    @Schema(description = "申请人用户ID") private Long applyUserId;
    @Schema(description = "申请人姓名") private String applyUserName;
    @Schema(description = "请假类型 1年假 2事假 3病假 4调休 5产假 6其他") private Integer leaveType;
    @Schema(description = "请假类型文本") private String leaveTypeText;
    @Schema(description = "开始日期") private LocalDate startDate;
    @Schema(description = "结束日期") private LocalDate endDate;
    @Schema(description = "请假天数") private BigDecimal leaveDays;
    @Schema(description = "请假事由") private String reason;
    @Schema(description = "关联审批实例ID") private Long flowInstanceId;
    @Schema(description = "申请状态 0草稿 1审批中 2通过 3驳回 4作废") private Integer applyStatus;
    @Schema(description = "申请状态文本") private String applyStatusText;
    @Schema(description = "备注") private String remark;
    @Schema(description = "创建人用户ID") private Long createBy;
    @Schema(description = "创建时间") private LocalDateTime createTime;
}

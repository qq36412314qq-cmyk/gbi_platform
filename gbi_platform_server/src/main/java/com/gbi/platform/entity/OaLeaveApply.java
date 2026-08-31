package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oa_leave_apply")
public class OaLeaveApply extends BaseEntity {
    private Long companyId;
    private String applyNo;
    private Long applyUserId;
    private String applyUserName;
    private Integer leaveType;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    private java.math.BigDecimal leaveDays;
    private String reason;
    private Long flowInstanceId;
    private Integer applyStatus;
    private String remark;
}

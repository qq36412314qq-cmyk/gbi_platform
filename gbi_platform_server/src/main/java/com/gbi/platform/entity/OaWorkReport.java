package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oa_work_report")
public class OaWorkReport extends BaseEntity {
    private Long companyId;
    private Integer reportType;
    private String reportPeriod;
    private Long submitUserId;
    private String submitUserName;
    private String reportContent;
    private Long flowInstanceId;
    private Integer reportStatus;
    private String remark;
}

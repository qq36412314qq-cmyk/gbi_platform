package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oa_work_report")
public class OaWorkReport extends BaseEntity {
    private Long companyId;
    private Integer reportType;

    @TableField("report_date")
    private String reportPeriod;

    @TableField("user_id")
    private Long submitUserId;

    @TableField("user_name")
    private String submitUserName;

    @TableField("content")
    private String reportContent;

    @TableField("flow_instance_id")
    private Long flowInstanceId;

    @TableField("status")
    private Integer reportStatus;

    @TableField(exist = false)
    private String remark;
}
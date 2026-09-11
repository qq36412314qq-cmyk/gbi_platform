package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oa_leave_apply")
public class OaLeaveApply extends BaseEntity {
    private Long companyId;

    /** 申请单号（生成后写入，数据库实际列为 apply_no，但表中未建该列，暂作计算字段不回写）*/
    @TableField(exist = false)
    private String applyNo;

    /** 申请人用户ID，对应数据库列 applicant_id */
    @TableField("applicant_id")
    private Long applyUserId;

    /** 申请人姓名快照，对应数据库列 applicant_name */
    @TableField("applicant_name")
    private String applyUserName;

    private Integer leaveType;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;

    /** 请假天数，对应数据库列 days */
    @TableField("days")
    private java.math.BigDecimal leaveDays;

    private String reason;

    /** 关联审批流程实例ID，对应数据库列 flow_instance_id */
    @TableField("flow_instance_id")
    private Long flowInstanceId;

    /** 申请状态，对应数据库列 status */
    @TableField("status")
    private Integer applyStatus;

    /** 备注，数据库表中无此列 */
    @TableField(exist = false)
    private String remark;
}

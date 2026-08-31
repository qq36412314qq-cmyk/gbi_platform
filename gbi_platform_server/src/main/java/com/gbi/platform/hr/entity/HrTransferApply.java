package com.gbi.platform.hr.entity;

import com.gbi.platform.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 调岗申请实体
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_transfer_apply")
public class HrTransferApply extends BaseEntity {
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    private Long oldOrgId;
    private Long oldPostId;
    private Long newOrgId;
    private Long newPostId;
    private String newPostLevel;
    private java.time.LocalDate transferDate;
    private String reason;
    private Long flowInstanceId;
    private Integer status;
}

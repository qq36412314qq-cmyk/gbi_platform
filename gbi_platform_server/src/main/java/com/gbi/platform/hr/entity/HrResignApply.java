package com.gbi.platform.hr.entity;

import com.gbi.platform.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 离职申请实体
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_resign_apply")
public class HrResignApply extends BaseEntity {
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    private java.time.LocalDate resignDate;
    private Integer resignType;
    private String reason;
    private String handoverRemark;
    private Long flowInstanceId;
    private Integer status;
}

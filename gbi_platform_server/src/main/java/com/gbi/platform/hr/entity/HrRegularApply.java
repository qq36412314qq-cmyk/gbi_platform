package com.gbi.platform.hr.entity;

import com.gbi.platform.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 转正申请实体
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_regular_apply")
public class HrRegularApply extends BaseEntity {
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    private java.time.LocalDate regularDate;
    private String remark;
    private Long flowInstanceId;
    private Integer status;
}

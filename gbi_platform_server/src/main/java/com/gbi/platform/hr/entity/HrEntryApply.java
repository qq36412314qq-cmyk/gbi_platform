package com.gbi.platform.hr.entity;

import com.gbi.platform.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 入职申请实体
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_entry_apply")
public class HrEntryApply extends BaseEntity {
    private Long companyId;
    private String employeeNo;
    private String name;
    private String idCardNo;
    private String phone;
    private Integer gender;
    private java.time.LocalDate birthdate;
    private java.time.LocalDate entryDate;
    private Integer employmentType;
    private Long orgId;
    private Long postId;
    private java.math.BigDecimal basicSalary;
    private String bankAccount;
    private Integer autoCreateUser;
    private Long flowInstanceId;
    private Integer status;
    private String remark;
}

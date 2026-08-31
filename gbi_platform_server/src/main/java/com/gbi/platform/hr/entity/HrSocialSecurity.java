package com.gbi.platform.hr.entity;

import com.gbi.platform.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社保公积金台账实体
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_social_security")
public class HrSocialSecurity extends BaseEntity {
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    private java.math.BigDecimal socialSecurityBase;
    private java.math.BigDecimal housingFundBase;
    private java.math.BigDecimal socialSecurityCompany;
    private java.math.BigDecimal socialSecurityPersonal;
    private java.math.BigDecimal housingFundCompany;
    private java.math.BigDecimal housingFundPersonal;
    private String startMonth;
    private String endMonth;
    private Integer status;
    private String remark;
}

package com.gbi.platform.entity.hr;

import com.gbi.platform.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 薪资规则模板
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_salary_rule")
public class HrSalaryRule extends BaseEntity {
    private Long companyId;
    private String ruleName;
    private Integer bindType;
    private Long postId;
    private String gradeCode;
    private BigDecimal basicSalary;
    private BigDecimal performanceBase;
    private BigDecimal positionAllowance;
    private BigDecimal otherAllowance;
    private BigDecimal fixedMonthBonus;
    /** 社保个人比例(%),NULL表示继承全局参数 */
    private BigDecimal socialSecurityRate;
    /** 公积金个人比例(%),NULL表示继承全局参数 */
    private BigDecimal housingFundRate;
    private String remark;
    private Long flowInstanceId;
    private Integer applyStatus;
    private Integer status;
}

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
    /** 是否跳过考勤核算 0参与 1跳过 */
    private Integer skipAttendance;
    /** 迟到扣款费率倍数（默认1.0=全额扣） */
    private BigDecimal latePenaltyRate;
    /** 早退扣款费率倍数（默认1.0=全额扣） */
    private BigDecimal earlyPenaltyRate;
    private String remark;
    private Long flowInstanceId;
    private Integer applyStatus;
    private Integer status;
}

package com.gbi.platform.entity.hr;

import com.gbi.platform.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * 薪资规则模板
 */
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

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public Integer getBindType() { return bindType; }
    public void setBindType(Integer bindType) { this.bindType = bindType; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public String getGradeCode() { return gradeCode; }
    public void setGradeCode(String gradeCode) { this.gradeCode = gradeCode; }
    public BigDecimal getBasicSalary() { return basicSalary; }
    public void setBasicSalary(BigDecimal basicSalary) { this.basicSalary = basicSalary; }
    public BigDecimal getPerformanceBase() { return performanceBase; }
    public void setPerformanceBase(BigDecimal performanceBase) { this.performanceBase = performanceBase; }
    public BigDecimal getPositionAllowance() { return positionAllowance; }
    public void setPositionAllowance(BigDecimal positionAllowance) { this.positionAllowance = positionAllowance; }
    public BigDecimal getOtherAllowance() { return otherAllowance; }
    public void setOtherAllowance(BigDecimal otherAllowance) { this.otherAllowance = otherAllowance; }
    public BigDecimal getFixedMonthBonus() { return fixedMonthBonus; }
    public void setFixedMonthBonus(BigDecimal fixedMonthBonus) { this.fixedMonthBonus = fixedMonthBonus; }
    public BigDecimal getSocialSecurityRate() { return socialSecurityRate; }
    public void setSocialSecurityRate(BigDecimal socialSecurityRate) { this.socialSecurityRate = socialSecurityRate; }
    public BigDecimal getHousingFundRate() { return housingFundRate; }
    public void setHousingFundRate(BigDecimal housingFundRate) { this.housingFundRate = housingFundRate; }
    public Integer getSkipAttendance() { return skipAttendance; }
    public void setSkipAttendance(Integer skipAttendance) { this.skipAttendance = skipAttendance; }
    public BigDecimal getLatePenaltyRate() { return latePenaltyRate; }
    public void setLatePenaltyRate(BigDecimal latePenaltyRate) { this.latePenaltyRate = latePenaltyRate; }
    public BigDecimal getEarlyPenaltyRate() { return earlyPenaltyRate; }
    public void setEarlyPenaltyRate(BigDecimal earlyPenaltyRate) { this.earlyPenaltyRate = earlyPenaltyRate; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Long getFlowInstanceId() { return flowInstanceId; }
    public void setFlowInstanceId(Long flowInstanceId) { this.flowInstanceId = flowInstanceId; }
    public Integer getApplyStatus() { return applyStatus; }
    public void setApplyStatus(Integer applyStatus) { this.applyStatus = applyStatus; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}

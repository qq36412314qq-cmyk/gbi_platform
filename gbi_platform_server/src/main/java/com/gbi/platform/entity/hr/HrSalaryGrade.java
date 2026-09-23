package com.gbi.platform.entity.hr;

import com.gbi.platform.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * 薪酬级别实体
 */
@TableName("hr_salary_grade")
public class HrSalaryGrade extends BaseEntity {
    private Long companyId;
    private String gradeCode;
    private String gradeName;
    private Integer gradeLevel;
    private BigDecimal bandMin;
    private BigDecimal bandMid;
    private BigDecimal bandMax;
    private Integer status;
    private String remark;

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getGradeCode() { return gradeCode; }
    public void setGradeCode(String gradeCode) { this.gradeCode = gradeCode; }
    public String getGradeName() { return gradeName; }
    public void setGradeName(String gradeName) { this.gradeName = gradeName; }
    public Integer getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(Integer gradeLevel) { this.gradeLevel = gradeLevel; }
    public BigDecimal getBandMin() { return bandMin; }
    public void setBandMin(BigDecimal bandMin) { this.bandMin = bandMin; }
    public BigDecimal getBandMid() { return bandMid; }
    public void setBandMid(BigDecimal bandMid) { this.bandMid = bandMid; }
    public BigDecimal getBandMax() { return bandMax; }
    public void setBandMax(BigDecimal bandMax) { this.bandMax = bandMax; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}

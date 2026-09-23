package com.gbi.platform.entity.hr;

import com.gbi.platform.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * 批量调薪任务
 */
@TableName("hr_salary_batch_adjust")
public class HrSalaryBatchAdjust extends BaseEntity {
    private Long companyId;
    private String adjustName;
    private Integer adjustMode;
    private BigDecimal adjustValue;
    private String targetGradeCode;
    private java.time.LocalDate effectiveDate;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private Integer status;
    private Long flowInstanceId;
    private String remark;

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getAdjustName() { return adjustName; }
    public void setAdjustName(String adjustName) { this.adjustName = adjustName; }
    public Integer getAdjustMode() { return adjustMode; }
    public void setAdjustMode(Integer adjustMode) { this.adjustMode = adjustMode; }
    public BigDecimal getAdjustValue() { return adjustValue; }
    public void setAdjustValue(BigDecimal adjustValue) { this.adjustValue = adjustValue; }
    public String getTargetGradeCode() { return targetGradeCode; }
    public void setTargetGradeCode(String targetGradeCode) { this.targetGradeCode = targetGradeCode; }
    public java.time.LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(java.time.LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Integer getSuccessCount() { return successCount; }
    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }
    public Integer getFailCount() { return failCount; }
    public void setFailCount(Integer failCount) { this.failCount = failCount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getFlowInstanceId() { return flowInstanceId; }
    public void setFlowInstanceId(Long flowInstanceId) { this.flowInstanceId = flowInstanceId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}

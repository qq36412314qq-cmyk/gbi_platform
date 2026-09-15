package com.gbi.platform.entity.hr;

import com.gbi.platform.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 批量调薪任务
 */
@Data
@EqualsAndHashCode(callSuper = true)
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
}

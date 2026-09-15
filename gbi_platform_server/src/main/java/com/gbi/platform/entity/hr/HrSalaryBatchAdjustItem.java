package com.gbi.platform.entity.hr;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 批量调薪明细（无继承BaseEntity，id独立）
 */
@Data
@TableName("hr_salary_batch_adjust_item")
public class HrSalaryBatchAdjustItem {
    private Long id;
    private Long batchId;
    private Long employeeId;
    private String employeeName;
    private Long stallId;
    private BigDecimal basicSalaryBefore;
    private BigDecimal basicSalaryAfter;
    private String gradeCodeBefore;
    private String gradeCodeAfter;
    private BigDecimal adjustValue;
    private Integer adjustMode;
    private Integer resultStatus;
    private String resultMsg;
    private LocalDateTime createTime;
}

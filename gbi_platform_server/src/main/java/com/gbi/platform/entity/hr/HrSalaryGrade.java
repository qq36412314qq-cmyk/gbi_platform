package com.gbi.platform.entity.hr;

import com.gbi.platform.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 薪酬级别实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
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
}

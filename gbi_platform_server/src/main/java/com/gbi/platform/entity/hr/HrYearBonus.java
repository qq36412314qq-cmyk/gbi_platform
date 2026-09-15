package com.gbi.platform.entity.hr;

import com.gbi.platform.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 年终奖/一次性奖金
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_year_bonus")
public class HrYearBonus extends BaseEntity {
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    private Integer bonusYear;
    private Integer bonusType;
    private BigDecimal bonusAmount;
    private String bonusReason;
    private Integer payStatus;
    private Long flowInstanceId;
    private Integer applyStatus;
    private Integer status;
    private String remark;
}

package com.gbi.platform.entity.hr;

import com.gbi.platform.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 月度薪资核算单实体
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_salary_month")
public class HrSalaryMonth extends BaseEntity {
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    private String salaryMonth;
    private java.math.BigDecimal basicSalary;
    private java.math.BigDecimal performanceSalary;
    private java.math.BigDecimal allowanceAmount;
    private java.math.BigDecimal socialSecurity;
    private java.math.BigDecimal housingFund;
    private java.math.BigDecimal taxAmount;
    private java.math.BigDecimal deductionAmount;
    private java.math.BigDecimal grossAmount;
    private java.math.BigDecimal netAmount;
    /** 养老保险个人扣除 */
    private java.math.BigDecimal pensionPersonal;
    /** 养老保险单位缴纳 */
    private java.math.BigDecimal pensionCompany;
    /** 医疗保险个人扣除 */
    private java.math.BigDecimal medicalPersonal;
    /** 医疗保险单位缴纳 */
    private java.math.BigDecimal medicalCompany;
    /** 失业保险个人扣除 */
    private java.math.BigDecimal unemploymentPersonal;
    /** 失业保险单位缴纳 */
    private java.math.BigDecimal unemploymentCompany;
    /** 工伤保险单位缴纳 */
    private java.math.BigDecimal workInjuryCompany;
    /** 生育保险单位缴纳 */
    private java.math.BigDecimal maternityCompany;
    /** 长期护理险个人扣除 */
    private java.math.BigDecimal longCarePersonal;
    /** 长期护理险单位缴纳 */
    private java.math.BigDecimal longCareCompany;
    /** 公积金个人扣除 */
    private java.math.BigDecimal housingFundPersonal;
    /** 公积金单位缴纳 */
    private java.math.BigDecimal housingFundCompany;
    /** 社保实际缴费基数 */
    private java.math.BigDecimal socialBase;
    /** 公积金实际缴费基数 */
    private java.math.BigDecimal housingFundBase;
    /** 基数生效年度快照 */
    private String baseEffectiveYear;
    private Integer payStatus;
    private java.time.LocalDateTime payTime;
    private Long flowInstanceId;
    private Long planId;
    private String remark;
}

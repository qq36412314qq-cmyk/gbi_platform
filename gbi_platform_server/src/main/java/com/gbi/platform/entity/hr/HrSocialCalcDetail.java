package com.gbi.platform.entity.hr;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gbi.platform.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 社保公积金核算明细实体：hr_social_calc_detail
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_social_calc_detail")
public class HrSocialCalcDetail extends BaseEntity {

    /** 所属公司ID */
    private Long companyId;

    /** 员工ID */
    private Long employeeId;

    /** 员工姓名快照 */
    private String employeeName;

    /** 城市快照 */
    private String cityCode;

    /** 薪资月份，如2026-09 */
    private String salaryMonth;

    /** 基数生效年度快照 */
    private String baseEffectiveYear;

    /** 社保实际缴费基数 */
    private BigDecimal socialBase;

    /** 公积金实际缴费基数 */
    private BigDecimal housingFundBase;

    // ========== 养老 ==========
    private BigDecimal pensionPersonal;
    private BigDecimal pensionCompany;
    private BigDecimal pensionRatePersonal;
    private BigDecimal pensionRateCompany;

    // ========== 医疗 ==========
    private BigDecimal medicalPersonal;
    private BigDecimal medicalCompany;
    private BigDecimal medicalRatePersonal;
    private BigDecimal medicalRateCompany;

    // ========== 失业 ==========
    private BigDecimal unemploymentPersonal;
    private BigDecimal unemploymentCompany;
    private BigDecimal unemploymentRatePersonal;
    private BigDecimal unemploymentRateCompany;

    // ========== 工伤 ==========
    private BigDecimal workInjuryCompany;
    private BigDecimal workInjuryRate;

    // ========== 生育 ==========
    private BigDecimal maternityCompany;
    private BigDecimal maternityRate;

    // ========== 长护险 ==========
    private BigDecimal longCarePersonal;
    private BigDecimal longCareCompany;
    private BigDecimal longCareRatePersonal;
    private BigDecimal longCareRateCompany;

    // ========== 公积金 ==========
    private BigDecimal housingFundPersonal;
    private BigDecimal housingFundCompany;
    private BigDecimal housingFundRate;

    /** 计算尾差 */
    private BigDecimal roundingDiff;
}

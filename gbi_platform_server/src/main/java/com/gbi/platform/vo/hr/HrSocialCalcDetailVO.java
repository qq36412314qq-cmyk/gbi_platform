package com.gbi.platform.vo.hr;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 社保核算明细视图对象
 */
@Data
public class HrSocialCalcDetailVO {
    private Long id;
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    private String cityCode;
    private String cityName;
    private String salaryMonth;
    private String baseEffectiveYear;
    private BigDecimal socialBase;
    private BigDecimal housingFundBase;
    // 养老
    private BigDecimal pensionPersonal;
    private BigDecimal pensionCompany;
    // 医疗
    private BigDecimal medicalPersonal;
    private BigDecimal medicalCompany;
    // 失业
    private BigDecimal unemploymentPersonal;
    private BigDecimal unemploymentCompany;
    // 工伤
    private BigDecimal workInjuryCompany;
    // 生育
    private BigDecimal maternityCompany;
    // 长护险
    private BigDecimal longCarePersonal;
    private BigDecimal longCareCompany;
    // 公积金
    private BigDecimal housingFundPersonal;
    private BigDecimal housingFundCompany;
    // 尾差
    private BigDecimal roundingDiff;
    private String createTime;
}

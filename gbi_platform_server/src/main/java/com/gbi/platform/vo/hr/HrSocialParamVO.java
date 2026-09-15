package com.gbi.platform.vo.hr;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 社保参数配置视图对象
 */
@Data
public class HrSocialParamVO {
    private Long id;
    private Long companyId;
    private String cityCode;
    /** 城市名称（关联查询） */
    private String cityName;
    private String insuranceCode;
    /** 险种名称（关联查询） */
    private String insuranceName;
    private String industryCode;
    /** 行业名称（关联查询） */
    private String industryName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal baseMin;
    private BigDecimal baseMax;
    private BigDecimal personalRate;
    private BigDecimal companyRate;
    private Integer isActive;
    private String remark;
    private String createTime;
}

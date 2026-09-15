package com.gbi.platform.vo.hr;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 公积金参数配置视图对象
 */
@Data
public class HrHousingFundVO {
    private Long id;
    private Long companyId;
    private String cityCode;
    /** 城市名称（关联查询） */
    private String cityName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal baseMin;
    private BigDecimal baseMax;
    private BigDecimal employeeRate;
    private BigDecimal companyRate;
    private Integer isActive;
    private String remark;
    private String createTime;
}

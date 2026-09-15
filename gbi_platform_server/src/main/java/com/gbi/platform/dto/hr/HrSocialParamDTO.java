package com.gbi.platform.dto.hr;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 社保参数配置DTO
 */
@Data
public class HrSocialParamDTO {
    private Long id;

    @NotBlank(message = "城市编码不能为空")
    private String cityCode;

    @NotBlank(message = "险种编码不能为空")
    private String insuranceCode;

    private String industryCode;

    @NotNull(message = "生效起始日期不能为空")
    private LocalDate periodStart;

    private LocalDate periodEnd;

    @NotNull(message = "基数下限不能为空")
    private BigDecimal baseMin;

    @NotNull(message = "基数上限不能为空")
    private BigDecimal baseMax;

    @NotNull(message = "个人比例不能为空")
    private BigDecimal personalRate;

    @NotNull(message = "单位比例不能为空")
    private BigDecimal companyRate;

    private String remark;
}

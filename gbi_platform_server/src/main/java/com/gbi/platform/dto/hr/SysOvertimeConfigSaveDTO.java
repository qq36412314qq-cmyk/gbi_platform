package com.gbi.platform.dto.hr;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 加班配置保存入参
 *
 * @author gbi
 */
@Data
public class SysOvertimeConfigSaveDTO {

    @NotNull(message = "配置ID不能为空")
    private Long id;

    private String configName;
    private BigDecimal overtimeMinHours;
    private Integer overtimeRoundMode;
    private BigDecimal workdayRate;
    private BigDecimal restdayRate;
    private BigDecimal holidayRate;
    private BigDecimal maxOvertimeHours;
    private Integer compensatePriority;
    private Integer status;
    private String remark;
}

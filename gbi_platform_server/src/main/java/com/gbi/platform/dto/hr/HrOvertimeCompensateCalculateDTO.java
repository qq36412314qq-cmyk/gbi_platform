package com.gbi.platform.dto.hr;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 加班补偿核算入参
 *
 * @author gbi
 */
@Data
public class HrOvertimeCompensateCalculateDTO {

    /** 核算月份，格式 yyyy-MM */
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "补偿月份格式必须为 yyyy-MM")
    private String compensateMonth;
}

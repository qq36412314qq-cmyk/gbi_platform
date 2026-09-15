package com.gbi.platform.dto.hr;

import lombok.Data;

/**
 * 年度基数重算请求DTO
 */
@Data
public class HrAnnualRecalcDTO {
    /** 重算年度，如2026 */
    private String recalcYear;
    /** 公司ID，null表示集团全局 */
    private Long companyId;
}

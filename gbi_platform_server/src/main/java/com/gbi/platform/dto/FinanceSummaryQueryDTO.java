package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 财务营收汇总查询入参（集团看全量，子公司自动过滤本公司）
 *
 * @author gbi
 */
@Data
@Schema(description = "财务营收汇总查询入参")
public class FinanceSummaryQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "业务类型 rent/water_elec/deposit/marketing，空则全部")
    private String businessType;

    @Schema(description = "所属子公司ID（仅集团账号可传）")
    private Long companyId;

    @Schema(description = "开始时间 yyyy-MM-dd HH:mm:ss")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}$", message = "开始时间格式不正确")
    private String startTime;

    @Schema(description = "结束时间 yyyy-MM-dd HH:mm:ss")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}$", message = "结束时间格式不正确")
    private String endTime;
}

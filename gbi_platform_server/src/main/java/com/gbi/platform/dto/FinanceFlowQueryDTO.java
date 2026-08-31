package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 财务流水分页查询入参
 * 多租户：子公司账号自动过滤 company_id，集团账号查全量（拦截器兜底）
 *
 * @author gbi
 */
@Data
@Schema(description = "财务流水分页查询入参")
public class FinanceFlowQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum;

    @Schema(description = "每页条数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private Integer pageSize;

    @Schema(description = "业务类型 rent/water_elec/deposit/marketing")
    private String businessType;

    @Schema(description = "流水类型 1收入 2支出退费")
    private Integer flowType;

    @Schema(description = "所属子公司ID（仅集团账号可传）")
    private Long companyId;

    @Schema(description = "开始时间 yyyy-MM-dd HH:mm:ss")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}$", message = "开始时间格式不正确")
    private String startTime;

    @Schema(description = "结束时间 yyyy-MM-dd HH:mm:ss")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}$", message = "结束时间格式不正确")
    private String endTime;

    /** 解析后的开始时间（Service 层填充） */
    public LocalDateTime getStartTimeValue() {
        return startTime == null || startTime.isBlank() ? null : LocalDateTime.parse(startTime.replace(' ', 'T'));
    }

    /** 解析后的结束时间（Service 层填充） */
    public LocalDateTime getEndTimeValue() {
        return endTime == null || endTime.isBlank() ? null : LocalDateTime.parse(endTime.replace(' ', 'T'));
    }
}

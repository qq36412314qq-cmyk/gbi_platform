package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 物业费账单分页查询入参
 *
 * @author gbi
 */
@Data
@Schema(description = "物业费账单分页查询入参")
public class PropertyFeeBillQueryDTO implements Serializable {

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

    @Schema(description = "账单月份 yyyy-MM")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "账单月份格式须为 yyyy-MM")
    private String billMonth;

    @Schema(description = "铺位ID")
    private Long stallId;

    @Schema(description = "市场ID（按铺位所属市场筛选）")
    private Long marketId;

    @Schema(description = "缴费状态 0待缴 1部分缴费 2已缴 3已退费 4已冲红 5已作废")
    private Integer payStatus;

    @Schema(description = "计费方式 1定额 2按面积")
    private Integer calcMode;
}

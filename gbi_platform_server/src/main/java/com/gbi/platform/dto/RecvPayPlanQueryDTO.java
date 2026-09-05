package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应收应付计划分页查询入参
 *
 * @author gbi
 */
@Data
@Schema(description = "应收应付计划分页查询入参")
public class RecvPayPlanQueryDTO implements Serializable {

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

    @Schema(description = "方向 1应收 2应付")
    private Integer direction;

    @Schema(description = "业务类型 rent/deposit/property/fee_bill")
    private String bizType;

    @Schema(description = "计划状态 0待执行 1部分 2完成 3逾期 4作废 5终止")
    private Integer planStatus;

    @Schema(description = "红冲标记 0正常 1反向冲销")
    private Integer redFlag;

    @Schema(description = "铺位ID")
    private Long stallId;

    @Schema(description = "期次标识 yyyy-MM/once")
    private String periodNo;
}
package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 市场档案分页查询入参
 *
 * @author gbi
 */
@Data
@Schema(description = "市场档案分页查询入参")
public class MarketQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum;

    @Schema(description = "每页条数", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大100")
    private Integer pageSize;

    @Schema(description = "市场名称（模糊）")
    private String marketName;

    @Schema(description = "状态 0停用 1启用")
    private Integer status;
}
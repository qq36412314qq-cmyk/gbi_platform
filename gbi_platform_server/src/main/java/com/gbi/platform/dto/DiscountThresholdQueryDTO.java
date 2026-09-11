package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 阈值配置分页查询 DTO
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper=false)
@Schema(description = "阈值配置分页查询入参")
public class DiscountThresholdQueryDTO extends PageDTO {

    @Schema(description = "业务类型筛选")
    private String bizType;

    @Schema(description = "阈值类型筛选")
    private Integer thresholdType;

    @Schema(description = "状态筛选 0停用 1启用")
    private Integer status;
}
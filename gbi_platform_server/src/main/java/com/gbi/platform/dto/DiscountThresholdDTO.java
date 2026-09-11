package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 阈值配置新增/编辑 DTO
 *
 * @author gbi
 */
@Data
@Schema(description = "阈值配置新增/编辑入参")
public class DiscountThresholdDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "阈值ID（编辑时必填）")
    private Long id;

    @NotNull(message = "业务类型不能为空")
    @Schema(description = "业务类型 rent/property_fee/water_elec/kindergarten")
    private String bizType;

    @NotNull(message = "阈值类型不能为空")
    @Schema(description = "阈值类型 1免租期上限 2折扣率下限 3减免金额上限 4定额上限 5占比上限")
    private Integer thresholdType;

    @NotNull(message = "阈值数值不能为空")
    @Schema(description = "阈值数值")
    private BigDecimal thresholdValue;

    @Schema(description = "是否需审批 0否 1是")
    private Integer requireAudit;

    @Schema(description = "备注")
    private String remark;
}
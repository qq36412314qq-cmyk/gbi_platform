package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 远程抄表入参（提交新读数，校验不小于当前读数）
 *
 * @author gbi
 */
@Data
@Schema(description = "远程抄表入参")
public class WaterElecReadDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "设备ID不能为空")
    private Long meterId;

    @Schema(description = "本次抄表读数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "本次读数不能为空")
    @DecimalMin(value = "0.00", message = "读数不能为负数")
    private BigDecimal currentRead;
}

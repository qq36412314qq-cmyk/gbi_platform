package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 编辑水电表设备入参（对齐前端 waterElecMeter 表单）
 *
 * @author gbi
 */
@Data
@Schema(description = "编辑水电表设备入参")
public class WaterElecMeterUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "设备ID不能为空")
    private Long id;

    @Schema(description = "绑定铺位ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "绑定铺位不能为空")
    private Long stallId;

    @Schema(description = "智能表设备编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "设备编号不能为空")
    @Size(max = 64, message = "设备编号不能超过64字符")
    private String meterNo;

    @Schema(description = "表类型 1水表 2电表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "表类型不能为空")
    private Integer meterType;

    @Schema(description = "物联网网关编码")
    @Size(max = 64, message = "网关编码不能超过64字符")
    private String gatewayCode;

    @Schema(description = "当前读数")
    private BigDecimal currentRead;

    @Schema(description = "账户余额")
    private BigDecimal balanceAmount;
}

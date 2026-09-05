package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 水电表设备分页查询入参
 *
 * @author gbi
 */
@Data
@Schema(description = "水电表设备分页查询入参")
public class WaterElecMeterQueryDTO implements Serializable {

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

    @Schema(description = "设备编号（模糊）")
    private String meterNo;

    @Schema(description = "表类型 1水表 2电表")
    private Integer meterType;

    @Schema(description = "设备状态 0断电 1通电")
    private Integer status;

    @Schema(description = "所属市场ID（按绑定铺位所属市场过滤）")
    private Long marketId;
}

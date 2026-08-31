package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 水电物业退费入参（按缴费记录原路退，幂等 requestId）
 *
 * @author gbi
 */
@Data
@Schema(description = "水电物业退费入参")
public class WaterElecRefundDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "原缴费记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "缴费记录ID不能为空")
    private Long payRecordId;

    @Schema(description = "幂等请求ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请求ID不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{8,64}$", message = "请求ID须为8-64位字母/数字/下划线/横线")
    private String requestId;

    @Schema(description = "退费备注")
    @Size(max = 500, message = "备注不能超过500字符")
    private String remark;
}

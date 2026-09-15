package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 授权设备增改 DTO
 *
 * @author gbi
 */
@Data
@Schema(description = "授权设备增改DTO")
public class ClientDeviceAuthDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID（编辑时必填）")
    private Long id;

    @Schema(description = "主板SN", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "主板SN不能为空")
    private String motherboardSn;

    @Schema(description = "CPU编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "CPU编号不能为空")
    private String cpuId;

    @Schema(description = "硬盘序列号")
    private String diskSn;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "授权有效期")
    private LocalDateTime expireTime;

    @Schema(description = "状态 0禁用 1启用")
    private Integer status;
}

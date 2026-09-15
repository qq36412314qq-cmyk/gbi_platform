package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 硬件信息采集 DTO（登录时使用）
 *
 * @author gbi
 */
@Data
@Schema(description = "硬件信息采集DTO")
public class DeviceHardwareInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主板SN")
    @NotBlank(message = "主板SN不能为空")
    private String motherboardSn;

    @Schema(description = "CPU编号")
    @NotBlank(message = "CPU编号不能为空")
    private String cpuId;

    @Schema(description = "硬盘序列号")
    private String diskSn;

    @Schema(description = "RSA签名（由后端签名，前端透传）")
    private String signature;

    @Schema(description = "时间戳")
    private Long timestamp;

    @Schema(description = "随机 nonce")
    @NotBlank(message = "nonce不能为空")
    private String nonce;
}

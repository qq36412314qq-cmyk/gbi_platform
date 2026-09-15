package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 授权设备列表 VO
 *
 * @author gbi
 */
@Data
@Schema(description = "授权设备VO")
public class ClientDeviceAuthVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "主板SN")
    private String motherboardSn;

    @Schema(description = "CPU编号")
    private String cpuId;

    @Schema(description = "硬盘序列号")
    private String diskSn;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "授权人ID")
    private Long authorizedBy;

    @Schema(description = "授权有效期")
    private LocalDateTime expireTime;

    @Schema(description = "状态 0禁用 1启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户端设备授权实体：sys_client_device_auth
 * 集团统一授权，无 company_id 隔离
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_client_device_auth")
public class SysClientDeviceAuth extends BaseEntity {

    /** 主板SN */
    private String motherboardSn;

    /** CPU编号 */
    private String cpuId;

    /** 硬盘序列号（可选） */
    private String diskSn;

    /** 设备名称（备注） */
    private String deviceName;

    /** 授权人ID */
    private Long authorizedBy;

    /** 授权有效期（NULL表示永久） */
    private java.time.LocalDateTime expireTime;

    /** 状态 0禁用 1启用 */
    private Integer status;
}

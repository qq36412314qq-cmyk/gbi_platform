package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 智能水电表设备实体：water_elec_meter
 * 设备绑定摊位，支持远程抄表、合闸/断电费控
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("property_water_elec_meter")
public class WaterElecMeter extends BaseEntity {

    /** 所属子公司ID */
    private Long companyId;

    /** 绑定摊位ID */
    private Long stallId;

    /** 智能表设备编号 */
    private String meterNo;

    /** 表类型 1水表 2电表 */
    private Integer meterType;

    /** 物联网网关编码 */
    private String gatewayCode;

    /** 当前读数 */
    private BigDecimal currentRead;

    /** 账户余额 */
    private BigDecimal balanceAmount;

    /** 设备状态 0断电 1通电正常 */
    private Integer status;
}

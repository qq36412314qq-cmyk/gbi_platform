package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 水电物业月度账单实体：property_water_elec_bill
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("property_water_elec_bill")
public class WaterElecBill extends BaseEntity {

    /**
     * 租户公司ID
     */
    @TableField(value = "company_id", fill = FieldFill.INSERT)
    private Long companyId;

    /**
     * 铺位ID
     */
    private Long stallId;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 计费计划ID
     */
    private Long planId;

    /**
     * 账单月份 yyyy‑MM
     */
    private String billMonth;

    /**
     * 账单类别 1水费 2电费
     */
    private Integer category;

    /**
     * 上期表读数
     */
    @TableField("prev_meter_read")
    private BigDecimal prevMeterRead;

    /**
     * 使用量（MySQL关键字，用反引号转义）
     */
    @TableField("`usage`")
    private BigDecimal usage;

    /**
     * 单价
     */
    @TableField("unit_price")
    private BigDecimal unitPrice;

    /**
     * 账单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 支付状态：0未支付 1已支付
     */
    private Integer payStatus;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;
}

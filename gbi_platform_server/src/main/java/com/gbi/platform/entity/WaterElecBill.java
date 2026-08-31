package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 水电物业月度账单实体：water_elec_bill
 * 按收费类别分行存储（水费/电费各一行），缴费状态联动
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("water_elec_bill")
public class WaterElecBill extends BaseEntity {

    /** 所属子公司ID */
    private Long companyId;

    /** 摊位ID */
    private Long stallId;

    /** 商户ID */
    private Long merchantId;

    /** 关联应收应付计划ID */
    private Long planId;

    /** 账单月份 yyyy-MM */
    private String billMonth;

    /** 收费类别 2物业费 3水费 4电费 */
    private Integer category;

    /** 上期抄表读数（用于计算本期用量 = 本次读数 - 上期读数） */
    @TableField("prev_meter_read")
    private BigDecimal prevMeterRead;

    /** 用量（水/电为读数差，物业费定额时存 0） */
    @TableField("`usage`")
    private BigDecimal usage;

    /** 计费单价快照 */
    @TableField("`unit_price`")
    private BigDecimal unitPrice;

    /** 账单总应收金额 */
    private BigDecimal totalAmount;

    /** 缴费状态 0待缴 1已缴 2部分缴费 */
    private Integer payStatus;

    /** 缴费完成时间 */
    private LocalDateTime payTime;
}

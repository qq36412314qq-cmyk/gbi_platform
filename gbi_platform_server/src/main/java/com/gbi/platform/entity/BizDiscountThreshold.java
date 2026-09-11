package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 优惠审批阈值配置表实体：finance_discount_threshold
 * 支持按业务类型配置不同审批阈值
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_discount_threshold")
public class BizDiscountThreshold extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 所属子公司ID，0=集团统一 */
    private Long companyId;

    /** 业务类型 rent/property_fee/water_elec/kindergarten */
    private String bizType;

    /** 阈值类型 1免租期上限 2折扣率下限 3减免金额上限 4定额上限 5占比上限 */
    private Integer thresholdType;

    /** 阈值数值 */
    private BigDecimal thresholdValue;

    /** 是否需审批 0否 1是 */
    private Integer requireAudit;

    /** 状态 0停用 1启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
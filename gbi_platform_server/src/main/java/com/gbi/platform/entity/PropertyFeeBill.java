package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物业费月度账单实体：property_bill
 * 规则驱动，定额/按面积，周期系数计算，按月批量生成
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("property_bill")
public class PropertyFeeBill extends BaseEntity {

    /** 所属子公司ID */
    private Long companyId;

    /** 摊位ID */
    private Long stallId;

    /** 商户ID */
    private Long merchantId;

    /** 账单月份 yyyy-MM */
    private String billMonth;

    /** 计费规则ID（biz_fee_rule，快照） */
    private Long ruleId;

    /** 收费类型ID（biz_fee_item，固定=物业费） */
    private Long feeItemId;

    /** 收费方式 1定额 2按面积 */
    private Integer calcMode;

    /** 收费周期 0不使用 1按年 2按月 3按日 */
    private Integer periodType;

    /** 用量：定额=0，按面积=摊位面积（快照） */
    @TableField("`usage`")
    private BigDecimal usage;

    /** 计费单价快照（规则修改不回溯） */
    @TableField("`unit_price`")
    private BigDecimal unitPrice;

    /** 周期系数（按年/12、按日×当月天数、按月=1） */
    @TableField("`period_factor`")
    private BigDecimal periodFactor;

    /** 本条账单金额 */
    private BigDecimal amount;

    /** 关联应收应付计划ID */
    private Long planId;

    /** 缴费状态 0待缴 1已缴 2部分缴费 */
    private Integer payStatus;

    /** 缴费完成时间 */
    private LocalDateTime payTime;
}

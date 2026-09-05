package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 优惠策略表实体：biz_discount_policy
 * 集团模板 company_id=0 全子公司可见，子公司可自定义覆盖；
 * 优惠类型 1免租期 2折扣率 3减免金额 4组合
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_discount_policy")
public class BizDiscountPolicy extends BaseEntity {

    /** 所属子公司ID，0=集团模板 */
    private Long companyId;

    /** 策略名称 */
    private String policyName;

    /** 优惠类型 1免租期 2折扣率 3减免金额 4组合 */
    private Integer discountType;

    /** 免租期月数（type=1/4） */
    private Integer waiveMonths;

    /** 折扣率%（100=无折扣，type=2/4） */
    private BigDecimal discountRate;

    /** 减免金额（type=3/4） */
    private BigDecimal deductAmount;

    /** 适用范围 1按合同 2按摊位 */
    private Integer scopeType;

    /** 策略生效时间 */
    private LocalDate startTime;

    /** 策略失效时间，NULL永久 */
    private LocalDate endTime;

    /** 状态 0停用 1启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
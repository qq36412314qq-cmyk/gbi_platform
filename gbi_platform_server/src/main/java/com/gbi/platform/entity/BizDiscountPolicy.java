package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 优惠策略表实体：finance_discount_policy
 * 集团模板 company_id=0 全子公司可见，子公司可自定义覆盖；
 * 优惠类型 1免租期 2折扣率 3减免金额 4组合 5定额 6阶梯
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

    /** 业务类型 rent=租赁费 property_fee=物业费 water_elec=水电费 kindergarten=幼儿园费 */
    private String bizType;

    /** 优惠类型 1免租期 2折扣率 3减免金额 4组合 5定额 6阶梯 */
    private Integer discountType;

    /** 免租期月数（type=1/4） */
    private Integer waiveMonths;

    /** 折扣率%（100=无折扣，type=2/4） */
    private BigDecimal discountRate;

    /** 减免金额（type=3/4） */
    private BigDecimal deductAmount;

    /** 定额优惠金额（每月固定减免，type=5） */
    private BigDecimal fixedAmount;

    /** 阶梯配置JSON（type=6）[{"min_amount":0,"discount_rate":100}] */
    private String tierConfig;

    /** 适用范围 1按合同 2按铺位 3按租户 4按市场 5按分类 */
    private Integer scopeType;

    /** 适用范围ID列表JSON ["1","2","3"] */
    private String scopeIds;

    /** 策略生效时间 */
    private LocalDate startTime;

    /** 策略失效时间，NULL永久 */
    private LocalDate endTime;

    /** 最多申请月数限制，NULL表示不限 */
    private Integer maxApplyMonths;

    /** 自动审批 0需审批 1自动生效 */
    private Integer autoApprove;

    /** 状态 0停用 1启用 */
    private Integer status;

    /** 排序权重 */
    private Integer sortOrder;

    /** 备注 */
    private String remark;
}
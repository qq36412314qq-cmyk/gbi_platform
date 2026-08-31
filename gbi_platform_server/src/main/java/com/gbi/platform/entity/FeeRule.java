package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 自定义收费规则实体：biz_fee_rule
 * 规则调用收费类型（biz_fee_item）；收费方式 1定额 2按面积；
 * 收费周期 1按年 2按月 3按日；滞纳金百分比逾期加收
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_fee_rule")
public class FeeRule extends BaseEntity {

    /** 所属子公司ID，0集团模板 */
    private Long companyId;

    /** 规则名称 */
    private String ruleName;

    /** 关联收费类型ID（biz_fee_item） */
    private Long feeItemId;

    /** 收费方式 1定额 2按面积 */
    private Integer calcMode;

    /** 单价（定额=固定金额/周期；按面积=每平米单价） */
    private BigDecimal price;

    /** 收费周期 1按年 2按月 3按日 */
    private Integer periodType;

    /** 滞纳金百分比（逾期加收比例，0=不收滞纳金） */
    private BigDecimal overdueRate;

    /** 规则状态 0停用 1启用 */
    private Integer status;

    /** 规则备注 */
    private String remark;
}
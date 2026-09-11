package com.gbi.platform.engine.discount;

import java.math.BigDecimal;

/**
 * 优惠计算策略接口
 * 支持多种优惠类型的计算逻辑扩展
 *
 * @author gbi
 */
public interface DiscountCalculator {

    /**
     * 计算优惠金额
     *
     * @param originalAmount 原价金额
     * @param apply 优惠申请单
     * @return 优惠金额
     */
    BigDecimal calculate(BigDecimal originalAmount, Object apply);

    /**
     * 判断是否支持该优惠类型
     *
     * @param discountType 优惠类型
     * @return true=支持
     */
    boolean support(Integer discountType);

    /**
     * 获取优惠类型描述
     *
     * @return 类型描述
     */
    String getTypeName();
}
package com.gbi.platform.engine.discount;

import com.gbi.platform.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 优惠计算器工厂
 * 根据优惠类型返回对应的计算器实现
 *
 * @author gbi
 */
@Component
@RequiredArgsConstructor
public class DiscountCalculatorFactory {

    private final List<DiscountCalculator> calculators;

    /**
     * 获取指定优惠类型的计算器
     *
     * @param discountType 优惠类型
     * @return 计算器实例
     */
    public DiscountCalculator getCalculator(Integer discountType) {
        return calculators.stream()
                .filter(c -> c.support(discountType))
                .findFirst()
                .orElseThrow(() -> new BizException("不支持的优惠类型: " + discountType));
    }
}
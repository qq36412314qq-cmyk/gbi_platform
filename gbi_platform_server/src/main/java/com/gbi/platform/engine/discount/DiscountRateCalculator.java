package com.gbi.platform.engine.discount;

import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.entity.BizDiscountApply;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class DiscountRateCalculator implements DiscountCalculator {
    @Override
    public BigDecimal calculate(BigDecimal originalAmount, Object apply) {
        BizDiscountApply app = (BizDiscountApply) apply;
        BigDecimal rate = app.getDiscountRate();
        if (rate == null || rate.compareTo(BigDecimal.valueOf(100)) >= 0) return BigDecimal.ZERO;
        BigDecimal r = rate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return originalAmount.multiply(BigDecimal.ONE.subtract(r));
    }
    @Override public boolean support(Integer t) {
        return Integer.valueOf(CommonConst.DISCOUNT_TYPE_RATE).equals(t) || Integer.valueOf(CommonConst.DISCOUNT_TYPE_COMBO).equals(t);
    }
    @Override public String getTypeName() { return "折扣率"; }
}
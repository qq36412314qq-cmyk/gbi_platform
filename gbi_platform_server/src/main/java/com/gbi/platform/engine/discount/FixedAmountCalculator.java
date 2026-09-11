package com.gbi.platform.engine.discount;

import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.entity.BizDiscountApply;
import java.math.BigDecimal;

public class FixedAmountCalculator implements DiscountCalculator {
    @Override
    public BigDecimal calculate(BigDecimal originalAmount, Object apply) {
        BizDiscountApply app = (BizDiscountApply) apply;
        BigDecimal fixed = app.getFixedAmount();
        if (fixed == null || fixed.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        int months = app.getTotalMonths() == null ? 1 : app.getTotalMonths();
        return months <= 0 ? fixed : fixed.multiply(BigDecimal.valueOf(months));
    }
    @Override public boolean support(Integer t) { return Integer.valueOf(CommonConst.DISCOUNT_TYPE_FIXED).equals(t); }
    @Override public String getTypeName() { return "定额优惠"; }
}
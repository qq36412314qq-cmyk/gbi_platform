package com.gbi.platform.engine.discount;

import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.entity.BizDiscountApply;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class WaiveMonthsCalculator implements DiscountCalculator {
    @Override
    public BigDecimal calculate(BigDecimal originalAmount, Object apply) {
        BizDiscountApply app = (BizDiscountApply) apply;
        int waive = app.getWaiveMonths() == null ? 0 : app.getWaiveMonths();
        int total = app.getTotalMonths() == null ? 0 : app.getTotalMonths();
        if (waive <= 0 || total <= 0) return BigDecimal.ZERO;
        return originalAmount.multiply(BigDecimal.valueOf(waive))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }
    @Override public boolean support(Integer t) {
        return Integer.valueOf(CommonConst.DISCOUNT_TYPE_WAIVE).equals(t) || Integer.valueOf(CommonConst.DISCOUNT_TYPE_COMBO).equals(t);
    }
    @Override public String getTypeName() { return "免租期"; }
}
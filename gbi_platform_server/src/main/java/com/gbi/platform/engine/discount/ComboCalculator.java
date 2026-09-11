package com.gbi.platform.engine.discount;

import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.entity.BizDiscountApply;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ComboCalculator implements DiscountCalculator {
    @Override
    public BigDecimal calculate(BigDecimal originalAmount, Object apply) {
        BizDiscountApply app = (BizDiscountApply) apply;
        BigDecimal total = BigDecimal.ZERO;
        int waive = app.getWaiveMonths() == null ? 0 : app.getWaiveMonths();
        int totalM = app.getTotalMonths() == null ? 0 : app.getTotalMonths();
        if (waive > 0 && totalM > 0) {
            total = total.add(originalAmount.multiply(BigDecimal.valueOf(waive))
                    .divide(BigDecimal.valueOf(totalM), 2, RoundingMode.HALF_UP));
        }
        BigDecimal rate = app.getDiscountRate();
        if (rate != null && rate.compareTo(BigDecimal.valueOf(100)) < 0) {
            total = total.add(originalAmount.multiply(BigDecimal.ONE
                    .subtract(rate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))));
        }
        BigDecimal deduct = app.getDeductAmount();
        if (deduct != null && deduct.compareTo(BigDecimal.ZERO) > 0) {
            total = total.add(deduct);
        }
        return total.min(originalAmount);
    }
    @Override public boolean support(Integer t) { return Integer.valueOf(CommonConst.DISCOUNT_TYPE_COMBO).equals(t); }
    @Override public String getTypeName() { return "组合优惠"; }
}
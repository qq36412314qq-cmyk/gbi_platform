package com.gbi.platform.engine.discount;

import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.entity.BizDiscountApply;
import java.math.BigDecimal;

public class DeductAmountCalculator implements DiscountCalculator {
    @Override
    public BigDecimal calculate(BigDecimal originalAmount, Object apply) {
        BizDiscountApply app = (BizDiscountApply) apply;
        BigDecimal deduct = app.getDeductAmount();
        if (deduct == null || deduct.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return deduct.min(originalAmount);
    }
    @Override public boolean support(Integer t) {
        return Integer.valueOf(CommonConst.DISCOUNT_TYPE_DEDUCT).equals(t) || Integer.valueOf(CommonConst.DISCOUNT_TYPE_COMBO).equals(t);
    }
    @Override public String getTypeName() { return "减免金额"; }
}
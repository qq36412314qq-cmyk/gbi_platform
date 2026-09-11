package com.gbi.platform.engine.discount;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.entity.BizDiscountApply;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;

public class TierCalculator implements DiscountCalculator {
    private final ObjectMapper mapper = new ObjectMapper();
    @Override
    public BigDecimal calculate(BigDecimal originalAmount, Object apply) {
        BizDiscountApply app = (BizDiscountApply) apply;
        String cfg = app.getTierConfig();
        if (cfg == null || cfg.isBlank()) return BigDecimal.ZERO;
        try {
            JsonNode root = mapper.readTree(cfg);
            if (!root.isArray() || root.isEmpty()) return BigDecimal.ZERO;
            Iterator<JsonNode> it = root.elements();
            while (it.hasNext()) {
                JsonNode tier = it.next();
                BigDecimal min = tier.path("min_amount").decimalValue();
                if (originalAmount.compareTo(min) >= 0) {
                    BigDecimal rate = tier.path("discount_rate").decimalValue()
                            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                    return originalAmount.multiply(BigDecimal.ONE.subtract(rate));
                }
            }
        } catch (Exception e) { throw new BizException("阶梯配置解析失败"); }
        return BigDecimal.ZERO;
    }
    @Override public boolean support(Integer t) { return Integer.valueOf(CommonConst.DISCOUNT_TYPE_TIER).equals(t); }
    @Override public String getTypeName() { return "阶梯优惠"; }
}
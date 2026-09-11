package com.gbi.platform.service.impl;

import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.dto.DiscountCalcDTO;
import com.gbi.platform.entity.BizDiscountApply;
import com.gbi.platform.engine.discount.DiscountCalculator;
import com.gbi.platform.engine.discount.DiscountCalculatorFactory;
import com.gbi.platform.service.DiscountCalcService;
import com.gbi.platform.vo.DiscountCalcVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 优惠计算服务实现（策略模式）
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountCalcServiceImpl implements DiscountCalcService {

    private final DiscountCalculatorFactory calculatorFactory;

    @Override
    public DiscountCalcVO calc(DiscountCalcDTO dto) {
        // 构建临时申请单
        BizDiscountApply apply = new BizDiscountApply();
        apply.setBizType(dto.getBizType());
        apply.setDiscountType(dto.getDiscountType());
        apply.setWaiveMonths(dto.getWaiveMonths());
        apply.setDiscountRate(dto.getDiscountRate());
        apply.setDeductAmount(dto.getDeductAmount());
        apply.setFixedAmount(dto.getFixedAmount());
        apply.setTierConfig(dto.getTierConfig());
        apply.setTotalMonths(dto.getTotalMonths());
        apply.setOriginalAmount(dto.getOriginalAmount());
        
        // 获取计算器并计算
        DiscountCalculator calculator = calculatorFactory.getCalculator(dto.getDiscountType());
        BigDecimal discountAmount = calculator.calculate(dto.getOriginalAmount(), apply);
        
        // 构建结果
        DiscountCalcVO vo = new DiscountCalcVO();
        vo.setOriginalAmount(dto.getOriginalAmount());
        vo.setDiscountAmount(discountAmount);
        vo.setRealAmount(dto.getOriginalAmount().subtract(discountAmount).max(BigDecimal.ZERO));
        return vo;
    }
}
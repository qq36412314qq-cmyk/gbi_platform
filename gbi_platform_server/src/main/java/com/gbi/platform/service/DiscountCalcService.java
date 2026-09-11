package com.gbi.platform.service;

import com.gbi.platform.dto.DiscountCalcDTO;
import com.gbi.platform.vo.DiscountCalcVO;

/**
 * 优惠计算服务（策略模式）
 *
 * @author gbi
 */
public interface DiscountCalcService {

    /**
     * 预览计算优惠金额
     */
    DiscountCalcVO calc(DiscountCalcDTO dto);
}
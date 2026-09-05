package com.gbi.platform.service;

import com.gbi.platform.dto.PayBillCreateDTO;
import com.gbi.platform.dto.PayBillPayDTO;

/**
 * 缴费单服务（合并缴费）
 * 从多条统一账单生成一条缴费单，再执行支付
 *
 * @author gbi
 */
public interface PropertyPayBillService {

    /**
     * 创建缴费单（finance_pay_order + finance_pay_order_item）
     *
     * @param dto 创建入参
     * @return 缴费单ID
     */
    Long createPayBill(PayBillCreateDTO dto);

    /**
     * 缴费单支付（更新状态 + 写入财务流水）
     *
     * @param dto 支付入参
     */
    void pay(PayBillPayDTO dto);
}
package com.gbi.platform.service;

import com.gbi.platform.dto.UnifiedPayDTO;

/**
 * 统一缴费服务：聚合物业费/水电费缴费，提供统一入口
 *
 * @author gbi
 */
public interface UnifiedPayService {

    /**
     * 统一线下缴费（requestId 幂等，同步财务流水，更新账单状态）
     *
     * @param dto 缴费入参，包含 billId（统一账单ID）、billType（业务类型）、payType（支付渠道）、requestId（幂等ID）、remark（备注）
     */
    void pay(UnifiedPayDTO dto);
}
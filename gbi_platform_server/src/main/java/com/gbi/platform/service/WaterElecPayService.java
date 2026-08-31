package com.gbi.platform.service;

import com.gbi.platform.dto.WaterElecPayDTO;
import com.gbi.platform.dto.WaterElecPayQueryDTO;
import com.gbi.platform.dto.WaterElecRefundDTO;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.WaterElecPayRecordVO;

/**
 * 水电物业缴费服务：线下缴费/退费（幂等 + 财务流水 + 审计）
 *
 * @author gbi
 */
public interface WaterElecPayService {

    /**
     * 缴费记录分页（自动 company_id 隔离）
     */
    PageVO<WaterElecPayRecordVO> page(WaterElecPayQueryDTO dto);

    /**
     * 线下缴费：requestId 幂等，更新账单状态，写入统一财务流水
     */
    void pay(WaterElecPayDTO dto);

    /**
     * 退费：requestId 幂等，按原缴费记录退，写支出流水，账单回退待缴
     */
    void refund(WaterElecRefundDTO dto);
}

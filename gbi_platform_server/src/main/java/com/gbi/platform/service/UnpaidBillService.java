package com.gbi.platform.service;

import com.gbi.platform.dto.UnpaidBillQueryDTO;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.UnpaidBillVO;

/**
 * 未支付订单聚合服务（物业费 + 水电费）
 *
 * @author gbi
 */
public interface UnpaidBillService {

    /**
     * 分页查询未支付订单（聚合 property_bill + water_elec_bill）
     */
    PageVO<UnpaidBillVO> page(UnpaidBillQueryDTO dto);
}

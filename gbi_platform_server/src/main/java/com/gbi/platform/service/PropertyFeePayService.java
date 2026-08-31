package com.gbi.platform.service;

import com.gbi.platform.dto.PropertyFeePayDTO;

/**
 * 物业费缴费服务
 *
 * @author gbi
 */
public interface PropertyFeePayService {

    /**
     * 物业费线下缴费
     */
    void pay(PropertyFeePayDTO dto);
}

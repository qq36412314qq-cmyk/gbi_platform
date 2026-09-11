package com.gbi.platform.service;

import com.gbi.platform.dto.DiscountThresholdDTO;
import com.gbi.platform.dto.DiscountThresholdQueryDTO;
import com.gbi.platform.entity.BizDiscountThreshold;
import com.gbi.platform.vo.DiscountThresholdVO;
import com.gbi.platform.vo.PageVO;

/**
 * 优惠审批阈值配置服务
 *
 * @author gbi
 */
public interface DiscountThresholdService {

    PageVO<DiscountThresholdVO> page(DiscountThresholdQueryDTO dto);

    void add(DiscountThresholdDTO dto);

    void update(DiscountThresholdDTO dto);

    void delete(Long id);
}
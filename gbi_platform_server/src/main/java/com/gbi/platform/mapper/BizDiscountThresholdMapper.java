package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.BizDiscountThreshold;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠审批阈值配置 Mapper：finance_discount_threshold
 *
 * @author gbi
 */
@Mapper
public interface BizDiscountThresholdMapper extends BaseMapper<BizDiscountThreshold> {
}
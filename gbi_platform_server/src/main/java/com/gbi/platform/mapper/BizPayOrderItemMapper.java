package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.BizPayOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 缴费单明细 Mapper
 *
 * @author gbi
 */
@Mapper
public interface BizPayOrderItemMapper extends BaseMapper<BizPayOrderItem> {
}
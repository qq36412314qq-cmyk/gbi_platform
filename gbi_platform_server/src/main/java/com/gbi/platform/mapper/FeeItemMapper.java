package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.FeeItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 自定义收费类型 Mapper（biz_fee_item，company_id 自动隔离）
 *
 * @author gbi
 */
@Mapper
public interface FeeItemMapper extends BaseMapper<FeeItem> {
}
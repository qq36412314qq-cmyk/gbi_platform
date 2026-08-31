package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.BizFeeBill;
import org.apache.ibatis.annotations.Mapper;

/**
 * 统一收费账单 Mapper
 *
 * @author gbi
 */
@Mapper
public interface BizFeeBillMapper extends BaseMapper<BizFeeBill> {
}

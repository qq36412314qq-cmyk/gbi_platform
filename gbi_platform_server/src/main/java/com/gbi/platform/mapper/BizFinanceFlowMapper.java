package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.BizFinanceFlow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 全域财务资金流水 Mapper（禁止 update/delete，只读+新增）
 *
 * @author gbi
 */
@Mapper
public interface BizFinanceFlowMapper extends BaseMapper<BizFinanceFlow> {
}

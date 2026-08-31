package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.BizFinanceWriteoff;
import org.apache.ibatis.annotations.Mapper;

/**
 * 核销分摊明细 Mapper：biz_finance_writeoff（自动对账权威源）
 *
 * @author gbi
 */
@Mapper
public interface BizFinanceWriteoffMapper extends BaseMapper<BizFinanceWriteoff> {
}
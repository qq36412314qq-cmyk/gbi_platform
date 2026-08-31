package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.WaterElecPayRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 水电物业缴费记录 Mapper（request_id 唯一索引支撑幂等）
 *
 * @author gbi
 */
@Mapper
public interface WaterElecPayRecordMapper extends BaseMapper<WaterElecPayRecord> {
}

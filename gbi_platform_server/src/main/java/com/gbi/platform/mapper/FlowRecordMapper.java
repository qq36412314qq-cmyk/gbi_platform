package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.FlowRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程流转记录 Mapper：flow_record（全流程留痕）
 *
 * @author gbi
 */
@Mapper
public interface FlowRecordMapper extends BaseMapper<FlowRecord> {
}
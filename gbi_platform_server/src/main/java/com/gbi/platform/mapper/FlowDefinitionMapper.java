package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.FlowDefinition;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程定义 Mapper：flow_definition（集团全局模板）
 *
 * @author gbi
 */
@Mapper
public interface FlowDefinitionMapper extends BaseMapper<FlowDefinition> {
}
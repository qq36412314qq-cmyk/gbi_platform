package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.FlowTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审批任务 Mapper：flow_task（待办/已办）
 *
 * @author gbi
 */
@Mapper
public interface FlowTaskMapper extends BaseMapper<FlowTask> {
}
package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.BillPlanRel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账单-计划关联中间表 Mapper：bill_plan_rel
 *
 * @author gbi
 */
@Mapper
public interface BillPlanRelMapper extends BaseMapper<BillPlanRel> {
}
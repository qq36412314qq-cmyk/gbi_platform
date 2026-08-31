package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.BizDiscountPolicy;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠策略 Mapper：biz_discount_policy（集团模板 company_id=0）
 *
 * @author gbi
 */
@Mapper
public interface BizDiscountPolicyMapper extends BaseMapper<BizDiscountPolicy> {
}
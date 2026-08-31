package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.StallCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租赁分类 Mapper（商铺/仓库/车位等自定义分类）
 *
 * @author gbi
 */
@Mapper
public interface StallCategoryMapper extends BaseMapper<StallCategory> {
}
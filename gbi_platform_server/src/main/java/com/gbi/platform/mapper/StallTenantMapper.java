package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.StallTenant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户档案 Mapper（单表 CRUD 复用 MyBatis-Plus 内置方法）
 *
 * @author gbi
 */
@Mapper
public interface StallTenantMapper extends BaseMapper<StallTenant> {
}
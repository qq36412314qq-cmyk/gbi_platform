package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户 Mapper
 *
 * @author gbi
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}

package com.gbi.platform.mapper.hr;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.hr.SysOvertimeConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 加班配置 Mapper（sys_overtime_config 为全局配置表，MybatisPlus 多租户豁免）
 *
 * @author gbi
 */
@Mapper
public interface SysOvertimeConfigMapper extends BaseMapper<SysOvertimeConfig> {
}

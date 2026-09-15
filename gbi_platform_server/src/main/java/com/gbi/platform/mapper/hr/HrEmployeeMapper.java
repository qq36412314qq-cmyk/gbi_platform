package com.gbi.platform.mapper.hr;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.gbi.platform.entity.hr.HrEmployee;

@Mapper
public interface HrEmployeeMapper extends BaseMapper<HrEmployee> {
}


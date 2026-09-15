package com.gbi.platform.mapper.hr;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.hr.HrHousingFundConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公积金参数配置 Mapper
 */
@Mapper
public interface HrHousingFundConfigMapper extends BaseMapper<HrHousingFundConfig> {

    /**
     * 查询某城市当前生效的公积金配置
     */
    HrHousingFundConfig selectActiveByCity(@Param("cityCode") String cityCode,
                                            @Param("companyId") Long companyId);
}

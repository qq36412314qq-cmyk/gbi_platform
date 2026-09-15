package com.gbi.platform.mapper.hr;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.hr.HrSocialParamConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 社保公积金参数配置 Mapper
 */
@Mapper
public interface HrSocialParamMapper extends BaseMapper<HrSocialParamConfig> {

    /**
     * 查询当前生效的参数配置（按城市+险种+时间范围）
     */
    List<HrSocialParamConfig> selectEffectiveByCityAndInsurance(
            @Param("cityCode") String cityCode,
            @Param("insuranceCode") String insuranceCode,
            @Param("companyId") Long companyId
    );

    /**
     * 查询某城市下所有险种的有效配置
     */
    List<HrSocialParamConfig> selectActiveByCity(@Param("cityCode") String cityCode,
                                                  @Param("companyId") Long companyId);
}

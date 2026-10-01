package com.gbi.platform.mapper.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.sys.SysHolidayConfig;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 法定节假日配置Mapper接口
 *
 * @author gbi
 */
@Mapper
public interface SysHolidayConfigMapper extends BaseMapper<SysHolidayConfig> {

    /**
     * 查询指定日期范围内的节假日配置
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param companyId 公司ID
     * @return 节假日配置列表
     */
    List<SysHolidayConfig> selectByDateRange(LocalDate startDate, LocalDate endDate, Long companyId);
}

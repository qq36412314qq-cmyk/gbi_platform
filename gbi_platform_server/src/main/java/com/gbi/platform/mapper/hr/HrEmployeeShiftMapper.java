package com.gbi.platform.mapper.hr;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.hr.HrEmployeeShift;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 员工班次配置Mapper接口
 *
 * @author gbi
 */
@Mapper
public interface HrEmployeeShiftMapper extends BaseMapper<HrEmployeeShift> {

    /**
     * 查询指定日期范围内生效的班次配置
     *
     * @param employeeId 员工ID
     * @param date       目标日期
     * @return 生效的班次配置列表
     */
    List<HrEmployeeShift> selectActiveShifts(Long employeeId, LocalDate date);
}

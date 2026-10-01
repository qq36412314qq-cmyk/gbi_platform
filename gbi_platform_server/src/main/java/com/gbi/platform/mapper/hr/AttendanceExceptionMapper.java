package com.gbi.platform.mapper.hr;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.hr.AttendanceException;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤异常记录Mapper接口
 *
 * @author gbi
 */
@Mapper
public interface AttendanceExceptionMapper extends BaseMapper<AttendanceException> {

    /**
     * 查询连续缺卡超阈值的员工列表
     *
     * @param companyId      公司ID
     * @param monthStart     月份起始日期
     * @param monthEnd       月份结束日期
     * @param absentThreshold 连续缺卡阈值（天）
     * @return 员工ID列表
     */
    List<Long> selectAbsentEmployees(Long companyId, LocalDate monthStart, LocalDate monthEnd, int absentThreshold);

    /**
     * 查询月度迟到频繁的员工列表
     *
     * @param companyId            公司ID
     * @param monthStart           月份起始日期
     * @param monthEnd             月份结束日期
     * @param lateFrequentThreshold 月度迟到频繁阈值（次）
     * @return 员工ID列表
     */
    List<Long> selectLateFrequentEmployees(Long companyId, LocalDate monthStart, LocalDate monthEnd, int lateFrequentThreshold);
}

package com.gbi.platform.mapper.hr;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.hr.HrAttendanceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HrAttendanceRecordMapper extends BaseMapper<HrAttendanceRecord> {

    // 分页查询：由Service层使用LambdaQueryWrapper构建，避免@Select注解中动态SQL与租户插件冲突

    /** 按员工+月份聚合考勤数据，供薪资核算调用 */
    @Select("SELECT " +
            "employee_id, " +
            "SUM(COALESCE(absent,0)) AS total_absent, " +
            "SUM(COALESCE(late_minutes,0)) AS total_late_minutes, " +
            "SUM(COALESCE(early_minutes,0)) AS total_early_minutes, " +
            "COALESCE(SUM(CASE WHEN leave_type = 0 THEN CAST(leave_days AS DECIMAL(10,2)) ELSE 0 END),0) AS unpaid_leave_days " +
            "FROM hr_attendance_record " +
            "WHERE company_id = #{companyId} AND attendance_month = #{attendanceMonth} AND is_delete = 0 " +
            "GROUP BY employee_id")
    java.util.List<java.util.Map<String, Object>> selectMonthSummary(
            @Param("companyId") Long companyId, @Param("attendanceMonth") String attendanceMonth);
}

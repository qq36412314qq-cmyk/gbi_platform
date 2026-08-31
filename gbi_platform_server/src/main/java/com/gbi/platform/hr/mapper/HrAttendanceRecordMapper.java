package com.gbi.platform.hr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.hr.entity.HrAttendanceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HrAttendanceRecordMapper extends BaseMapper<HrAttendanceRecord> {

    @Select("SELECT id, company_id, employee_id, employee_name, attendance_month, attendance_day, clock_in_time, clock_out_time, clock_type, late_minutes, early_minutes, absent, leave_days, work_days, actual_days, remark FROM hr_attendance_record WHERE company_id = #{companyId} AND is_delete = 0 <if test='employeeId != null'>AND employee_id = #{employeeId}</if> <if test='attendanceMonth != null'>AND attendance_month = #{attendanceMonth}</if> ORDER BY attendance_day DESC LIMIT #{offset}, #{pageSize}")
    List<HrAttendanceRecord> selectPage(@Param("companyId") Long companyId, @Param("employeeId") Long employeeId, @Param("attendanceMonth") String attendanceMonth, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM hr_attendance_record WHERE company_id = #{companyId} AND is_delete = 0 <if test='employeeId != null'>AND employee_id = #{employeeId}</if> <if test='attendanceMonth != null'>AND attendance_month = #{attendanceMonth}</if>")
    long countByCondition(@Param("companyId") Long companyId, @Param("employeeId") Long employeeId, @Param("attendanceMonth") String attendanceMonth);
}

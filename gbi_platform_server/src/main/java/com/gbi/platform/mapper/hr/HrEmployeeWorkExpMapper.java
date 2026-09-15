package com.gbi.platform.mapper.hr;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.hr.HrEmployeeWorkExp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 员工工作经历Mapper
 *
 * @author gbi
 */
@Mapper
public interface HrEmployeeWorkExpMapper extends BaseMapper<HrEmployeeWorkExp> {

    /**
     * 按员工ID批量查询工作经历（带多租户隔离）
     */
    @Select("SELECT * FROM hr_employee_work_exp " +
            "WHERE company_id = #{companyId} AND employee_id = #{employeeId} AND is_delete = 0 " +
            "ORDER BY start_date DESC")
    List<HrEmployeeWorkExp> selectByEmployee(@Param("companyId") Long companyId,
                                              @Param("employeeId") Long employeeId);
}

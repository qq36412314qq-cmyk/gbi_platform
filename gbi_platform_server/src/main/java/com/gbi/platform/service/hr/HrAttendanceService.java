package com.gbi.platform.service.hr;

import com.gbi.platform.dto.hr.*;
import com.gbi.platform.vo.hr.*;
import com.gbi.platform.vo.PageVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 考勤管理服务接口
 *
 * 支持多种休息日类型、法定节假日、班次管理、免考勤员工、异常检测、报表生成等功能。
 * 数据表：hr_attendance_record、sys_workweek_config、sys_holiday_config、hr_employee_shift、hr_attendance_exception
 *
 * @author gbi
 */
public interface HrAttendanceService {

    /** 分页查询考勤记录 */
    PageVO<HrAttendanceVO> pageAttendance(AttendanceQueryDTO dto);

    /** 同步考勤（保留原有简单逻辑，兼容旧调用） */
    int syncMonthly(String attendanceMonth);

    /** 导出考勤记录 */
    List<HrAttendanceVO> exportAttendance(Long employeeId, String attendanceMonth);

    /**
     * 同步预览：不写入数据库，仅计算并返回预览结果
     *
     * @param month         考勤月份 yyyy-MM
     * @param employeeScope 员工范围 0=全部 1=指定
     * @param employeeIds   指定员工ID列表
     * @param syncType      同步类型 1=首次同步 2=重新同步
     * @return 预览统计信息
     */
    Map<String, Object> syncPreview(String month, Integer employeeScope, List<Long> employeeIds, Integer syncType);

    /**
     * 执行考勤同步：删除当月旧记录后重新生成，支持免考勤/班次/节假日处理
     *
     * @param month         考勤月份 yyyy-MM
     * @param employeeScope 员工范围 0=全部 1=指定
     * @param employeeIds   指定员工ID列表
     * @param syncType      同步类型 1=首次同步 2=重新同步
     * @return 新增记录总数
     */
    int syncMonthlyAdvanced(String month, Integer employeeScope, List<Long> employeeIds, Integer syncType);

    /** 分页查询考勤异常记录 */
    PageVO<AttendanceExceptionVO> pageExceptions(Integer pageNum, Integer pageSize, Long employeeId, Integer exceptionType, Integer status);

    /** 处理考勤异常（确认/豁免/忽略） */
    void handleException(Long id, Integer handleType, String handleRemark, Long handlerId);

    /** 获取考勤配置参数列表 */
    Map<String, String> getAttendanceConfigs();

    /** 批量更新考勤配置参数 */
    void updateAttendanceConfigs(Map<String, String> configs);

    /** 分页查询休息日配置 */
    PageVO<SysWorkweekConfigVO> pageWorkweekConfigs(Integer pageNum, Integer pageSize, Long companyId);

    /** 查询休息日配置列表 */
    List<SysWorkweekConfigVO> listWorkweekConfigs();

    /** 新增休息日配置 */
    Long addWorkweekConfig(SysWorkweekConfigDTO dto);

    /** 更新休息日配置 */
    void updateWorkweekConfig(SysWorkweekConfigDTO dto);

    /** 删除休息日配置 */
    void deleteWorkweekConfig(Long id);

    /** 设为默认休息日配置 */
    void setDefaultWorkweekConfig(Long id);

    /** 分页查询节假日配置 */
    PageVO<SysHolidayConfigVO> pageHolidayConfigs(Integer pageNum, Integer pageSize, Long companyId, Integer year, Integer holidayType);

    /** 查询节假日配置列表 */
    List<SysHolidayConfigVO> listHolidayConfigs(Integer year);

    /** 批量导入节假日配置 */
    void batchImportHolidays(List<SysHolidayConfigDTO> holidayList);

    /** 查询员工当日班次配置 */
    ShiftConfigVO getEmployeeShift(Long employeeId, LocalDate date);

    /** 查询员工班次配置列表 */
    List<HrEmployeeShiftVO> listEmployeeShifts(Long employeeId);

    /** 新增员工班次配置 */
    void addEmployeeShift(HrEmployeeShiftDTO dto);

    /** 更新员工班次配置 */
    void updateEmployeeShift(HrEmployeeShiftDTO dto);

    /** 删除员工班次配置 */
    void deleteEmployeeShift(Long id);

    /** 手动触发异常检测 */
    void detectExceptions(String month);
}
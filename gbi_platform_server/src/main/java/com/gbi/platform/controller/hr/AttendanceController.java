package com.gbi.platform.controller.hr;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.hr.*;
import com.gbi.platform.entity.sys.SysHolidayConfig;
import com.gbi.platform.mapper.sys.SysHolidayConfigMapper;
import com.gbi.platform.service.hr.HrAttendanceService;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.hr.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 考勤管理控制器
 * 提供考勤同步、异常管理、休息日/节假日/班次配置、参数配置等REST接口
 *
 * @author gbi
 */
@Tag(name = "人力资源-考勤管理")
@RestController
@RequestMapping("/hr/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final HrAttendanceService attendanceService;
    private final SysHolidayConfigMapper holidayConfigMapper;

    // ==================== 考勤记录 ====================

    @Operation(summary = "考勤分页查询")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_ATTENDANCE_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<HrAttendanceVO>> page(AttendanceQueryDTO dto) {
        return Result.success(attendanceService.pageAttendance(dto));
    }

    @Operation(summary = "同步考勤（旧接口，兼容调用）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_ATTENDANCE_SYNC,'')")
    @PostMapping("/sync")
    public Result<Integer> sync(@RequestParam(required = false) String attendanceMonth) {
        return Result.success(attendanceService.syncMonthly(attendanceMonth));
    }

    @Operation(summary = "导出考勤记录")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_ATTENDANCE_EXPORT,'')")
    @GetMapping("/export")
    public Result<List<HrAttendanceVO>> export(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String attendanceMonth) {
        return Result.success(attendanceService.exportAttendance(employeeId, attendanceMonth));
    }

    @Operation(summary = "同步预览（不写入数据）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_ATTENDANCE_PREVIEW,'')")
    @PostMapping("/sync/preview")
    public Result<Map<String, Object>> syncPreview(@RequestBody AttendancePreviewDTO dto) {
        return Result.success(attendanceService.syncPreview(dto.getMonth(), dto.getEmployeeScope(), dto.getEmployeeIds(), dto.getSyncType()));
    }

    @Operation(summary = "执行同步打卡")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_ATTENDANCE_SYNC,'')")
    @PostMapping("/sync/advanced")
    public Result<Integer> syncAdvanced(@RequestBody AttendancePreviewDTO dto) {
        return Result.success(attendanceService.syncMonthlyAdvanced(dto.getMonth(), dto.getEmployeeScope(), dto.getEmployeeIds(), dto.getSyncType()));
    }

    // ==================== 考勤异常 ====================

    @Operation(summary = "分页查询考勤异常")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_ATTENDANCE_EXCEPTION_VIEW,'')")
    @GetMapping("/exception/page")
    public Result<PageVO<AttendanceExceptionVO>> pageExceptions(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer exceptionType,
            @RequestParam(required = false) Integer status) {
        return Result.success(attendanceService.pageExceptions(pageNum, pageSize, employeeId, exceptionType, status));
    }

    @Operation(summary = "处理考勤异常")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_ATTENDANCE_EXCEPTION_HANDLE,'')")
    @PutMapping("/exception/handle")
    public Result<Void> handleException(
            @RequestParam Long id,
            @RequestParam Integer handleType,
            @RequestParam(required = false) String handleRemark,
            @RequestParam Long handlerId) {
        attendanceService.handleException(id, handleType, handleRemark, handlerId);
        return Result.success();
    }

    @Operation(summary = "手动触发异常检测")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_ATTENDANCE_SYNC,'')")
    @GetMapping("/exception/detect")
    public Result<Void> detectExceptions(@RequestParam String month) {
        attendanceService.detectExceptions(month);
        return Result.success();
    }

    // ==================== 参数配置 ====================

    @Operation(summary = "获取考勤配置参数")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CONFIG_ATTENDANCE,'')")
    @GetMapping("/config/params")
    public Result<Map<String, String>> getAttendanceConfigs() {
        return Result.success(attendanceService.getAttendanceConfigs());
    }

    @Operation(summary = "批量更新考勤配置参数")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CONFIG_ATTENDANCE,'')")
    @PutMapping("/config/params")
    public Result<Void> updateAttendanceConfigs(@RequestBody Map<String, String> configs) {
        attendanceService.updateAttendanceConfigs(configs);
        return Result.success();
    }

    // ==================== 休息日配置 ====================

    @Operation(summary = "分页查询休息日配置")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CONFIG_WORKWEEK,'')")
    @GetMapping("/workweek/page")
    public Result<PageVO<SysWorkweekConfigVO>> pageWorkweekConfigs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long companyId) {
        return Result.success(attendanceService.pageWorkweekConfigs(pageNum, pageSize, companyId));
    }

    @Operation(summary = "查询休息日配置列表（不分页）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CONFIG_WORKWEEK,'')")
    @GetMapping("/workweek/list")
    public Result<List<SysWorkweekConfigVO>> listWorkweekConfigs() {
        return Result.success(attendanceService.listWorkweekConfigs());
    }

    @Operation(summary = "新增休息日配置")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CONFIG_WORKWEEK,'')")
    @PostMapping("/workweek/add")
    public Result<Long> addWorkweekConfig(@RequestBody SysWorkweekConfigDTO dto) {
        return Result.success(attendanceService.addWorkweekConfig(dto));
    }

    @Operation(summary = "更新休息日配置")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CONFIG_WORKWEEK,'')")
    @PutMapping("/workweek/update")
    public Result<Void> updateWorkweekConfig(@RequestBody SysWorkweekConfigDTO dto) {
        attendanceService.updateWorkweekConfig(dto);
        return Result.success();
    }

    @Operation(summary = "删除休息日配置")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CONFIG_WORKWEEK,'')")
    @PostMapping("/workweek/delete/{id}")
    public Result<Void> deleteWorkweekConfig(@PathVariable Long id) {
        attendanceService.deleteWorkweekConfig(id);
        return Result.success();
    }

    @Operation(summary = "设为默认休息日配置")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CONFIG_WORKWEEK,'')")
    @PostMapping("/workweek/default/{id}")
    public Result<Void> setDefaultWorkweekConfig(@PathVariable Long id) {
        attendanceService.setDefaultWorkweekConfig(id);
        return Result.success();
    }

    // ==================== 节假日配置 ====================

    @Operation(summary = "分页查询节假日配置")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CONFIG_HOLIDAY,'')")
    @GetMapping("/holiday/page")
    public Result<PageVO<SysHolidayConfigVO>> pageHolidayConfigs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer holidayType) {
        return Result.success(attendanceService.pageHolidayConfigs(pageNum, pageSize, companyId, year, holidayType));
    }

    @Operation(summary = "查询节假日配置列表（按年份）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CONFIG_HOLIDAY,'')")
    @GetMapping("/holiday/list")
    public Result<List<SysHolidayConfigVO>> listHolidayConfigs(@RequestParam(required = false) Integer year) {
        return Result.success(attendanceService.listHolidayConfigs(year));
    }

    @Operation(summary = "批量导入节假日配置")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CONFIG_HOLIDAY,'')")
    @PostMapping("/holiday/batch")
    public Result<Void> batchImportHolidays(@RequestBody List<SysHolidayConfigDTO> holidayList) {
        attendanceService.batchImportHolidays(holidayList);
        return Result.success();
    }

    @Operation(summary = "新增节假日配置")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CONFIG_HOLIDAY,'')")
    @PostMapping("/holiday/add")
    public Result<Long> addHolidayConfig(@RequestBody SysHolidayConfigDTO dto) {
        attendanceService.batchImportHolidays(List.of(dto));
        return Result.success(null);
    }

    @Operation(summary = "更新节假日配置")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CONFIG_HOLIDAY,'')")
    @PutMapping("/holiday/update")
    public Result<Void> updateHolidayConfig(@RequestBody SysHolidayConfigDTO dto) {
        if (dto.getId() == null) throw new BizException("缺少节假日ID");
        attendanceService.batchImportHolidays(List.of(dto));
        return Result.success();
    }

    @Operation(summary = "删除节假日配置")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CONFIG_HOLIDAY,'')")
    @PostMapping("/holiday/delete/{id}")
    public Result<Void> deleteHolidayConfig(@PathVariable Long id) {
        SysHolidayConfig existing = holidayConfigMapper.selectById(id);
        if (existing == null) throw new BizException("节假日配置不存在");
        holidayConfigMapper.deleteById(id);
        return Result.success();
    }

    // ==================== 员工班次配置 ====================

    @Operation(summary = "查询员工班次列表")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CONFIG_ATTENDANCE,'')")
    @GetMapping("/shift/list/{employeeId}")
    public Result<List<HrEmployeeShiftVO>> listEmployeeShifts(@PathVariable Long employeeId) {
        return Result.success(attendanceService.listEmployeeShifts(employeeId));
    }

    @Operation(summary = "新增员工班次配置")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CONFIG_ATTENDANCE,'')")
    @PostMapping("/shift/add")
    public Result<Void> addEmployeeShift(@RequestBody HrEmployeeShiftDTO dto) {
        attendanceService.addEmployeeShift(dto);
        return Result.success();
    }

    @Operation(summary = "更新员工班次配置")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CONFIG_ATTENDANCE,'')")
    @PutMapping("/shift/update")
    public Result<Void> updateEmployeeShift(@RequestBody HrEmployeeShiftDTO dto) {
        attendanceService.updateEmployeeShift(dto);
        return Result.success();
    }

    @Operation(summary = "删除员工班次配置")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CONFIG_ATTENDANCE,'')")
    @PostMapping("/shift/delete/{id}")
    public Result<Void> deleteEmployeeShift(@PathVariable Long id) {
        attendanceService.deleteEmployeeShift(id);
        return Result.success();
    }
}

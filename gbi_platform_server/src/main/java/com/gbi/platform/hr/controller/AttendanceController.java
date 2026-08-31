package com.gbi.platform.hr.controller;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.hr.dto.AttendanceQueryDTO;
import com.gbi.platform.hr.service.HrAttendanceService;
import com.gbi.platform.hr.vo.HrAttendanceVO;
import com.gbi.platform.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "人力资源-考勤管理")
@RestController
@RequestMapping("/hr/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final HrAttendanceService attendanceService;

    @Operation(summary = "考勤分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_ATTENDANCE_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<HrAttendanceVO>> page(AttendanceQueryDTO dto) {
        return Result.success(attendanceService.pageAttendance(dto));
    }

    @Operation(summary = "同步考勤")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_ATTENDANCE_SYNC,'')")
    @PostMapping("/sync")
    public Result<Integer> sync(@RequestParam(required = false) String attendanceMonth) {
        return Result.success(attendanceService.syncMonthly(attendanceMonth));
    }

    @Operation(summary = "导出考勤")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_ATTENDANCE_EXPORT,'')")
    @PostMapping("/export")
    public Result<List<HrAttendanceVO>> export(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String attendanceMonth) {
        return Result.success(attendanceService.exportAttendance(employeeId, attendanceMonth));
    }
}

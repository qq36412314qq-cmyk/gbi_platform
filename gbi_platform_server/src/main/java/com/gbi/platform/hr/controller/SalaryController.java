package com.gbi.platform.hr.controller;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.hr.dto.*;
import com.gbi.platform.hr.service.HrSalaryService;
import com.gbi.platform.hr.vo.*;
import com.gbi.platform.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "人力资源-薪酬管理")
@RestController
@RequestMapping("/hr/salary")
@RequiredArgsConstructor
public class SalaryController {

    private final HrSalaryService salaryService;

    @Operation(summary = "薪资档案分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SALARY_ARCHIVE_LIST,'')")
    @GetMapping("/archive/page")
    public Result<PageVO<HrSalaryArchiveVO>> pageArchive(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) Long employeeId) {
        return Result.success(salaryService.pageArchive(pageNum, pageSize, employeeId));
    }

    @Operation(summary = "新增薪资档案")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SALARY_ARCHIVE_ADD,'')")
    @PostMapping("/archive/add")
    public Result<Void> addArchive(@RequestBody SalaryArchiveDTO dto) {
        salaryService.addArchive(dto);
        return Result.success();
    }

    @Operation(summary = "编辑薪资档案")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SALARY_ARCHIVE_EDIT,'')")
    @PostMapping("/archive/update")
    public Result<Void> updateArchive(@RequestBody SalaryArchiveDTO dto) {
        salaryService.updateArchive(dto);
        return Result.success();
    }

    @Operation(summary = "删除薪资档案")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SALARY_ARCHIVE_LIST,'')")
    @PostMapping("/archive/delete/{id}")
    public Result<Void> deleteArchive(@PathVariable Long id) {
        salaryService.deleteArchive(id);
        return Result.success();
    }

    @Operation(summary = "月度薪资分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SALARY_MONTH_LIST,'')")
    @GetMapping("/month/page")
    public Result<PageVO<HrSalaryMonthVO>> pageMonth(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String salaryMonth) {
        return Result.success(salaryService.pageMonth(pageNum, pageSize, employeeId, salaryMonth));
    }

    @Operation(summary = "生成月度薪资")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SALARY_MONTH_GENERATE,'')")
    @PostMapping("/month/generate")
    public Result<Void> generateMonth(@RequestBody SalaryMonthDTO dto) {
        salaryService.generateMonth(dto);
        return Result.success();
    }

    @Operation(summary = "发放薪资")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SALARY_MONTH_PAY,'')")
    @PostMapping("/month/pay/{id}")
    public Result<Void> payMonth(@PathVariable Long id) {
        salaryService.payMonth(id);
        return Result.success();
    }

    @Operation(summary = "导出月度薪资")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SALARY_MONTH_EXPORT,'')")
    @PostMapping("/month/export")
    public Result<List<HrSalaryMonthVO>> exportMonth(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String salaryMonth) {
        return Result.success(salaryService.exportMonth(employeeId, salaryMonth));
    }
}

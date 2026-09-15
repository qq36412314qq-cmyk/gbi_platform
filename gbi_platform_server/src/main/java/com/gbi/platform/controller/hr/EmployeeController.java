package com.gbi.platform.controller.hr;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.hr.EmployeeDTO;
import com.gbi.platform.dto.hr.EduExpDTO;
import com.gbi.platform.dto.hr.WorkExpDTO;
import com.gbi.platform.service.hr.HrEmployeeService;
import com.gbi.platform.vo.hr.EduExpVO;
import com.gbi.platform.vo.hr.HrEmployeeVO;
import com.gbi.platform.vo.hr.WorkExpVO;
import com.gbi.platform.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "人力资源-员工档案")
@RestController
@RequestMapping("/hr/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final HrEmployeeService employeeService;

    @Operation(summary = "员工分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_EMPLOYEE_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<HrEmployeeVO>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) Integer employeeStatus) {
        return Result.success(employeeService.page(pageNum, pageSize, name, employeeNo, employeeStatus));
    }

    @Operation(summary = "员工详情")
    @GetMapping("/{id}")
    public Result<HrEmployeeVO> get(@PathVariable Long id) {
        return Result.success(employeeService.get(id));
    }

    @Operation(summary = "新增员工")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_EMPLOYEE_ADD,'')")
    @PostMapping("/add")
    public Result<Long> add(@RequestBody EmployeeDTO dto) {
        return Result.success(employeeService.add(dto));
    }

    @Operation(summary = "编辑员工")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_EMPLOYEE_EDIT,'')")
    @PostMapping("/update")
    public Result<Void> update(@RequestBody EmployeeDTO dto) {
        employeeService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除员工")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_EMPLOYEE_DELETE,'')")
    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return Result.success();
    }

    @Operation(summary = "导出员工")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_EMPLOYEE_EXPORT,'')")
    @PostMapping("/export")
    public Result<List<HrEmployeeVO>> export(@RequestBody List<Long> ids) {
        return Result.success(employeeService.export(ids));
    }

    // ==================== 工作经历 ====================

    @Operation(summary = "查询工作经历列表")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_WORK_EXP_LIST,'')")
    @GetMapping("/{employeeId}/workExps")
    public Result<List<WorkExpVO>> getWorkExps(@PathVariable Long employeeId) {
        return Result.success(employeeService.getWorkExps(employeeId));
    }

    @Operation(summary = "新增工作经历")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_WORK_EXP_ADD,'')")
    @PostMapping("/workExp/add")
    public Result<Void> addWorkExp(@RequestBody WorkExpDTO dto) {
        employeeService.addWorkExp(dto);
        return Result.success();
    }

    @Operation(summary = "修改工作经历")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_WORK_EXP_EDIT,'')")
    @PostMapping("/workExp/update")
    public Result<Void> updateWorkExp(@RequestBody WorkExpDTO dto) {
        employeeService.updateWorkExp(dto);
        return Result.success();
    }

    @Operation(summary = "删除工作经历")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_WORK_EXP_DELETE,'')")
    @PostMapping("/workExp/delete/{id}")
    public Result<Void> deleteWorkExp(@PathVariable Long id) {
        employeeService.deleteWorkExp(id);
        return Result.success();
    }

    // ==================== 学业经历 ====================

    @Operation(summary = "查询学业经历列表")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_EDU_EXP_LIST,'')")
    @GetMapping("/{employeeId}/eduExps")
    public Result<List<EduExpVO>> getEduExps(@PathVariable Long employeeId) {
        return Result.success(employeeService.getEduExps(employeeId));
    }

    @Operation(summary = "新增学业经历")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_EDU_EXP_ADD,'')")
    @PostMapping("/eduExp/add")
    public Result<Void> addEduExp(@RequestBody EduExpDTO dto) {
        employeeService.addEduExp(dto);
        return Result.success();
    }

    @Operation(summary = "修改学业经历")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_EDU_EXP_EDIT,'')")
    @PostMapping("/eduExp/update")
    public Result<Void> updateEduExp(@RequestBody EduExpDTO dto) {
        employeeService.updateEduExp(dto);
        return Result.success();
    }

    @Operation(summary = "删除学业经历")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_EDU_EXP_DELETE,'')")
    @PostMapping("/eduExp/delete/{id}")
    public Result<Void> deleteEduExp(@PathVariable Long id) {
        employeeService.deleteEduExp(id);
        return Result.success();
    }
}

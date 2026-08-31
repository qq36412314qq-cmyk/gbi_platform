package com.gbi.platform.hr.controller;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.hr.dto.EmployeeDTO;
import com.gbi.platform.hr.service.HrEmployeeService;
import com.gbi.platform.hr.vo.HrEmployeeVO;
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
}

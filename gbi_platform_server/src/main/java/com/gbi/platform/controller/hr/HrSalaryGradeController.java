package com.gbi.platform.controller.hr;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.entity.hr.HrSalaryGrade;
import com.gbi.platform.service.hr.HrSalaryGradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "HR-薪酬级别管理")
@RestController
@RequestMapping("/hr/salary/grade")
@RequiredArgsConstructor
public class HrSalaryGradeController {

    private final HrSalaryGradeService salaryGradeService;

    @Operation(summary = "分页查询薪酬级别")
    @GetMapping("/page")
    public Result<IPage<HrSalaryGrade>> page(@RequestParam(defaultValue = "") String keyword,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Long companyId = com.gbi.platform.common.security.UserContext.getLoginUser().getCompanyId();
        return Result.success(salaryGradeService.pageByCompany(companyId, keyword, pageNum, pageSize));
    }

    @Operation(summary = "列表查询（全部启用）")
    @GetMapping("/list")
    public Result<List<HrSalaryGrade>> list() {
        Long companyId = com.gbi.platform.common.security.UserContext.getLoginUser().getCompanyId();
        return Result.success(salaryGradeService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HrSalaryGrade>()
                .eq(HrSalaryGrade::getCompanyId, companyId)
                .eq(HrSalaryGrade::getStatus, 1)
                .eq(HrSalaryGrade::getIsDelete, 0)
                .orderByAsc(HrSalaryGrade::getGradeLevel)));
    }

    @Operation(summary = "新增薪酬级别")
    @PostMapping("/add")
    public Result<Long> add(@RequestBody HrSalaryGrade dto) {
        Long companyId = com.gbi.platform.common.security.UserContext.getLoginUser().getCompanyId();
        dto.setCompanyId(companyId);
        dto.setStatus(1);
        salaryGradeService.save(dto);
        return Result.success("新增成功", dto.getId());
    }

    @Operation(summary = "修改薪酬级别")
    @PostMapping("/update")
    public Result<Void> update(@RequestBody HrSalaryGrade dto) {
        salaryGradeService.updateById(dto);
        return Result.success("修改成功", null);
    }

    @Operation(summary = "停用薪酬级别")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SALARY_GRADE_DISABLE,'')")
    @PostMapping("/disable/{id}")
    public Result<Void> disable(@PathVariable Long id) {
        HrSalaryGrade grade = salaryGradeService.getById(id);
        grade.setStatus(0);
        salaryGradeService.updateById(grade);
        return Result.success("停用成功", null);
    }
}

package com.gbi.platform.controller.hr;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.entity.hr.HrSalaryRule;
import com.gbi.platform.service.hr.HrSalaryRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "HR-薪资模板管理")
@RestController
@RequestMapping("/hr/salary/rule")
@RequiredArgsConstructor
public class HrSalaryRuleController {

    private final HrSalaryRuleService salaryRuleService;

    @Operation(summary = "分页查询薪资模板")
    @GetMapping("/page")
    public Result<IPage<HrSalaryRule>> page(@RequestParam(required = false) Integer bindType,
                                            @RequestParam(required = false) Long postId,
                                            @RequestParam(required = false) String gradeCode,
                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        return Result.success(salaryRuleService.pageByCompany(companyId, bindType, postId, gradeCode, pageNum, pageSize));
    }

    @Operation(summary = "新增薪资模板")
    @PostMapping("/add")
    public Result<Long> add(@RequestBody HrSalaryRule dto) {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        dto.setCompanyId(companyId);
        dto.setStatus(1);
        dto.setApplyStatus(1);
        salaryRuleService.save(dto);
        return Result.success("新增成功", dto.getId());
    }

    @Operation(summary = "修改薪资模板")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SALARY_RULE_EDIT,'')")
    @PostMapping("/update")
    public Result<Void> update(@RequestBody HrSalaryRule dto) {
        salaryRuleService.updateById(dto);
        return Result.success("修改成功", null);
    }

    @Operation(summary = "停用薪资模板")
    @PostMapping("/disable/{id}")
    public Result<Void> disable(@PathVariable Long id) {
        HrSalaryRule rule = salaryRuleService.getById(id);
        rule.setStatus(0);
        salaryRuleService.updateById(rule);
        return Result.success("停用成功", null);
    }

    @Operation(summary = "提交模板变更去审批")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SALARY_RULE_SUBMIT,'')")
    @PostMapping("/submitAudit/{id}")
    public Result<Void> submitAudit(@PathVariable Long id) {
        salaryRuleService.submitAudit(id);
        return Result.success("已提交审批", null);
    }
}

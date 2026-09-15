package com.gbi.platform.controller.hr;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.entity.hr.HrIndustry;
import com.gbi.platform.service.hr.HrIndustryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 行业字典管理接口
 */
@Tag(name = "HR-行业字典管理")
@RestController
@RequestMapping("/hr/industry")
@RequiredArgsConstructor
public class HrIndustryController {

    private final HrIndustryService industryService;

    @Operation(summary = "分页查询行业列表")
    @GetMapping("/page")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_INDUSTRY_LIST,'')")
    public Result<IPage<HrIndustry>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(industryService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize)));
    }

    @Operation(summary = "新增行业")
    @PostMapping
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_INDUSTRY_ADD,'')")
    public Result<Void> add(@RequestBody HrIndustry industry) {
        industryService.save(industry);
        return Result.success();
    }

    @Operation(summary = "编辑行业")
    @PutMapping
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_INDUSTRY_EDIT,'')")
    public Result<Void> update(@RequestBody HrIndustry industry) {
        industryService.updateById(industry);
        return Result.success();
    }

    @Operation(summary = "删除行业")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_INDUSTRY_DELETE,'')")
    public Result<Void> delete(@PathVariable Long id) {
        industryService.removeById(id);
        return Result.success();
    }
}


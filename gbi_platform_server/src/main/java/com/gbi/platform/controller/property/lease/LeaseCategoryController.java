package com.gbi.platform.controller.property.lease;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.CategoryDTO;
import com.gbi.platform.dto.CategoryQueryDTO;
import com.gbi.platform.service.LeaseCategoryService;
import com.gbi.platform.vo.CategoryVO;
import com.gbi.platform.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 租赁分类接口（商铺/仓库/车位等，子公司可自定义，前端 api/lease.ts category 部分）
 *
 * @author gbi
 */
@Tag(name = "物业管理-租赁分类")
@RestController
@RequestMapping("/property/lease/category")
@RequiredArgsConstructor
public class LeaseCategoryController {

    private final LeaseCategoryService leaseCategoryService;

    @Operation(summary = "分类全量列表（启用优先，供下拉选择）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).LEASE_CATEGORY_LIST,'')")
    @GetMapping("/list")
    public Result<List<CategoryVO>> list(CategoryQueryDTO dto) {
        return Result.success(leaseCategoryService.list(dto));
    }

    @Operation(summary = "分类分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).LEASE_CATEGORY_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<CategoryVO>> page(@Valid CategoryQueryDTO dto) {
        return Result.success(leaseCategoryService.page(dto));
    }

    @Operation(summary = "新增分类")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).LEASE_CATEGORY_ADD,'')")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody CategoryDTO dto) {
        leaseCategoryService.add(dto);
        return Result.success();
    }

    @Operation(summary = "编辑分类")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).LEASE_CATEGORY_EDIT,'')")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody CategoryDTO dto) {
        leaseCategoryService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除分类（被铺位引用禁止删除，逻辑删除）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).LEASE_CATEGORY_DELETE,'')")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        leaseCategoryService.delete(id);
        return Result.success();
    }
}
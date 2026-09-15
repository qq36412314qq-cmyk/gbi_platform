package com.gbi.platform.controller.hr;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.entity.hr.HrCity;
import com.gbi.platform.service.hr.HrCityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 城市字典管理接口
 */
@Tag(name = "HR-城市字典管理")
@RestController
@RequestMapping("/hr/city")
@RequiredArgsConstructor
public class HrCityController {

    private final HrCityService cityService;

    @Operation(summary = "分页查询城市列表")
    @GetMapping("/page")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CITY_LIST,'')")
    public Result<IPage<HrCity>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(cityService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize)));
    }

    @Operation(summary = "新增城市")
    @PostMapping
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CITY_ADD,'')")
    public Result<Void> add(@RequestBody HrCity city) {
        cityService.save(city);
        return Result.success();
    }

    @Operation(summary = "编辑城市")
    @PutMapping
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CITY_EDIT,'')")
    public Result<Void> update(@RequestBody HrCity city) {
        cityService.updateById(city);
        return Result.success();
    }

    @Operation(summary = "删除城市")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_CITY_DELETE,'')")
    public Result<Void> delete(@PathVariable Long id) {
        cityService.removeById(id);
        return Result.success();
    }
}


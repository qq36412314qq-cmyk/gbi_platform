package com.gbi.platform.controller.hr;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.entity.hr.HrInsuranceType;
import com.gbi.platform.service.hr.HrInsuranceTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 险种字典管理接口
 */
@Tag(name = "HR-险种字典管理")
@RestController
@RequestMapping("/hr/insurance-type")
@RequiredArgsConstructor
public class HrInsuranceTypeController {

    private final HrInsuranceTypeService insuranceTypeService;

    @Operation(summary = "分页查询险种列表")
    @GetMapping("/page")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_INSURANCE_LIST,'')")
    public Result<IPage<HrInsuranceType>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(insuranceTypeService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize)));
    }

    @Operation(summary = "新增险种")
    @PostMapping
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_INSURANCE_ADD,'')")
    public Result<Void> add(@RequestBody HrInsuranceType insuranceType) {
        insuranceTypeService.save(insuranceType);
        return Result.success();
    }

    @Operation(summary = "编辑险种")
    @PutMapping
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_INSURANCE_EDIT,'')")
    public Result<Void> update(@RequestBody HrInsuranceType insuranceType) {
        insuranceTypeService.updateById(insuranceType);
        return Result.success();
    }

    @Operation(summary = "删除险种")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_INSURANCE_DELETE,'')")
    public Result<Void> delete(@PathVariable Long id) {
        insuranceTypeService.removeById(id);
        return Result.success();
    }
}


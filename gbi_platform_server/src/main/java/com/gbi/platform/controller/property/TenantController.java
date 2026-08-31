package com.gbi.platform.controller.property;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.TenantAddDTO;
import com.gbi.platform.dto.TenantQueryDTO;
import com.gbi.platform.dto.TenantUpdateDTO;
import com.gbi.platform.service.TenantService;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.TenantVO;
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

/**
 * 租户管理接口（物业管理模块，前端 api/tenant.ts）
 *
 * @author gbi
 */
@Tag(name = "物业管理-租户管理")
@RestController
@RequestMapping("/property/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @Operation(summary = "租户分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).TENANT_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<TenantVO>> page(@Valid TenantQueryDTO dto) {
        return Result.success(tenantService.page(dto));
    }

    @Operation(summary = "新增租户")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).TENANT_ADD,'')")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody TenantAddDTO dto) {
        tenantService.add(dto);
        return Result.success();
    }

    @Operation(summary = "编辑租户")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).TENANT_EDIT,'')")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody TenantUpdateDTO dto) {
        tenantService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除租户（逻辑删除，有生效合同禁止删除）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).TENANT_DELETE,'')")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        tenantService.delete(id);
        return Result.success();
    }
}
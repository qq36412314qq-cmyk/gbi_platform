package com.gbi.platform.controller.org;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.RoleDTO;
import com.gbi.platform.dto.RoleMenuSaveDTO;
import com.gbi.platform.service.RoleService;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色接口（对齐前端 api/org.ts role 部分）
 *
 * @author gbi
 */
@Tag(name = "角色")
@RestController
@RequestMapping("/org/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "角色列表（下拉）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).ROLE_LIST,'')")
    @GetMapping("/list")
    public Result<List<RoleVO>> list(@RequestParam(required = false) Long companyId) {
        return Result.success(roleService.list(companyId));
    }

    @Operation(summary = "角色分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).ROLE_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<RoleVO>> page(@RequestParam Integer pageNum,
                                       @RequestParam Integer pageSize,
                                       @RequestParam(required = false) String roleName) {
        return Result.success(roleService.page(pageNum, pageSize, roleName));
    }

    @Operation(summary = "新增角色")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).ROLE_ADD,'')")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody RoleDTO dto) {
        roleService.add(dto);
        return Result.success();
    }

    @Operation(summary = "编辑角色")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).ROLE_EDIT,'')")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody RoleDTO dto) {
        roleService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).ROLE_DELETE,'')")
    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @Operation(summary = "查询角色已授权菜单ID")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).ROLE_MENU,'')")
    @GetMapping("/menus")
    public Result<List<Long>> roleMenus(@RequestParam Long roleId) {
        return Result.success(roleService.getRoleMenus(roleId));
    }

    @Operation(summary = "保存角色菜单授权")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).ROLE_MENU,'')")
    @PostMapping("/menus/save")
    public Result<Void> saveRoleMenus(@Valid @RequestBody RoleMenuSaveDTO dto) {
        roleService.saveRoleMenus(dto.getRoleId(), dto.getMenuIds());
        return Result.success();
    }
}
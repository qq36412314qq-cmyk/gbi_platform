package com.gbi.platform.controller.org;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.MenuDTO;
import com.gbi.platform.service.MenuService;
import com.gbi.platform.vo.MenuTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 菜单接口（对齐前端 api/org.ts menu 部分）
 *
 * @author gbi
 */
@Tag(name = "菜单")
@RestController
@RequestMapping("/org/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "菜单树")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).MENU_LIST,'')")
    @GetMapping("/tree")
    public Result<List<MenuTreeVO>> tree() {
        return Result.success(menuService.tree());
    }

    @Operation(summary = "获取当前用户授权菜单树（动态路由用）")
    @GetMapping("/authorized-tree")
    public Result<List<MenuTreeVO>> authorizedTree() {
        return Result.success(menuService.getAuthorizedTree());
    }

    @Operation(summary = "新增菜单")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).MENU_ADD,'')")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody MenuDTO dto) {
        menuService.add(dto);
        return Result.success();
    }

    @Operation(summary = "编辑菜单")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).MENU_EDIT,'')")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody MenuDTO dto) {
        menuService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除菜单")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).MENU_DELETE,'')")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Long> body) {
        Long id = body.get("id");
        menuService.delete(id);
        return Result.success();
    }
}
package com.gbi.platform.controller.org;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.ChangeStatusDTO;
import com.gbi.platform.dto.ResetPwdDTO;
import com.gbi.platform.dto.UserAddDTO;
import com.gbi.platform.dto.UserQueryDTO;
import com.gbi.platform.dto.UserUpdateDTO;
import com.gbi.platform.service.UserService;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.UserListVO;
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

import java.util.Map;

/**
 * 用户接口（对齐前端 api/org.ts user 部分）
 *
 * @author gbi
 */
@Tag(name = "用户")
@RestController
@RequestMapping("/org/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).USER_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<UserListVO>> page(@Valid UserQueryDTO dto) {
        return Result.success(userService.page(dto));
    }

    @Operation(summary = "新增用户")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).USER_ADD,'')")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody UserAddDTO dto) {
        userService.add(dto);
        return Result.success();
    }

    @Operation(summary = "编辑用户")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).USER_EDIT,'')")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody UserUpdateDTO dto) {
        userService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).USER_DELETE,'')")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        userService.delete(id);
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).USER_RESET_PWD,'')")
    @PostMapping("/resetPwd")
    public Result<Void> resetPwd(@Valid @RequestBody ResetPwdDTO dto) {
        userService.resetPwd(dto.getId(), dto.getPassword());
        return Result.success();
    }

    @Operation(summary = "启用/禁用账号")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).USER_CHANGE_STATUS,'')")
    @PostMapping("/changeStatus")
    public Result<Void> changeStatus(@Valid @RequestBody ChangeStatusDTO dto) {
        userService.changeStatus(dto.getId(), dto.getStatus());
        return Result.success();
    }
}
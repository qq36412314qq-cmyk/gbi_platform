package com.gbi.platform.controller.sys;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.ClientDeviceAuthDTO;
import com.gbi.platform.service.SysClientDeviceAuthService;
import com.gbi.platform.vo.ClientDeviceAuthVO;
import com.gbi.platform.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 客户端设备授权管理接口
 *
 * @author gbi
 */
@Tag(name = "客户端设备授权")
@RestController
@RequestMapping("/sys/device/auth")
@RequiredArgsConstructor
public class ClientDeviceAuthController {

    private final SysClientDeviceAuthService deviceAuthService;

    @Operation(summary = "授权设备分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DEVICE_AUTH_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<ClientDeviceAuthVO>> page(@RequestParam Integer pageNum,
                                                    @RequestParam Integer pageSize,
                                                    @RequestParam(required = false) String motherboardSn,
                                                    @RequestParam(required = false) String cpuId) {
        return Result.success(deviceAuthService.page(pageNum, pageSize, motherboardSn, cpuId));
    }

    @Operation(summary = "切换设备启用/禁用状态")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DEVICE_AUTH_TOGGLE,'')")
    @PostMapping("/toggle/{id}")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        deviceAuthService.toggleStatus(id);
        return Result.success();
    }

    @Operation(summary = "新增授权设备")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DEVICE_AUTH_ADD,'')")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody ClientDeviceAuthDTO dto) {
        deviceAuthService.add(dto);
        return Result.success();
    }

    @Operation(summary = "编辑授权设备")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DEVICE_AUTH_ADD,'')")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody ClientDeviceAuthDTO dto) {
        deviceAuthService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除授权设备")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DEVICE_AUTH_DELETE,'')")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        deviceAuthService.delete(id);
        return Result.success();
    }
}

package com.gbi.platform.controller.hr;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.wecom.WecomConfigEditDTO;
import com.gbi.platform.dto.wecom.WecomConfigQueryDTO;
import com.gbi.platform.dto.wecom.WecomSyncTriggerDTO;
import com.gbi.platform.dto.wecom.WecomTenantEditDTO;
import com.gbi.platform.service.wecom.WecomConfigService;
import com.gbi.platform.vo.wecom.WecomConfigVO;
import com.gbi.platform.vo.wecom.WecomStatusVO;
import com.gbi.platform.vo.wecom.WecomSyncTriggerVO;
import com.gbi.platform.vo.wecom.WecomTenantVO;
import com.gbi.platform.vo.wecom.WecomTokenVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企业微信配置管理接口
 *
 * @author gbi
 */
@Tag(name = "企微配置管理")
@RestController
@RequestMapping("/hr/wecom/config")
@RequiredArgsConstructor
public class WecomConfigController {

    private final WecomConfigService wecomConfigService;

    @Operation(summary = "查询企微配置列表")
    @PostMapping("/list")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WECOM_CONFIG_LIST,'')")
    public Result<List<WecomConfigVO>> list(@RequestBody WecomConfigQueryDTO dto) {
        return Result.success(wecomConfigService.listConfigs(dto));
    }

    @Operation(summary = "获取租户corpId配置")
    @PostMapping("/getTenant")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WECOM_CONFIG_VIEW,'')")
    public Result<WecomTenantVO> getTenant(@RequestParam(required = false) Long companyId) {
        return Result.success(wecomConfigService.getTenant(companyId));
    }

    @Operation(summary = "编辑业务配置（sys_config）")
    @PostMapping("/edit")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WECOM_CONFIG_EDIT,'')")
    public Result<Void> edit(@RequestBody WecomConfigEditDTO dto) {
        wecomConfigService.editConfig(dto);
        return Result.success();
    }

    @Operation(summary = "编辑企微租户corpId配置")
    @PostMapping("/editTenant")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WECOM_CONFIG_EDIT,'')")
    public Result<Void> editTenant(@RequestBody WecomTenantEditDTO dto) {
        wecomConfigService.editTenantConfig(dto);
        return Result.success();
    }

    @Operation(summary = "刷新企微access_token")
    @PostMapping("/refreshToken")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WECOM_TOKEN_REFRESH,'')")
    public Result<WecomTokenVO> refreshToken(@RequestParam(required = false) Long companyId) {
        return Result.success(wecomConfigService.refreshToken(companyId));
    }

    @Operation(summary = "获取企微同步开关状态")
    @PostMapping("/status")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WECOM_CONFIG_VIEW,'')")
    public Result<WecomStatusVO> status() {
        return Result.success(wecomConfigService.getStatus());
    }

    @Operation(summary = "手动触发企微同步")
    @PostMapping("/syncTrigger")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WECOM_SYNC_TRIGGER,'')")
    public Result<WecomSyncTriggerVO> syncTrigger(@RequestBody WecomSyncTriggerDTO dto) {
        return Result.success(wecomConfigService.triggerSync(dto));
    }
}

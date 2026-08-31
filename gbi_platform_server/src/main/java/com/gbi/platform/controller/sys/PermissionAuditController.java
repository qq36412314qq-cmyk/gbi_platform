package com.gbi.platform.controller.sys;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.PermissionAuditDTO;
import com.gbi.platform.dto.PermissionAuditQueryDTO;
import com.gbi.platform.service.PermissionAuditService;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.PermissionAuditVO;
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

/**
 * 权限二级复核接口（对齐前端 api/sys.ts permissionAudit 部分）
 *
 * @author gbi
 */
@Tag(name = "权限二级复核")
@RestController
@RequestMapping("/sys/permissionAudit")
@RequiredArgsConstructor
public class PermissionAuditController {

    private final PermissionAuditService permissionAuditService;

    @Operation(summary = "复核记录分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).PERMISSION_AUDIT_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<PermissionAuditVO>> page(@Valid PermissionAuditQueryDTO dto) {
        return Result.success(permissionAuditService.page(dto));
    }

    @Operation(summary = "复核处理：通过/驳回")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).PERMISSION_AUDIT_AUDIT,'')")
    @PostMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody PermissionAuditDTO dto) {
        permissionAuditService.audit(dto);
        return Result.success();
    }
}
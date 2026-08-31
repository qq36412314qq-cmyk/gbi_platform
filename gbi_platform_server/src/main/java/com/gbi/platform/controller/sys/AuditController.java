package com.gbi.platform.controller.sys;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.AuditLogQueryDTO;
import com.gbi.platform.service.AuditService;
import com.gbi.platform.vo.AuditLogVO;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 审计日志接口（对齐前端 api/sys.ts audit 部分，日志只读 + 导出）
 *
 * @author gbi
 */
@Tag(name = "审计日志")
@RestController
@RequestMapping("/sys/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @Operation(summary = "审计日志分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).AUDIT_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<AuditLogVO>> page(@Valid AuditLogQueryDTO dto) {
        return Result.success(auditService.page(dto));
    }

    @Operation(summary = "导出审计日志CSV，返回下载地址")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).AUDIT_EXPORT,'')")
    @PostMapping("/export")
    public Result<Map<String, String>> export(@Valid @RequestBody AuditLogQueryDTO dto) {
        return Result.success(Map.of("url", auditService.export(dto)));
    }
}
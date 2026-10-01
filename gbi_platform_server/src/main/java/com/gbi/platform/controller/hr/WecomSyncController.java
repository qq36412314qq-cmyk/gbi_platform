package com.gbi.platform.controller.hr;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.wecom.WecomSyncQueryDTO;
import com.gbi.platform.service.wecom.WecomSyncService;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.wecom.WecomSyncVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 企微同步记录管理接口
 *
 * @author gbi
 */
@Tag(name = "企微同步记录")
@RestController
@RequestMapping("/hr/wecom/sync")
@RequiredArgsConstructor
public class WecomSyncController {

    private final WecomSyncService wecomSyncService;

    @Operation(summary = "分页查询同步记录")
    @PostMapping("/list")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WECOM_SYNC_VIEW,'')")
    public Result<PageVO<WecomSyncVO>> list(@RequestBody WecomSyncQueryDTO dto) {
        return Result.success(wecomSyncService.pageSyncs(dto));
    }

    @Operation(summary = "查看同步详情")
    @PostMapping("/detail")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WECOM_SYNC_VIEW,'')")
    public Result<WecomSyncVO> detail(@RequestParam Long id) {
        return Result.success(wecomSyncService.getSyncDetail(id));
    }

    @Operation(summary = "导出同步记录")
    @PostMapping("/export")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WECOM_SYNC_EXPORT,'')")
    public void export(@RequestBody WecomSyncQueryDTO dto, HttpServletResponse response) throws IOException {
        wecomSyncService.exportSyncs(dto, response);
    }
}

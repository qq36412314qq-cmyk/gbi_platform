package com.gbi.platform.controller.hr;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.wecom.WecomMessageQueryDTO;
import com.gbi.platform.service.wecom.WecomMessageService;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.wecom.WecomMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 企微消息记录管理接口
 *
 * @author gbi
 */
@Tag(name = "企微消息记录")
@RestController
@RequestMapping("/hr/wecom/message")
@RequiredArgsConstructor
public class WecomMessageController {

    private final WecomMessageService wecomMessageService;

    @Operation(summary = "分页查询消息记录")
    @PostMapping("/list")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WECOM_MESSAGE_VIEW,'')")
    public Result<PageVO<WecomMessageVO>> list(@RequestBody WecomMessageQueryDTO dto) {
        return Result.success(wecomMessageService.pageMessages(dto));
    }

    @Operation(summary = "查看消息详情")
    @PostMapping("/detail")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WECOM_MESSAGE_VIEW,'')")
    public Result<WecomMessageVO> detail(@RequestParam Long id) {
        return Result.success(wecomMessageService.getMessageDetail(id));
    }

    @Operation(summary = "导出消息记录")
    @PostMapping("/export")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WECOM_SYNC_EXPORT,'')")
    public void export(@RequestBody WecomMessageQueryDTO dto, HttpServletResponse response) throws IOException {
        wecomMessageService.exportMessages(dto, response);
    }
}

package com.gbi.platform.controller.property;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.UnifiedPayDTO;
import com.gbi.platform.service.UnifiedPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统一缴费控制器：物业费/水电费共用入口
 *
 * @author gbi
 */
@Tag(name = "统一缴费")
@RestController
@RequestMapping("/property/unifiedPay")
@RequiredArgsConstructor
public class UnifiedPayController {

    private final UnifiedPayService unifiedPayService;

    @Operation(summary = "统一线下缴费（requestId 幂等，同步财务流水）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).UNIFIED_PAY_ADD,\'\')")
    @PostMapping("/pay")
    public Result<Void> pay(@Valid @RequestBody UnifiedPayDTO dto) {
        unifiedPayService.pay(dto);
        return Result.success();
    }
}
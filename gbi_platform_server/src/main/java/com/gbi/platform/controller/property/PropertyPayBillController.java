package com.gbi.platform.controller.property;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.PayBillCreateDTO;
import com.gbi.platform.dto.PayBillPayDTO;
import com.gbi.platform.service.PropertyPayBillService;
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
 * 缴费单控制器（合并缴费）
 * 创建缴费单 + 执行支付
 *
 * @author gbi
 */
@Tag(name = "缴费单管理")
@RestController
@RequestMapping("/property/payBill")
@RequiredArgsConstructor
public class PropertyPayBillController {

    private final PropertyPayBillService propertyPayBillService;

    @Operation(summary = "创建缴费单（从多条统一账单生成一条缴费单）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).UNIFIED_PAY_ADD,'')")
    @PostMapping("/create")
    public Result<Long> createPayBill(@Valid @RequestBody PayBillCreateDTO dto) {
        Long payBillId = propertyPayBillService.createPayBill(dto);
        return Result.success("创建成功", payBillId);
    }

    @Operation(summary = "缴费单支付（更新状态 + 写入财务流水）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).UNIFIED_PAY_ADD,'')")
    @PostMapping("/pay")
    public Result<Void> pay(@Valid @RequestBody PayBillPayDTO dto) {
        propertyPayBillService.pay(dto);
        return Result.success();
    }
}
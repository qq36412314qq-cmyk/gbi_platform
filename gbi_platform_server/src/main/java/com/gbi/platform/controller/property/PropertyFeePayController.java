package com.gbi.platform.controller.property;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.PropertyFeePayDTO;
import com.gbi.platform.service.PropertyFeePayService;
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
 * 物业费缴费接口
 *
 * @author gbi
 */
@Tag(name = "物业费-缴费管理")
@RestController
@RequestMapping("/property/feePay")
@RequiredArgsConstructor
public class PropertyFeePayController {

    private final PropertyFeePayService propertyFeePayService;

    @Operation(summary = "物业费线下缴费（requestId 幂等，同步财务流水）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).PROPERTY_FEE_PAY_ADD,'\'''\'' )")
    @PostMapping("/pay")
    public Result<Void> pay(@Valid @RequestBody PropertyFeePayDTO dto) {
        propertyFeePayService.pay(dto);
        return Result.success();
    }
}

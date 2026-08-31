package com.gbi.platform.controller.waterElec;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.WaterElecPayDTO;
import com.gbi.platform.dto.WaterElecPayQueryDTO;
import com.gbi.platform.dto.WaterElecRefundDTO;
import com.gbi.platform.service.WaterElecPayService;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.WaterElecPayRecordVO;
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
 * 水电物业缴费接口（物业模块，前端 api/waterElec.ts pay 部分）
 * 缴费/退费均带 requestId 幂等，重复提交返回 5002
 *
 * @author gbi
 */
@Tag(name = "水电物业-缴费管理")
@RestController
@RequestMapping("/waterElec/pay")
@RequiredArgsConstructor
public class WaterElecPayController {

    private final WaterElecPayService waterElecPayService;

    @Operation(summary = "缴费记录分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WATER_ELEC_PAY_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<WaterElecPayRecordVO>> page(@Valid WaterElecPayQueryDTO dto) {
        return Result.success(waterElecPayService.page(dto));
    }

    @Operation(summary = "线下缴费（requestId 幂等，同步财务流水）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WATER_ELEC_PAY_ADD,'')")
    @PostMapping("/pay")
    public Result<Void> pay(@Valid @RequestBody WaterElecPayDTO dto) {
        waterElecPayService.pay(dto);
        return Result.success();
    }

    @Operation(summary = "退费（requestId 幂等，写支出流水，账单回退待缴）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WATER_ELEC_PAY_REFUND,'')")
    @PostMapping("/refund")
    public Result<Void> refund(@Valid @RequestBody WaterElecRefundDTO dto) {
        waterElecPayService.refund(dto);
        return Result.success();
    }
}

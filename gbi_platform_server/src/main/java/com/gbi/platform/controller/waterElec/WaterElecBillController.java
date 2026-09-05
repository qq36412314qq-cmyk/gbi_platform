package com.gbi.platform.controller.waterElec;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.WaterElecBillGenerateDTO;
import com.gbi.platform.dto.WaterElecBillQueryDTO;
import com.gbi.platform.service.WaterElecBillService;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.WaterElecBillVO;
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

import java.util.List;

/**
 * 水电物业月度账单接口（物业模块，前端 api/waterElec.ts bill 部分）
 *
 * @author gbi
 */
@Tag(name = "水电物业-账单管理")
@RestController
@RequestMapping("/waterElec/bill")
@RequiredArgsConstructor
public class WaterElecBillController {

    private final WaterElecBillService waterElecBillService;

    @Operation(summary = "账单分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WATER_ELEC_BILL_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<WaterElecBillVO>> page(@Valid WaterElecBillQueryDTO dto) {
        return Result.success(waterElecBillService.page(dto));
    }

    @Operation(summary = "账单详情（缴费核对用）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WATER_ELEC_BILL_LIST,'')")
    @GetMapping("/detail")
    public Result<WaterElecBillVO> detail(@RequestParam Long id) {
        return Result.success(waterElecBillService.detail(id));
    }

    @Operation(summary = "生成月度账单（幂等：同摊位同月份不重复生成）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WATER_ELEC_BILL_GENERATE,'')")
    @PostMapping("/generate")
    public Result<Integer> generate(@Valid @RequestBody WaterElecBillGenerateDTO dto) {
        int count = waterElecBillService.generate(dto);
        return Result.success("生成成功", count);
    }

    @Operation(summary = "同步已有水电费记录到未支付订单（finance_fee_pay_bill）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WATER_ELEC_BILL_LIST,'')")
    @PostMapping("/sync/{id}")
    public Result<String> syncToUnpaidBill(@PathVariable Long id) {
        String msg = waterElecBillService.syncToUnpaidBill(id);
        return Result.success(msg);
    }

    @Operation(summary = "批量将水电费记录同步到未支付订单（finance_fee_pay_bill）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WATER_ELEC_BILL_LIST,'')")
    @PostMapping("/batchSync")
    public Result<Integer> batchSyncToUnpaidBill(@RequestBody List<Long> ids) {
        int count = waterElecBillService.batchSyncToUnpaidBill(ids);
        return Result.success("批量同步成功", count);
    }
}
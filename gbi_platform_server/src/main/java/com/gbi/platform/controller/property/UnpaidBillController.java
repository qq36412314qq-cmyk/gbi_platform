package com.gbi.platform.controller.property;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.UnpaidBillQueryDTO;
import com.gbi.platform.service.UnpaidBillService;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.UnpaidBillVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 未支付订单聚合接口（物业费 + 水电费）
 * 前端"未支付订单"页面数据源，后续对接移动支付入口
 *
 * @author gbi
 */
@Tag(name = "未支付订单聚合查询")
@RestController
@RequestMapping("/property/unpaidBill")
@RequiredArgsConstructor
public class UnpaidBillController {

    private final UnpaidBillService unpaidBillService;

    @Operation(summary = "未支付订单分页（聚合物业费+水电费）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).PROPERTY_UNPAID_BILL_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<UnpaidBillVO>> page(@Valid UnpaidBillQueryDTO dto) {
        return Result.success(unpaidBillService.page(dto));
    }
}

package com.gbi.platform.controller.property;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.PropertyFeeBillGenerateDTO;
import com.gbi.platform.dto.PropertyFeeBillQueryDTO;
import com.gbi.platform.service.PropertyFeeBillService;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.PropertyFeeBillPreviewVO;
import com.gbi.platform.vo.PropertyFeeBillVO;
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

/**
 * 物业费月度账单接口（物业模块）
 *
 * @author gbi
 */
@Tag(name = "物业费-账单管理")
@RestController
@RequestMapping("/property/feeBill")
@RequiredArgsConstructor
public class PropertyFeeBillController {

    private final PropertyFeeBillService propertyFeeBillService;

    @Operation(summary = "物业费账单分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).PROPERTY_FEE_BILL_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<PropertyFeeBillVO>> page(@Valid PropertyFeeBillQueryDTO dto) {
        return Result.success(propertyFeeBillService.page(dto));
    }

    @Operation(summary = "物业费账单详情（缴费核对用）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).PROPERTY_FEE_BILL_LIST,'')")
    @GetMapping("/detail")
    public Result<PropertyFeeBillVO> detail(@RequestParam Long id) {
        return Result.success(propertyFeeBillService.detail(id));
    }

    @Operation(summary = "批量生成物业费账单（按月+市场）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).PROPERTY_FEE_BILL_GENERATE,'')")
    @PostMapping("/generate")
    public Result<Integer> generateBatch(@Valid @RequestBody PropertyFeeBillGenerateDTO dto) {
        int count = propertyFeeBillService.generateBatch(dto);
        return Result.success("生成成功", count);
    }

    @Operation(summary = "单条生成物业费账单（指定摊位）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).PROPERTY_FEE_BILL_GENERATE_SINGLE,'')")
    @PostMapping("/generateSingle")
    public Result<Long> generateSingle(@Valid @RequestBody PropertyFeeBillGenerateDTO dto) {
        Long billId = propertyFeeBillService.generateSingle(dto);
        return Result.success("生成成功", billId);
    }

    @Operation(summary = "预览物业费账单金额（不创建账单）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).PROPERTY_FEE_BILL_LIST,'')")
    @PostMapping("/preview")
    public Result<PropertyFeeBillPreviewVO> preview(@Valid @RequestBody PropertyFeeBillGenerateDTO dto) {
        return Result.success(propertyFeeBillService.preview(dto));
    }

    @Operation(summary = "同步已有账单记录到未支付订单（biz_fee_bill）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).PROPERTY_FEE_BILL_LIST,'')")
    @PostMapping("/sync/{id}")
    public Result<String> syncToUnpaidBill(@PathVariable Long id) {
        String msg = propertyFeeBillService.syncToUnpaidBill(id);
        return Result.success(msg);
    }
}

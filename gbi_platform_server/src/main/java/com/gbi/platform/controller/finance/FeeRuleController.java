package com.gbi.platform.controller.finance;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.FeeRuleDTO;
import com.gbi.platform.dto.FeeRuleQueryDTO;
import com.gbi.platform.service.FeeRuleService;
import com.gbi.platform.vo.FeeRuleVO;
import com.gbi.platform.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 自定义收费规则管理：调用收费类型（feeItemId），收费方式 1定额 2按面积，
 * 收费周期 1按年 2按月 3按日，滞纳金百分比；子公司可配置，集团账号仅只读
 * 权限标识 fee:rule:*
 *
 * @author gbi
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/finance/feeRule")
@Tag(name = "自定义收费规则管理")
public class FeeRuleController {

    private final FeeRuleService feeRuleService;

    @Operation(summary = "收费规则分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FEE_RULE_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<FeeRuleVO>> page(FeeRuleQueryDTO dto) {
        return Result.success(feeRuleService.page(dto));
    }

    @Operation(summary = "新增收费规则")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FEE_RULE_ADD,'')")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody FeeRuleDTO dto) {
        feeRuleService.add(dto);
        return Result.success();
    }

    @Operation(summary = "编辑收费规则")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FEE_RULE_EDIT,'')")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody FeeRuleDTO dto) {
        feeRuleService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除收费规则（逻辑删除）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FEE_RULE_DELETE,'')")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam("id") Long id) {
        feeRuleService.delete(id);
        return Result.success();
    }
}
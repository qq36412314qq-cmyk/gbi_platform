package com.gbi.platform.controller.finance;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.DiscountApplyQueryDTO;
import com.gbi.platform.dto.DiscountPolicyQueryDTO;
import com.gbi.platform.entity.BizDiscountPolicy;
import com.gbi.platform.service.DiscountApplyService;
import com.gbi.platform.service.DiscountPolicyService;
import com.gbi.platform.vo.DiscountApplyVO;
import com.gbi.platform.vo.DiscountPolicyVO;
import com.gbi.platform.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 优惠管理接口（对齐规范 6.3 /finance/discount）
 * 优惠策略：集团模板 company_id=0 + 子公司自建；优惠申请：随合同提交，超集团阈值自动发起审批
 *
 * @author gbi
 */
@Tag(name = "优惠管理")
@RestController
@RequestMapping("/finance/discount")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountPolicyService discountPolicyService;
    private final DiscountApplyService discountApplyService;

    @Operation(summary = "优惠策略分页")
    @GetMapping("/policy/page")
    public Result<PageVO<DiscountPolicyVO>> policyPage(@Valid DiscountPolicyQueryDTO dto) {
        return Result.success(discountPolicyService.page(dto));
    }

    @Operation(summary = "新增优惠策略")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DISCOUNT_POLICY_ADD,'')")
    @PostMapping("/policy/add")
    public Result<Void> policyAdd(@RequestBody BizDiscountPolicy policy) {
        discountPolicyService.add(policy);
        return Result.success();
    }

    @Operation(summary = "编辑优惠策略")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DISCOUNT_POLICY_EDIT,'')")
    @PostMapping("/policy/update")
    public Result<Void> policyUpdate(@RequestBody BizDiscountPolicy policy) {
        discountPolicyService.update(policy);
        return Result.success();
    }

    @Operation(summary = "删除优惠策略（被申请引用禁止删除）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DISCOUNT_POLICY_DELETE,'')")
    @PostMapping("/policy/delete")
    public Result<Void> policyDelete(@RequestParam Long id) {
        discountPolicyService.delete(id);
        return Result.success();
    }

    @Operation(summary = "优惠申请分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DISCOUNT_APPLY_LIST,'')")
    @GetMapping("/apply/page")
    public Result<PageVO<DiscountApplyVO>> applyPage(@Valid DiscountApplyQueryDTO dto) {
        return Result.success(discountApplyService.page(dto));
    }

    @Operation(summary = "优惠申请详情")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DISCOUNT_APPLY_LIST,'')")
    @GetMapping("/apply/detail")
    public Result<DiscountApplyVO> applyDetail(@RequestParam Long id) {
        return Result.success(discountApplyService.detail(id));
    }

    @Operation(summary = "撤销优惠申请（仅草稿/审批中）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DISCOUNT_APPLY_CANCEL,'')")
    @PostMapping("/apply/cancel")
    public Result<Void> applyCancel(@RequestParam Long id) {
        discountApplyService.cancel(id);
        return Result.success();
    }
}
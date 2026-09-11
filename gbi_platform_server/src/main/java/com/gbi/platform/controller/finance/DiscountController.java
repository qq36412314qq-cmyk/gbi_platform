package com.gbi.platform.controller.finance;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.DiscountApplyDTO;
import com.gbi.platform.dto.DiscountApplyQueryDTO;
import com.gbi.platform.dto.DiscountCalcDTO;
import com.gbi.platform.dto.DiscountPolicyDTO;
import com.gbi.platform.dto.DiscountPolicyQueryDTO;
import com.gbi.platform.dto.DiscountThresholdDTO;
import com.gbi.platform.dto.DiscountThresholdQueryDTO;
import com.gbi.platform.entity.BizDiscountPolicy;
import com.gbi.platform.service.DiscountApplyService;
import com.gbi.platform.service.DiscountCalcService;
import com.gbi.platform.service.DiscountPolicyService;
import com.gbi.platform.service.DiscountThresholdService;
import com.gbi.platform.vo.DiscountApplyVO;
import com.gbi.platform.vo.DiscountCalcVO;
import com.gbi.platform.vo.DiscountPolicyVO;
import com.gbi.platform.vo.DiscountThresholdVO;
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
 * 优惠管理接口（支持多业务类型：租赁/物业/水电/幼儿园）
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
    private final DiscountThresholdService discountThresholdService;
    private final DiscountCalcService discountCalcService;

    @Operation(summary = "优惠策略分页")
    @GetMapping("/policy/page")
    public Result<PageVO<DiscountPolicyVO>> policyPage(@Valid DiscountPolicyQueryDTO dto) {
        return Result.success(discountPolicyService.page(dto));
    }

    @Operation(summary = "新增优惠策略")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DISCOUNT_POLICY_ADD,'')")
    @PostMapping("/policy/add")
    public Result<Void> policyAdd(@Valid @RequestBody DiscountPolicyDTO dto) {
        BizDiscountPolicy policy = convertToEntity(dto);
        discountPolicyService.add(policy);
        return Result.success();
    }

    @Operation(summary = "编辑优惠策略")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DISCOUNT_POLICY_EDIT,'')")
    @PostMapping("/policy/update")
    public Result<Void> policyUpdate(@Valid @RequestBody DiscountPolicyDTO dto) {
        BizDiscountPolicy policy = convertToEntity(dto);
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

    @Operation(summary = "提交优惠申请")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DISCOUNT_APPLY_ADD,'')")
    @PostMapping("/apply/submit")
    public Result<Long> applySubmit(@Valid @RequestBody DiscountApplyDTO dto) {
        return Result.success(discountApplyService.createApply(dto));
    }

    @Operation(summary = "撤销优惠申请（仅草稿/审批中）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DISCOUNT_APPLY_CANCEL,'')")
    @PostMapping("/apply/cancel")
    public Result<Void> applyCancel(@RequestParam Long id) {
        discountApplyService.cancel(id);
        return Result.success();
    }

    @Operation(summary = "预览计算优惠金额")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DISCOUNT_APPLY_LIST,'')")
    @PostMapping("/apply/calc")
    public Result<DiscountCalcVO> applyCalc(@Valid @RequestBody DiscountCalcDTO dto) {
        return Result.success(discountCalcService.calc(dto));
    }

    @Operation(summary = "阈值配置分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DISCOUNT_THRESHOLD_LIST,'')")
    @GetMapping("/threshold/page")
    public Result<PageVO<DiscountThresholdVO>> thresholdPage(@Valid DiscountThresholdQueryDTO dto) {
        return Result.success(discountThresholdService.page(dto));
    }

    @Operation(summary = "新增阈值配置")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DISCOUNT_THRESHOLD_ADD,'')")
    @PostMapping("/threshold/add")
    public Result<Void> thresholdAdd(@Valid @RequestBody DiscountThresholdDTO dto) {
        discountThresholdService.add(dto);
        return Result.success();
    }

    @Operation(summary = "编辑阈值配置")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DISCOUNT_THRESHOLD_EDIT,'')")
    @PostMapping("/threshold/update")
    public Result<Void> thresholdUpdate(@Valid @RequestBody DiscountThresholdDTO dto) {
        discountThresholdService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除阈值配置")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DISCOUNT_THRESHOLD_DELETE,'')")
    @PostMapping("/threshold/delete")
    public Result<Void> thresholdDelete(@RequestParam Long id) {
        discountThresholdService.delete(id);
        return Result.success();
    }

    /* ------------------------------ 内部方法 ------------------------------ */

    private BizDiscountPolicy convertToEntity(DiscountPolicyDTO dto) {
        BizDiscountPolicy policy = new BizDiscountPolicy();
        policy.setId(dto.getId());
        policy.setPolicyName(dto.getPolicyName());
        policy.setBizType(dto.getBizType());
        policy.setDiscountType(dto.getDiscountType());
        policy.setWaiveMonths(dto.getWaiveMonths());
        policy.setDiscountRate(dto.getDiscountRate());
        policy.setDeductAmount(dto.getDeductAmount());
        policy.setFixedAmount(dto.getFixedAmount());
        policy.setTierConfig(dto.getTierConfig());
        policy.setScopeType(dto.getScopeType());
        policy.setScopeIds(dto.getScopeIds());
        policy.setStartTime(dto.getStartTime());
        policy.setEndTime(dto.getEndTime());
        policy.setMaxApplyMonths(dto.getMaxApplyMonths());
        policy.setAutoApprove(dto.getAutoApprove());
        policy.setSortOrder(dto.getSortOrder());
        policy.setRemark(dto.getRemark());
        return policy;
    }
}
package com.gbi.platform.controller.finance;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.PlanAdjustDTO;
import com.gbi.platform.dto.PlanGenerateDTO;
import com.gbi.platform.dto.PlanTerminateDTO;
import com.gbi.platform.dto.RecvPayPlanQueryDTO;
import com.gbi.platform.dto.ReconcileQueryDTO;
import com.gbi.platform.service.RecvPayPlanService;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.RecvPayPlanDetailVO;
import com.gbi.platform.vo.RecvPayPlanVO;
import com.gbi.platform.vo.ReconcileDiffVO;
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

import java.util.Map;

/**
 * 应收应付计划接口（对齐规范 6.1 /finance/recvPayPlan）
 * 计划为全系统唯一应收应付台账：生成幂等、调账/作废超阈值审批、自动对账、导出限流审计
 *
 * @author gbi
 */
@Tag(name = "应收应付计划")
@RestController
@RequestMapping("/finance/recvPayPlan")
@RequiredArgsConstructor
public class RecvPayPlanController {

    private final RecvPayPlanService recvPayPlanService;

    @Operation(summary = "计划分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).PLAN_RECVPAY_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<RecvPayPlanVO>> page(@Valid RecvPayPlanQueryDTO dto) {
        return Result.success(recvPayPlanService.page(dto));
    }

    @Operation(summary = "计划详情（含关联账单/核销分摊）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).PLAN_RECVPAY_LIST,'')")
    @GetMapping("/detail")
    public Result<RecvPayPlanDetailVO> detail(@RequestParam Long planId) {
        return Result.success(recvPayPlanService.detail(planId));
    }

    @Operation(summary = "按合同补生成计划（幂等）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).PLAN_RECVPAY_GENERATE,'')")
    @PostMapping("/generate")
    public Result<Void> generate(@Valid @RequestBody PlanGenerateDTO dto) {
        recvPayPlanService.generateByContract(dto.getContractId());
        return Result.success();
    }

    @Operation(summary = "计划人工调账（敏感：二次确认+审计，超阈值走审批）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).PLAN_RECVPAY_ADJUST,'')")
    @PostMapping("/adjust")
    public Result<Void> adjust(@Valid @RequestBody PlanAdjustDTO dto) {
        recvPayPlanService.adjust(dto);
        return Result.success();
    }

    @Operation(summary = "计划作废/终止（高危：审批+红冲链）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).PLAN_RECVPAY_TERMINATE,'')")
    @PostMapping("/terminate")
    public Result<Void> terminate(@Valid @RequestBody PlanTerminateDTO dto) {
        recvPayPlanService.terminatePlan(dto);
        return Result.success();
    }

    @Operation(summary = "自动对账（以计划为权威源输出差异）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).PLAN_RECVPAY_RECONCILE,'')")
    @GetMapping("/reconcile")
    public Result<PageVO<ReconcileDiffVO>> reconcile(@Valid ReconcileQueryDTO dto) {
        return Result.success(recvPayPlanService.reconcile(dto));
    }

    @Operation(summary = "计划导出 CSV")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).PLAN_RECVPAY_EXPORT,'')")
    @PostMapping("/export")
    public Result<Map<String, String>> export(@Valid RecvPayPlanQueryDTO dto) {
        return Result.success(Map.of("url", recvPayPlanService.export(dto)));
    }
}
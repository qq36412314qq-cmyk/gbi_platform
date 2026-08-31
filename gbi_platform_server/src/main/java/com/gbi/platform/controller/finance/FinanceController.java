package com.gbi.platform.controller.finance;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.FinanceFlowQueryDTO;
import com.gbi.platform.dto.FinanceSummaryQueryDTO;
import com.gbi.platform.service.FinanceService;
import com.gbi.platform.vo.FinanceFlowVO;
import com.gbi.platform.vo.FinanceSummaryVO;
import com.gbi.platform.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 财务流水接口（财务模块，前端 api/finance.ts）
 * 双视图：子公司看本公司，集团看全量；流水只读 + 导出
 *
 * @author gbi
 */
@Tag(name = "财务流水")
@RestController
@RequestMapping("/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @Operation(summary = "财务流水分页（双视图自动隔离）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FINANCE_FLOW_LIST,'')")
    @GetMapping("/flow/page")
    public Result<PageVO<FinanceFlowVO>> page(@Valid FinanceFlowQueryDTO dto) {
        return Result.success(financeService.page(dto));
    }

    @Operation(summary = "财务流水导出 CSV（高危操作，审计留痕）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FINANCE_FLOW_EXPORT,'')")
    @PostMapping("/flow/export")
    public Result<String> export(@Valid @RequestBody FinanceFlowQueryDTO dto) {
        return Result.success(financeService.exportCsv(dto));
    }

    @Operation(summary = "营收汇总（按公司/业务类型/收支方向）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FINANCE_REPORT_LIST,'')")
    @GetMapping("/summary")
    public Result<List<FinanceSummaryVO>> summary(@Valid FinanceSummaryQueryDTO dto) {
        return Result.success(financeService.summary(dto));
    }

    @Operation(summary = "冲红申请（需审批）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FINANCE_FLOW_RED_FLUSH,'')")
    @PostMapping("/flow/redFlush")
    public Result<Void> redFlush(@RequestBody @Valid RedFlushDTO dto) {
        financeService.redFlush(dto.getFlowId(), dto.getReason());
        return Result.success();
    }

    @Operation(summary = "冲红审批通过")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FINANCE_FLOW_APPROVE,'')")
    @PostMapping("/flow/approveRedFlush")
    public Result<Void> approveRedFlush(@RequestBody @Valid ApproveRedFlushDTO dto) {
        financeService.approveRedFlush(dto.getFlowId(), dto.getRedFlushFlowId());
        return Result.success();
    }

    @Operation(summary = "作废流水（仅草稿/未记账）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FINANCE_FLOW_VOID,'')")
    @PostMapping("/flow/void")
    public Result<Void> voidFlow(@RequestBody @Valid VoidFlowDTO dto) {
        financeService.voidFlow(dto.getFlowId(), dto.getReason());
        return Result.success();
    }

    @Operation(summary = "打印收据 HTML")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FINANCE_FLOW_LIST,'')")
    @GetMapping("/flow/{flowId}/print")
    public Result<String> print(@PathVariable Long flowId) {
        return Result.success(financeService.getPrintHtml(flowId));
    }

    // ---- DTOs ----
    public static class RedFlushDTO {
        private Long flowId;
        private String reason;
        public Long getFlowId() { return flowId; }
        public void setFlowId(Long flowId) { this.flowId = flowId; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class ApproveRedFlushDTO {
        private Long flowId;
        private Long redFlushFlowId;
        public Long getFlowId() { return flowId; }
        public void setFlowId(Long flowId) { this.flowId = flowId; }
        public Long getRedFlushFlowId() { return redFlushFlowId; }
        public void setRedFlushFlowId(Long redFlushFlowId) { this.redFlushFlowId = redFlushFlowId; }
    }

    public static class VoidFlowDTO {
        private Long flowId;
        private String reason;
        public Long getFlowId() { return flowId; }
        public void setFlowId(Long flowId) { this.flowId = flowId; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}

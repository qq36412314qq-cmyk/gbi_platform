package com.gbi.platform.hr.controller;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.hr.dto.*;
import com.gbi.platform.hr.service.HrTransferService;
import com.gbi.platform.hr.vo.*;
import com.gbi.platform.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "人力资源-人事异动")
@RestController
@RequestMapping("/hr/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final HrTransferService transferService;

    @Operation(summary = "入职申请分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_ENTRY_LIST,'')")
    @GetMapping("/entry/page")
    public Result<PageVO<HrEntryApplyVO>> pageEntry(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(transferService.pageEntry(pageNum, pageSize, status));
    }

    @Operation(summary = "入职申请详情")
    @GetMapping("/entry/{id}")
    public Result<HrEntryApplyVO> getEntry(@PathVariable Long id) {
        return Result.success(transferService.getEntry(id));
    }

    @Operation(summary = "提交入职申请")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_ENTRY_ADD,'')")
    @PostMapping("/entry/submit")
    public Result<Long> submitEntry(@RequestBody EntryApplyDTO dto) {
        return Result.success(transferService.submitEntry(dto));
    }

    @Operation(summary = "撤回入职申请")
    @PostMapping("/entry/revoke/{id}")
    public Result<Void> revokeEntry(@PathVariable Long id) {
        transferService.revokeEntry(id);
        return Result.success();
    }

    @Operation(summary = "转正申请分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_REGULAR_LIST,'')")
    @GetMapping("/regular/page")
    public Result<PageVO<HrRegularApplyVO>> pageRegular(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(transferService.pageRegular(pageNum, pageSize, status));
    }

    @Operation(summary = "提交转正申请")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_REGULAR_ADD,'')")
    @PostMapping("/regular/submit")
    public Result<Long> submitRegular(@RequestBody RegularApplyDTO dto) {
        return Result.success(transferService.submitRegular(dto));
    }

    @Operation(summary = "撤回转正申请")
    @PostMapping("/regular/revoke/{id}")
    public Result<Void> revokeRegular(@PathVariable Long id) {
        transferService.revokeRegular(id);
        return Result.success();
    }

    @Operation(summary = "调岗申请分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_TRANSFER_LIST,'')")
    @GetMapping("/transfer/page")
    public Result<PageVO<HrTransferApplyVO>> pageTransfer(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(transferService.pageTransfer(pageNum, pageSize, status));
    }

    @Operation(summary = "提交调岗申请")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_TRANSFER_ADD,'')")
    @PostMapping("/transfer/submit")
    public Result<Long> submitTransfer(@RequestBody TransferApplyDTO dto) {
        return Result.success(transferService.submitTransfer(dto));
    }

    @Operation(summary = "撤回调岗申请")
    @PostMapping("/transfer/revoke/{id}")
    public Result<Void> revokeTransfer(@PathVariable Long id) {
        transferService.revokeTransfer(id);
        return Result.success();
    }

    @Operation(summary = "离职申请分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_RESIGN_LIST,'')")
    @GetMapping("/resign/page")
    public Result<PageVO<HrResignApplyVO>> pageResign(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(transferService.pageResign(pageNum, pageSize, status));
    }

    @Operation(summary = "提交离职申请")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_RESIGN_ADD,'')")
    @PostMapping("/resign/submit")
    public Result<Long> submitResign(@RequestBody ResignApplyDTO dto) {
        return Result.success(transferService.submitResign(dto));
    }

    @Operation(summary = "撤回离职申请")
    @PostMapping("/resign/revoke/{id}")
    public Result<Void> revokeResign(@PathVariable Long id) {
        transferService.revokeResign(id);
        return Result.success();
    }
}
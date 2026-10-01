package com.gbi.platform.controller.hr;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.hr.HrOvertimeApplyAddDTO;
import com.gbi.platform.dto.hr.HrOvertimeApplyEditDTO;
import com.gbi.platform.dto.hr.HrOvertimeApplyQueryDTO;
import com.gbi.platform.dto.hr.HrOvertimeCompensateCalculateDTO;
import com.gbi.platform.dto.hr.HrOvertimeCompensateQueryDTO;
import com.gbi.platform.dto.hr.HrOvertimeRecordConfirmDTO;
import com.gbi.platform.dto.hr.HrOvertimeRecordQueryDTO;
import com.gbi.platform.dto.hr.SysOvertimeConfigSaveDTO;
import com.gbi.platform.service.hr.HrOvertimeService;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.hr.HrOvertimeApplyVO;
import com.gbi.platform.vo.hr.HrOvertimeAutoDetectVO;
import com.gbi.platform.vo.hr.HrOvertimeCompensateVO;
import com.gbi.platform.vo.hr.HrOvertimeRecordVO;
import com.gbi.platform.vo.hr.SysOvertimeConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

/**
 * 加班管理控制器
 * 提供加班申请、加班记录、加班补偿、加班配置等REST接口
 *
 * @author gbi
 */
@Tag(name = "人力资源-加班管理")
@RestController
@RequestMapping("/hr/overtime")
@RequiredArgsConstructor
public class HrOvertimeController {

    private final HrOvertimeService overtimeService;

    // ==================== 加班申请 ====================

    @Operation(summary = "加班申请分页查询")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_OVERTIME_APPLY_LIST,'')")
    @GetMapping("/apply/page")
    public Result<PageVO<HrOvertimeApplyVO>> pageApply(HrOvertimeApplyQueryDTO dto) {
        return Result.success(overtimeService.pageApply(dto));
    }

    @Operation(summary = "新增加班申请（含发起审批流）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_OVERTIME_APPLY_ADD,'')")
    @PostMapping("/apply/add")
    public Result<Long> addApply(@RequestBody HrOvertimeApplyAddDTO dto) {
        return Result.success(overtimeService.addApply(dto));
    }

    @Operation(summary = "编辑加班申请")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_OVERTIME_APPLY_EDIT,'')")
    @PutMapping("/apply/edit")
    public Result<Void> editApply(@RequestBody HrOvertimeApplyEditDTO dto) {
        overtimeService.editApply(dto);
        return Result.success();
    }

    @Operation(summary = "删除加班申请")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_OVERTIME_APPLY_DELETE,'')")
    @DeleteMapping("/apply/delete/{id}")
    public Result<Void> deleteApply(@PathVariable Long id) {
        overtimeService.deleteApply(id);
        return Result.success();
    }

    @Operation(summary = "撤回加班申请")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_OVERTIME_APPLY_REVOKE,'')")
    @PostMapping("/apply/revoke/{id}")
    public Result<Void> revokeApply(@PathVariable Long id) {
        overtimeService.revokeApply(id);
        return Result.success();
    }

    // ==================== 加班记录 ====================

    @Operation(summary = "加班记录分页查询")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_OVERTIME_RECORD_LIST,'')")
    @GetMapping("/record/page")
    public Result<PageVO<HrOvertimeRecordVO>> pageRecord(HrOvertimeRecordQueryDTO dto) {
        return Result.success(overtimeService.pageRecord(dto));
    }

    @Operation(summary = "确认/驳回加班记录")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_OVERTIME_RECORD_CONFIRM,'')")
    @PostMapping("/record/confirm")
    public Result<Void> confirmRecord(@RequestBody HrOvertimeRecordConfirmDTO dto) {
        overtimeService.confirmRecord(dto);
        return Result.success();
    }

    @Operation(summary = "手动触发自动识别（昨日）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_OVERTIME_RECORD_DETECT,'')")
    @PostMapping("/record/detect")
    public Result<HrOvertimeAutoDetectVO> autoDetect() {
        return Result.success(overtimeService.autoDetect());
    }

    @Operation(summary = "手动触发指定日期自动识别")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_OVERTIME_RECORD_DETECT,'')")
    @PostMapping("/record/detect/byDate")
    public Result<HrOvertimeAutoDetectVO> autoDetectByDate(@RequestParam String date) {
        return Result.success(overtimeService.autoDetectByDate(LocalDate.parse(date)));
    }

    @Operation(summary = "导出加班记录")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_OVERTIME_RECORD_EXPORT,'')")
    @GetMapping("/record/export")
    public void exportRecords(HrOvertimeRecordQueryDTO dto, HttpServletResponse response) throws IOException {
        overtimeService.exportRecords(dto, response);
    }

    // ==================== 加班补偿 ====================

    @Operation(summary = "加班补偿台账分页查询")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_OVERTIME_COMPENSATE_LIST,'')")
    @GetMapping("/compensate/page")
    public Result<PageVO<HrOvertimeCompensateVO>> pageCompensate(HrOvertimeCompensateQueryDTO dto) {
        return Result.success(overtimeService.pageCompensate(dto));
    }

    @Operation(summary = "核算当月加班补偿")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_OVERTIME_COMPENSATE_CALCULATE,'')")
    @PostMapping("/compensate/calculate")
    public Result<Void> calculateCompensate(@RequestBody HrOvertimeCompensateCalculateDTO dto) {
        overtimeService.calculateCompensate(dto);
        return Result.success();
    }

    @Operation(summary = "发放加班费")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_OVERTIME_COMPENSATE_PAY,'')")
    @PostMapping("/compensate/pay/{id}")
    public Result<Void> payCompensate(@PathVariable Long id) {
        overtimeService.payCompensate(id);
        return Result.success();
    }

    @Operation(summary = "导出补偿明细")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_OVERTIME_COMPENSATE_EXPORT,'')")
    @GetMapping("/compensate/export")
    public void exportCompensate(HrOvertimeCompensateQueryDTO dto, HttpServletResponse response) throws IOException {
        overtimeService.exportCompensate(dto, response);
    }

    // ==================== 加班配置 ====================

    @Operation(summary = "查询当前公司加班配置")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_OVERTIME_CONFIG_LIST,'')")
    @GetMapping("/config")
    public Result<SysOvertimeConfigVO> getConfig() {
        return Result.success(overtimeService.getConfig());
    }

    @Operation(summary = "保存加班配置")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_OVERTIME_CONFIG_SAVE,'')")
    @PutMapping("/config/save")
    public Result<Void> saveConfig(@RequestBody SysOvertimeConfigSaveDTO dto) {
        overtimeService.saveConfig(dto);
        return Result.success();
    }

    @Operation(summary = "更新配置状态（启用/禁用）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_OVERTIME_CONFIG_STATUS,'')")
    @PutMapping("/config/status")
    public Result<Void> updateConfigStatus(@RequestParam Long id, @RequestParam Integer status) {
        overtimeService.updateConfigStatus(id, status);
        return Result.success();
    }
}

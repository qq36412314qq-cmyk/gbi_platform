package com.gbi.platform.controller.flow;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.FlowDefDTO;
import com.gbi.platform.dto.FlowHandleDTO;
import com.gbi.platform.dto.FlowQueryDTO;
import com.gbi.platform.dto.FlowSubmitDTO;
import com.gbi.platform.service.FlowEngineService;
import com.gbi.platform.vo.FlowDefVO;
import com.gbi.platform.vo.FlowInstanceDetailVO;
import com.gbi.platform.vo.FlowInstanceVO;
import com.gbi.platform.vo.FlowTaskVO;
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
 * 统一审批中心接口（对齐规范 6.2 /flow）
 * 待办/申请/实例：全公司共享；流程定义：集团专属配置
 *
 * @author gbi
 */
@Tag(name = "统一审批中心")
@RestController
@RequestMapping("/flow")
@RequiredArgsConstructor
public class FlowController {

    private final FlowEngineService flowEngineService;

    @Operation(summary = "我的待办分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FLOW_TASK_LIST,'')")
    @GetMapping("/task/page")
    public Result<PageVO<FlowTaskVO>> pageMyTodo(@Valid FlowQueryDTO dto) {
        return Result.success(flowEngineService.pageMyTodo(dto));
    }

    @Operation(summary = "审批处理（pass/reject/transfer）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FLOW_TASK_HANDLE,'')")
    @PostMapping("/task/handle")
    public Result<Void> handle(@Valid @RequestBody FlowHandleDTO dto) {
        flowEngineService.handle(dto);
        return Result.success();
    }

    @Operation(summary = "催办")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FLOW_TASK_URGE,'')")
    @PostMapping("/task/urge")
    public Result<Void> urge(@RequestParam Long taskId) {
        flowEngineService.urge(taskId);
        return Result.success();
    }

    @Operation(summary = "我的申请分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FLOW_APPLY_LIST,'')")
    @GetMapping("/apply/page")
    public Result<PageVO<FlowInstanceVO>> pageMyApply(@Valid FlowQueryDTO dto) {
        return Result.success(flowEngineService.pageMyApply(dto));
    }

    @Operation(summary = "撤回申请")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FLOW_APPLY_CANCEL,'')")
    @PostMapping("/apply/revoke")
    public Result<Void> revoke(@RequestParam Long instanceId) {
        flowEngineService.revoke(instanceId);
        return Result.success();
    }

    @Operation(summary = "流程定义分页（集团专属）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FLOW_DEF_LIST,'')")
    @GetMapping("/def/page")
    public Result<PageVO<FlowDefVO>> pageDefinition(@Valid FlowQueryDTO dto) {
        return Result.success(flowEngineService.pageDefinition(dto));
    }

    @Operation(summary = "新增流程定义")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FLOW_DEF_ADD,'')")
    @PostMapping("/def/add")
    public Result<Void> addDefinition(@Valid @RequestBody FlowDefDTO dto) {
        flowEngineService.addDefinition(dto);
        return Result.success();
    }

    @Operation(summary = "编辑流程定义")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FLOW_DEF_EDIT,'')")
    @PostMapping("/def/update")
    public Result<Void> updateDefinition(@Valid @RequestBody FlowDefDTO dto) {
        flowEngineService.updateDefinition(dto);
        return Result.success();
    }

    @Operation(summary = "删除流程定义")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FLOW_DEF_DELETE,'')")
    @PostMapping("/def/delete")
    public Result<Void> deleteDefinition(@RequestParam Long id) {
        flowEngineService.deleteDefinition(id);
        return Result.success();
    }

    @Operation(summary = "流程实例分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FLOW_INSTANCE_LIST,'')")
    @GetMapping("/instance/page")
    public Result<PageVO<FlowInstanceVO>> pageInstance(@Valid FlowQueryDTO dto) {
        return Result.success(flowEngineService.pageInstance(dto));
    }

    @Operation(summary = "流程实例详情（含流转轨迹）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FLOW_INSTANCE_LIST,'')")
    @GetMapping("/instance/detail")
    public Result<FlowInstanceDetailVO> instanceDetail(@RequestParam Long instanceId) {
        return Result.success(flowEngineService.instanceDetail(instanceId));
    }
}
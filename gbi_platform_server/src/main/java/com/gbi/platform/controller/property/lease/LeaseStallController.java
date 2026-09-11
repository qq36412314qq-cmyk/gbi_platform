package com.gbi.platform.controller.property.lease;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.StallAddDTO;
import com.gbi.platform.dto.StallQueryDTO;
import com.gbi.platform.dto.StallUpdateDTO;
import com.gbi.platform.service.FeeRuleStallRelService;
import com.gbi.platform.service.LeaseStallService;
import com.gbi.platform.vo.FeeRuleOptionVO;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.StallOptionVO;
import com.gbi.platform.vo.StallRuleRelVO;
import com.gbi.platform.vo.StallVO;
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

import java.util.List;
import java.util.Map;

/**
 * 租赁铺位接口（商铺/仓库/车位等租赁标的，前端 api/lease.ts stall 部分）
 *
 * @author gbi
 */
@Tag(name = "物业管理-租赁铺位")
@RestController
@RequestMapping("/property/lease/stall")
@RequiredArgsConstructor
public class LeaseStallController {

    private final LeaseStallService leaseStallService;

    /** 跨模块调用收费规则绑定 Service 接口：铺位收费规则选择/回显 */
    private final FeeRuleStallRelService feeRuleStallRelService;

    @Operation(summary = "铺位分页（含分类名称）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).LEASE_STALL_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<StallVO>> page(@Valid StallQueryDTO dto) {
        return Result.success(leaseStallService.page(dto));
    }

    @Operation(summary = "铺位联动下拉选项（市场/租赁分类过滤，水电表绑定等复用）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).LEASE_STALL_LIST,'')")
    @GetMapping("/options")
    public Result<List<StallOptionVO>> options(@RequestParam(required = false) Long marketId,
                                               @RequestParam(required = false) Long stallCategoryId,
                                               @RequestParam(required = false) Integer status) {
        return Result.success(leaseStallService.listOptions(marketId, stallCategoryId, status));
    }

    @Operation(summary = "新增铺位（初始状态空置）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).LEASE_STALL_ADD,'')")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody StallAddDTO dto) {
        leaseStallService.add(dto);
        return Result.success();
    }

    @Operation(summary = "编辑铺位")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).LEASE_STALL_EDIT,'')")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody StallUpdateDTO dto) {
        leaseStallService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除铺位（有合同禁止删除，逻辑删除）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).LEASE_STALL_DELETE,'')")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        leaseStallService.delete(id);
        return Result.success();
    }

    @Operation(summary = "启用中的收费规则选项（铺位绑定下拉选择，company_id 自动隔离）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).LEASE_STALL_LIST,'')")
    @GetMapping("/ruleOptions")
    public Result<List<FeeRuleOptionVO>> ruleOptions() {
        return Result.success(feeRuleStallRelService.listRuleOptions());
    }

    @Operation(summary = "铺位已绑定收费规则（编辑回显，company_id 自动隔离）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).LEASE_STALL_LIST,'')")
    @GetMapping("/ruleRel/list")
    public Result<List<StallRuleRelVO>> ruleRelList(@RequestParam("stallId") Long stallId) {
        return Result.success(feeRuleStallRelService.listByStallId(stallId));
    }
}
package com.gbi.platform.controller.property.lease;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.ContractAddDTO;
import com.gbi.platform.dto.ContractQueryDTO;
import com.gbi.platform.dto.ContractTerminateDTO;
import com.gbi.platform.service.LeaseContractService;
import com.gbi.platform.vo.ContractVO;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * 租赁合同接口（租户+铺位租赁，生效联动铺位状态与押金流水，前端 api/lease.ts contract 部分）
 *
 * @author gbi
 */
@Tag(name = "物业管理-租赁合同")
@RestController
@RequestMapping("/property/lease/contract")
@RequiredArgsConstructor
public class LeaseContractController {

    private final LeaseContractService leaseContractService;

    @Operation(summary = "合同分页（含租户/铺位/分类名称）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).LEASE_CONTRACT_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<ContractVO>> page(@Valid ContractQueryDTO dto) {
        return Result.success(leaseContractService.page(dto));
    }

    @Operation(summary = "新增合同（铺位置为已租，押金写收入流水）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).LEASE_CONTRACT_ADD,'')")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody ContractAddDTO dto) {
        leaseContractService.add(dto);
        return Result.success();
    }

    @Operation(summary = "退租（高危操作：铺位置空、押金退费支出流水、强制审计）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).LEASE_CONTRACT_TERMINATE,'')")
    @PostMapping("/terminate")
    public Result<Void> terminate(@Valid @RequestBody ContractTerminateDTO dto) {
        leaseContractService.terminate(dto);
        return Result.success();
    }
}
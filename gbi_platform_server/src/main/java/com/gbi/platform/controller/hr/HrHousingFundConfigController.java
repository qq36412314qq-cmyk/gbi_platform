package com.gbi.platform.controller.hr;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.hr.HrHousingFundDTO;
import com.gbi.platform.service.hr.HrHousingFundConfigService;
import com.gbi.platform.vo.hr.HrHousingFundVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 公积金参数配置管理接口
 */
@Tag(name = "HR-公积金参数配置管理")
@RestController
@RequestMapping("/hr/housing-fund")
@RequiredArgsConstructor
public class HrHousingFundConfigController {

    private final HrHousingFundConfigService housingFundConfigService;

    @Operation(summary = "分页查询公积金参数配置")
    @GetMapping("/page")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_HOUSING_FUND_LIST,'')")
    public Result<IPage<HrHousingFundVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "20") Integer pageSize,
                                               @RequestParam(required = false) String cityCode) {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        return Result.success(housingFundConfigService.page(pageNum, pageSize, cityCode, companyId));
    }

    @Operation(summary = "新增公积金参数配置")
    @PostMapping
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_HOUSING_FUND_ADD,'')")
    public Result<Void> add(@RequestBody HrHousingFundDTO dto) {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        housingFundConfigService.add(dto, companyId);
        return Result.success();
    }

    @Operation(summary = "激活公积金参数配置")
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_HOUSING_FUND_ACTIVATE,'')")
    public Result<Void> activate(@PathVariable Long id) {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        housingFundConfigService.activate(id, companyId);
        return Result.success();
    }

    @Operation(summary = "删除公积金参数配置")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_HOUSING_FUND_DELETE,'')")
    public Result<Void> delete(@PathVariable Long id) {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        housingFundConfigService.delete(id, companyId);
        return Result.success();
    }
}

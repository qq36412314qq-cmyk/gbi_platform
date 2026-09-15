package com.gbi.platform.controller.hr;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.hr.HrSocialParamDTO;
import com.gbi.platform.service.hr.HrSocialParamService;
import com.gbi.platform.vo.hr.HrSocialParamVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "HR-社保参数配置管理")
@RestController
@RequestMapping("/hr/social-param")
@RequiredArgsConstructor
public class HrSocialParamController {

    private final HrSocialParamService socialParamService;

    @Operation(summary = "分页查询社保参数配置")
    @GetMapping("/page")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SOCIAL_PARAM_LIST,'')")
    public Result<IPage<HrSocialParamVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "20") Integer pageSize,
                                               @RequestParam(required = false) String cityCode,
                                               @RequestParam(required = false) String insuranceCode) {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        return Result.success(socialParamService.page(pageNum, pageSize, cityCode, insuranceCode, companyId));
    }

    @Operation(summary = "新增社保参数配置")
    @PostMapping
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SOCIAL_PARAM_ADD,'')")
    public Result<Void> add(@RequestBody HrSocialParamDTO dto) {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        socialParamService.add(dto, companyId);
        return Result.success();
    }

    @Operation(summary = "编辑社保参数配置")
    @PutMapping
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SOCIAL_PARAM_EDIT,'')")
    public Result<Void> edit(@RequestBody HrSocialParamDTO dto) {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        socialParamService.edit(dto, companyId);
        return Result.success();
    }

    @Operation(summary = "激活社保参数配置")
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SOCIAL_PARAM_ACTIVATE,'')")
    public Result<Void> activate(@PathVariable Long id) {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        socialParamService.activate(id, companyId);
        return Result.success();
    }

    @Operation(summary = "切换社保参数配置状态（启用/停用）")
    @PostMapping("/{id}/toggle")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SOCIAL_PARAM_DEACTIVATE,'')")
    public Result<Void> toggle(@PathVariable Long id) {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        socialParamService.toggle(id, companyId);
        return Result.success();
    }

    @Operation(summary = "删除社保参数配置")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SOCIAL_PARAM_DELETE,'')")
    public Result<Void> delete(@PathVariable Long id) {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        socialParamService.delete(id, companyId);
        return Result.success();
    }
}

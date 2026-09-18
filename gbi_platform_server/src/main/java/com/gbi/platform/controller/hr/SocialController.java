package com.gbi.platform.controller.hr;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.hr.SocialDTO;
import com.gbi.platform.service.hr.HrSocialService;
import com.gbi.platform.vo.hr.HrSocialVO;
import com.gbi.platform.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "人力资源-社保公积金")
@RestController
@RequestMapping("/hr/social")
@RequiredArgsConstructor
public class SocialController {

    private final HrSocialService socialService;

    @Operation(summary = "社保分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SOCIAL_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<HrSocialVO>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer status) {
        return Result.success(socialService.pageSocial(pageNum, pageSize, employeeId, status));
    }

    @Operation(summary = "新增社保（已废弃）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SOCIAL_ADD,'')")
    @PostMapping("/add")
    public Result<Void> add(@RequestBody SocialDTO dto) {
        return Result.error("社保台账功能已废弃，请使用 /hr/social-calc/detail 查看核算明细");
    }

    @Operation(summary = "编辑社保（已废弃）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SOCIAL_EDIT,'')")
    @PostMapping("/update")
    public Result<Void> update(@RequestBody SocialDTO dto) {
        return Result.error("社保台账功能已废弃，请使用 /hr/social-calc/detail 查看核算明细");
    }

    @Operation(summary = "删除社保（已废弃）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SOCIAL_DELETE,'')")
    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return Result.error("社保台账功能已废弃，请使用 /hr/social-calc/detail 查看核算明细");
    }
}

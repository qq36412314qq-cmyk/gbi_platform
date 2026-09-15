package com.gbi.platform.controller.hr;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.hr.HrAnnualRecalcDTO;
import com.gbi.platform.service.hr.HrSocialCalcService;
import com.gbi.platform.vo.hr.HrSocialCalcDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 社保核算管理接口
 */
@Tag(name = "HR-社保核算管理")
@RestController
@RequestMapping("/hr/social-calc")
@RequiredArgsConstructor
public class HrSocialCalcController {

    private final HrSocialCalcService socialCalcService;

    @Operation(summary = "分页查询社保核算明细")
    @GetMapping("/detail/page")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SOCIAL_CALC_LIST,'')")
    public Result<IPage<HrSocialCalcDetailVO>> pageDetail(@RequestParam(defaultValue = "1") Integer pageNum,
                                                           @RequestParam(defaultValue = "20") Integer pageSize,
                                                           @RequestParam(required = false) String cityCode,
                                                           @RequestParam(required = false) String salaryMonth) {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        return Result.success(socialCalcService.page(pageNum, pageSize, cityCode, salaryMonth, companyId));
    }

    @Operation(summary = "导出社保核算明细")
    @GetMapping("/detail/export")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SOCIAL_CALC_EXPORT,'')")
    public void exportDetail(@RequestParam(required = false) String cityCode,
                              @RequestParam(required = false) String salaryMonth) {
        // TODO: 导出功能待实现
    }

    @Operation(summary = "触发年度基数重算")
    @PostMapping("/annual-recalc")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_RECALC_EXECUTE,'')")
    public Result<Map<String, Object>> triggerAnnualRecalc(@RequestBody HrAnnualRecalcDTO dto) {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        Map<String, Object> result = socialCalcService.triggerAnnualRecalc(dto, companyId);
        return Result.success(result);
    }
}

package com.gbi.platform.controller.hr;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.entity.hr.HrYearBonus;
import com.gbi.platform.mapper.hr.HrYearBonusMapper;
import com.gbi.platform.service.FlowEngineService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Tag(name = "HR-年终奖管理")
@RestController
@RequestMapping("/hr/salary/yearBonus")
@RequiredArgsConstructor
public class HrYearBonusController {

    private final HrYearBonusMapper yearBonusMapper;
    private final FlowEngineService flowEngineService;

    @Operation(summary = "分页查询年终奖")
    @GetMapping("/page")
    public Result<IPage<HrYearBonus>> page(@RequestParam(required = false) Integer bonusYear,
                                           @RequestParam(required = false) Long employeeId,
                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        LambdaQueryWrapper<HrYearBonus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrYearBonus::getCompanyId, companyId)
               .eq(HrYearBonus::getIsDelete, 0);
        if (bonusYear != null) wrapper.eq(HrYearBonus::getBonusYear, bonusYear);
        if (employeeId != null) wrapper.eq(HrYearBonus::getEmployeeId, employeeId);
        wrapper.orderByDesc(HrYearBonus::getBonusYear).orderByDesc(HrYearBonus::getCreateTime);
        return Result.success(yearBonusMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    @Operation(summary = "新增年终奖")
    @PostMapping("/add")
    public Result<Long> add(@RequestBody HrYearBonus dto) {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        dto.setCompanyId(companyId);
        dto.setApplyStatus(1);
        dto.setStatus(1);
        dto.setPayStatus(0);
        yearBonusMapper.insert(dto);
        return Result.success("新增成功", dto.getId());
    }

    @Operation(summary = "修改年终奖")
    @PostMapping("/update")
    public Result<Void> update(@RequestBody HrYearBonus dto) {
        yearBonusMapper.updateById(dto);
        return Result.success("修改成功", null);
    }

    @Operation(summary = "提交年终奖去审批")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SALARY_YEAR_BONUS_SUBMIT,'')")
    @PostMapping("/submitAudit/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> submitAudit(@PathVariable Long id) {
        HrYearBonus bonus = yearBonusMapper.selectById(id);
        if (bonus == null) return Result.error("年终奖记录不存在");
        Long instanceId = flowEngineService.submit(
                "salary_year_bonus",
                "hr_year_bonus",
                String.valueOf(id),
                "年终奖审批：" + bonus.getEmployeeName() + " " + bonus.getBonusYear()
        );
        bonus.setApplyStatus(0);
        bonus.setFlowInstanceId(instanceId);
        yearBonusMapper.updateById(bonus);
        return Result.success("已提交审批", null);
    }
}

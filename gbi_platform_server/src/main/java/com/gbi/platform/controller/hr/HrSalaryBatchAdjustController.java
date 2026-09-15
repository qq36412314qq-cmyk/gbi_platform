package com.gbi.platform.controller.hr;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.entity.hr.HrSalaryBatchAdjust;
import com.gbi.platform.mapper.hr.HrSalaryBatchAdjustMapper;
import com.gbi.platform.service.FlowEngineService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Tag(name = "HR-批量调薪")
@RestController
@RequestMapping("/hr/salary/batchAdjust")
@RequiredArgsConstructor
public class HrSalaryBatchAdjustController {

    private final HrSalaryBatchAdjustMapper batchMapper;
    private final FlowEngineService flowEngineService;

    @Operation(summary = "分页查询批量调薪任务")
    @GetMapping("/page")
    public Result<IPage<HrSalaryBatchAdjust>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        LambdaQueryWrapper<HrSalaryBatchAdjust> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrSalaryBatchAdjust::getCompanyId, companyId)
               .eq(HrSalaryBatchAdjust::getIsDelete, 0)
               .orderByDesc(HrSalaryBatchAdjust::getCreateTime);
        return Result.success(batchMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    @Operation(summary = "创建批量调薪任务（页面筛选方式，Excel导入二期）")
    @PostMapping("/createByFilter")
    public Result<Long> createByFilter(@RequestBody HrSalaryBatchAdjust dto) {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        dto.setCompanyId(companyId);
        dto.setStatus(0); // 草稿
        batchMapper.insert(dto);
        return Result.success("创建成功", dto.getId());
    }

    @Operation(summary = "提交批量调薪去审批")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_SALARY_BATCH_SUBMIT,'')")
    @PostMapping("/submitAudit/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> submitAudit(@PathVariable Long id) {
        HrSalaryBatchAdjust batch = batchMapper.selectById(id);
        if (batch == null) return Result.error("任务不存在");
        Long instanceId = flowEngineService.submit(
                "salary_batch_adjust",
                "hr_salary_batch_adjust",
                String.valueOf(id),
                "批量调薪审批：" + batch.getAdjustName()
        );
        batch.setStatus(1); // 审批中
        batch.setFlowInstanceId(instanceId);
        batchMapper.updateById(batch);
        return Result.success("已提交审批", null);
    }
}

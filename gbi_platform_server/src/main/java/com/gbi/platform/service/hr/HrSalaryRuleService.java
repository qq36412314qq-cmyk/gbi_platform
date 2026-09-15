package com.gbi.platform.service.hr;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gbi.platform.entity.hr.HrSalaryRule;

public interface HrSalaryRuleService extends IService<HrSalaryRule> {
    IPage<HrSalaryRule> pageByCompany(Long companyId, Integer bindType, Long postId, String gradeCode, Integer pageNum, Integer pageSize);
    /** 根据岗位+薪酬级别获取当前生效模板 */
    HrSalaryRule getEffectiveByPostAndGrade(Long postId, String gradeCode, Long companyId);
    /** 提交模板变更去审批（调用FlowEngineService.submit） */
    void submitAudit(Long ruleId);
}

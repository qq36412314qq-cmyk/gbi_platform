package com.gbi.platform.service.impl.hr;

import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.entity.hr.HrSalaryRule;
import com.gbi.platform.mapper.hr.HrSalaryRuleMapper;
import com.gbi.platform.service.FlowBizHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 薪资模板变更审批回调处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalaryRuleFlowHandler implements FlowBizHandler {

    private final HrSalaryRuleMapper salaryRuleMapper;

    @Override
    public String bizType() {
        return "hr_salary_rule";
    }

    @Override
    public void onSubmit(Long sourceId) {
        HrSalaryRule rule = salaryRuleMapper.selectById(sourceId);
        if (rule == null) {
            throw new com.gbi.platform.common.exception.BizException("薪资模板不存在");
        }
        if (rule.getApplyStatus() != null && rule.getApplyStatus() != 1) {
            throw new com.gbi.platform.common.exception.BizException("模板状态不允许提交审批");
        }
    }

    @Override
    public void onPass(Long sourceId, Long instanceId) {
        log.info("SalaryRuleFlowHandler.onPass: ruleId={}, instanceId={}", sourceId, instanceId);
        HrSalaryRule rule = salaryRuleMapper.selectById(sourceId);
        if (rule != null) {
            rule.setApplyStatus(1);
            rule.setFlowInstanceId(null);
            salaryRuleMapper.updateById(rule);
        }
    }

    @Override
    public void onReject(Long sourceId, Long instanceId) {
        log.info("SalaryRuleFlowHandler.onReject: ruleId={}, instanceId={}", sourceId, instanceId);
        HrSalaryRule rule = salaryRuleMapper.selectById(sourceId);
        if (rule != null) {
            rule.setApplyStatus(2);
            salaryRuleMapper.updateById(rule);
        }
    }

    @Override
    public void onCancel(Long sourceId, Long instanceId) {
        log.info("SalaryRuleFlowHandler.onCancel: ruleId={}, instanceId={}", sourceId, instanceId);
        HrSalaryRule rule = salaryRuleMapper.selectById(sourceId);
        if (rule != null) {
            rule.setApplyStatus(3);
            salaryRuleMapper.updateById(rule);
        }
    }
}

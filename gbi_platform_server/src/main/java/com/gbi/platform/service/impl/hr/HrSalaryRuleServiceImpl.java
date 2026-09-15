package com.gbi.platform.service.impl.hr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.entity.hr.HrSalaryRule;
import com.gbi.platform.mapper.hr.HrSalaryRuleMapper;
import com.gbi.platform.service.hr.HrSalaryRuleService;
import com.gbi.platform.service.FlowEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HrSalaryRuleServiceImpl extends ServiceImpl<HrSalaryRuleMapper, HrSalaryRule> implements HrSalaryRuleService {

    private final FlowEngineService flowEngineService;

    @Override
    public IPage<HrSalaryRule> pageByCompany(Long companyId, Integer bindType, Long postId, String gradeCode, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<HrSalaryRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrSalaryRule::getCompanyId, companyId)
               .eq(HrSalaryRule::getIsDelete, 0);
        if (bindType != null) wrapper.eq(HrSalaryRule::getBindType, bindType);
        if (postId != null) wrapper.eq(HrSalaryRule::getPostId, postId);
        if (gradeCode != null && !gradeCode.isEmpty()) wrapper.eq(HrSalaryRule::getGradeCode, gradeCode);
        wrapper.orderByDesc(HrSalaryRule::getStatus).orderByDesc(HrSalaryRule::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public HrSalaryRule getEffectiveByPostAndGrade(Long postId, String gradeCode, Long companyId) {
        // 优先匹配 岗位+薪酬级别 组合
        HrSalaryRule combo = getOne(new LambdaQueryWrapper<HrSalaryRule>()
                .eq(HrSalaryRule::getCompanyId, companyId)
                .eq(HrSalaryRule::getBindType, 3)
                .eq(HrSalaryRule::getPostId, postId)
                .eq(HrSalaryRule::getGradeCode, gradeCode)
                .eq(HrSalaryRule::getStatus, 1)
                .eq(HrSalaryRule::getApplyStatus, 1)
                .eq(HrSalaryRule::getIsDelete, 0)
                .last("LIMIT 1"));
        if (combo != null) return combo;
        // 次选 单独薪酬级别
        HrSalaryRule grade = getOne(new LambdaQueryWrapper<HrSalaryRule>()
                .eq(HrSalaryRule::getCompanyId, companyId)
                .eq(HrSalaryRule::getBindType, 2)
                .eq(HrSalaryRule::getGradeCode, gradeCode)
                .eq(HrSalaryRule::getStatus, 1)
                .eq(HrSalaryRule::getApplyStatus, 1)
                .eq(HrSalaryRule::getIsDelete, 0)
                .last("LIMIT 1"));
        if (grade != null) return grade;
        // 最后 单独岗位
        return getOne(new LambdaQueryWrapper<HrSalaryRule>()
                .eq(HrSalaryRule::getCompanyId, companyId)
                .eq(HrSalaryRule::getBindType, 1)
                .eq(HrSalaryRule::getPostId, postId)
                .eq(HrSalaryRule::getStatus, 1)
                .eq(HrSalaryRule::getApplyStatus, 1)
                .eq(HrSalaryRule::getIsDelete, 0)
                .last("LIMIT 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAudit(Long ruleId) {
        HrSalaryRule rule = getById(ruleId);
        if (rule == null) throw new BizException("薪资模板不存在");
        if (rule.getApplyStatus() != null && rule.getApplyStatus() != 1) {
            throw new BizException("当前模板状态不允许提交审批");
        }
        Long instanceId = flowEngineService.submit(
                "salary_rule",                    // defCode
                "hr_salary_rule",                 // sourceType
                String.valueOf(ruleId),           // sourceId
                "薪资模板变更：" + rule.getRuleName()
        );
        rule.setApplyStatus(0);
        rule.setFlowInstanceId(instanceId);
        updateById(rule);
    }
}

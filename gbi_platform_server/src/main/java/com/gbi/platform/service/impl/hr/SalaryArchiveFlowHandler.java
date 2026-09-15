package com.gbi.platform.service.impl.hr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gbi.platform.entity.hr.HrEmployee;
import com.gbi.platform.entity.hr.HrEmployeeGradeLog;
import com.gbi.platform.entity.hr.HrSalaryArchive;
import com.gbi.platform.mapper.hr.HrEmployeeGradeLogMapper;
import com.gbi.platform.mapper.hr.HrEmployeeMapper;
import com.gbi.platform.mapper.hr.HrSalaryArchiveMapper;
import com.gbi.platform.service.FlowBizHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 单人调薪/晋升调级审批回调处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalaryArchiveFlowHandler implements FlowBizHandler {

    private final HrEmployeeMapper employeeMapper;
    private final HrEmployeeGradeLogMapper gradeLogMapper;
    private final HrSalaryArchiveMapper archiveMapper;

    @Override
    public String bizType() {
        return "hr_salary_archive";
    }

    @Override
    public void onPass(Long sourceId, Long instanceId) {
        log.info("SalaryArchiveFlowHandler.onPass: archiveId={}, instanceId={}", sourceId, instanceId);
        HrSalaryArchive archive = archiveMapper.selectById(sourceId);
        if (archive == null) return;

        // 1. 旧版本标记为历史
        archiveMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<HrSalaryArchive>()
                .eq(HrSalaryArchive::getEmployeeId, archive.getEmployeeId())
                .eq(HrSalaryArchive::getIsCurrent, 1)
                .set(HrSalaryArchive::getIsCurrent, 0));

        // 2. 创建新版本
        HrSalaryArchive newVersion = new HrSalaryArchive();
        newVersion.setCompanyId(archive.getCompanyId());
        newVersion.setEmployeeId(archive.getEmployeeId());
        newVersion.setEmployeeName(archive.getEmployeeName());
        newVersion.setVersionNo(archive.getVersionNo() + 1);
        newVersion.setSourceType(4); // 晋升调级
        newVersion.setSourceId(instanceId);
        newVersion.setGradeCode(archive.getGradeCode());
        newVersion.setGradeName(archive.getGradeName());
        newVersion.setRuleId(archive.getRuleId());
        newVersion.setRuleName(archive.getRuleName());
        newVersion.setEffectiveDate(LocalDate.now());
        newVersion.setIsCurrent(1);
        newVersion.setBasicSalary(archive.getBasicSalary());
        newVersion.setPerformanceSalary(archive.getPerformanceSalary());
        newVersion.setPositionAllowance(archive.getPositionAllowance());
        newVersion.setOtherAllowance(archive.getOtherAllowance());
        newVersion.setSocialSecurityPersonal(archive.getSocialSecurityPersonal());
        newVersion.setHousingFundPersonal(archive.getHousingFundPersonal());
        newVersion.setRemark(archive.getRemark());
        archiveMapper.insert(newVersion);

        // 3. 更新员工主档案的薪级
        if (archive.getGradeCode() != null) {
            HrEmployee emp = employeeMapper.selectById(archive.getEmployeeId());
            if (emp != null && !archive.getGradeCode().equals(emp.getSalaryGradeCode())) {
                String oldGrade = emp.getSalaryGradeCode();
                emp.setSalaryGradeCode(archive.getGradeCode());
                if (archive.getBasicSalary() != null) {
                    emp.setBasicSalary(archive.getBasicSalary());
                }
                employeeMapper.updateById(emp);
                // 写入薪级变更流水
                HrEmployeeGradeLog logEntity = new HrEmployeeGradeLog();
                logEntity.setCompanyId(archive.getCompanyId());
                logEntity.setEmployeeId(emp.getId());
                logEntity.setEmployeeName(emp.getName());
                logEntity.setFromGradeCode(oldGrade);
                logEntity.setToGradeCode(archive.getGradeCode());
                logEntity.setChangeType(1);
                logEntity.setChangeReason("晋升调级审批通过");
                logEntity.setFlowInstanceId(instanceId);
                logEntity.setEffectiveDate(LocalDate.now());
                logEntity.setBasicSalaryBefore(archive.getBasicSalary() != null ? archive.getBasicSalary() : BigDecimal.ZERO);
                gradeLogMapper.insert(logEntity);
            }
        }
    }

    @Override
    public void onReject(Long sourceId, Long instanceId) {
        log.info("SalaryArchiveFlowHandler.onReject: archiveId={}, instanceId={}", sourceId, instanceId);
        // 审批驳回，不生成新版本，保持原档案不变
    }

    @Override
    public void onCancel(Long sourceId, Long instanceId) {
        log.info("SalaryArchiveFlowHandler.onCancel: archiveId={}, instanceId={}", sourceId, instanceId);
    }
}

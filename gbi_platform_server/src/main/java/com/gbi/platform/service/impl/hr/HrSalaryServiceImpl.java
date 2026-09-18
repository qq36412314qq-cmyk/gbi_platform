package com.gbi.platform.service.impl.hr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.hr.AttendanceQueryDTO;
import com.gbi.platform.dto.hr.SalaryArchiveDTO;
import com.gbi.platform.dto.hr.SalaryMonthDTO;
import com.gbi.platform.entity.hr.*;
import com.gbi.platform.mapper.hr.*;
import com.gbi.platform.service.hr.HrSalaryService;
import com.gbi.platform.vo.hr.*;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HrSalaryServiceImpl implements HrSalaryService {

    private final HrSalaryArchiveMapper salaryArchiveMapper;
    private final HrSalaryMonthMapper salaryMonthMapper;
    private final HrEmployeeMapper employeeMapper;
    private final AuditLogUtil auditLogUtil;
    private final com.gbi.platform.service.FlowEngineService flowEngineService;
    private final com.gbi.platform.service.hr.HrSocialCalcService socialCalcService;
    private final com.gbi.platform.mapper.hr.HrAttendanceRecordMapper attendanceRecordMapper;
    private final com.gbi.platform.mapper.hr.HrSalaryRuleMapper salaryRuleMapper;
    private final com.gbi.platform.mapper.hr.HrCityMapper cityMapper;

    @Override
    public PageVO<HrSalaryArchiveVO> pageArchive(Long pageNum, Long pageSize, Long employeeId) {
        return pageArchive(pageNum, pageSize, employeeId, null, null);
    }

    @Override
    public PageVO<HrSalaryArchiveVO> pageArchive(Long pageNum, Long pageSize, Long employeeId, String keyword, String gradeCode) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<HrSalaryArchive> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HrSalaryArchive> wrapper = new LambdaQueryWrapper<HrSalaryArchive>()
                .eq(HrSalaryArchive::getCompanyId, loginUser.getCompanyId())
                .eq(employeeId != null, HrSalaryArchive::getEmployeeId, employeeId)
                .eq(org.springframework.util.StringUtils.hasText(gradeCode), HrSalaryArchive::getGradeCode, gradeCode)
                .and(org.springframework.util.StringUtils.hasText(keyword), w -> w
                        .like(HrSalaryArchive::getEmployeeName, keyword)
                        .or().like(HrSalaryArchive::getGradeCode, keyword))
                .orderByAsc(HrSalaryArchive::getIsCurrent)
                .orderByDesc(HrSalaryArchive::getEmployeeId)
                .orderByDesc(HrSalaryArchive::getVersionNo);
        Page<HrSalaryArchive> result = salaryArchiveMapper.selectPage(page, wrapper);
        List<HrSalaryArchiveVO> voList = result.getRecords().stream().map(this::toArchiveVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addArchive(SalaryArchiveDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        HrSalaryArchive archive = new HrSalaryArchive();
        archive.setCompanyId(loginUser.getCompanyId());
        archive.setEmployeeId(dto.getEmployeeId());
        HrEmployee employee = employeeMapper.selectById(dto.getEmployeeId());
        if (employee != null) archive.setEmployeeName(employee.getName());
        archive.setGradeCode(dto.getGradeCode());
        archive.setGradeName(dto.getGradeName());
        archive.setRuleId(dto.getRuleId());
        archive.setRuleName(dto.getRuleName());
        // 版本化字段赋默认值：查询该员工最大版本号后+1
        LambdaQueryWrapper<HrSalaryArchive> versionQuery = new LambdaQueryWrapper<HrSalaryArchive>()
                .eq(HrSalaryArchive::getEmployeeId, dto.getEmployeeId())
                .eq(HrSalaryArchive::getCompanyId, loginUser.getCompanyId())
                .orderByDesc(HrSalaryArchive::getVersionNo)
                .last("LIMIT 1");
        HrSalaryArchive maxVersionArchive = salaryArchiveMapper.selectOne(versionQuery);
        archive.setVersionNo(maxVersionArchive != null ? maxVersionArchive.getVersionNo() + 1 : 1);
        archive.setSourceType(dto.getSourceType() != null ? dto.getSourceType() : 2);
        archive.setSourceId(dto.getSourceId());
        archive.setEffectiveDate(dto.getEffectiveDate() != null ? dto.getEffectiveDate() : LocalDate.now());
        archive.setIsCurrent(dto.getIsCurrent() != null ? dto.getIsCurrent() : 1);
        archive.setBasicSalary(dto.getBasicSalary());
        archive.setPerformanceSalary(dto.getPerformanceSalary());
        archive.setPositionAllowance(dto.getPositionAllowance());
        archive.setOtherAllowance(dto.getOtherAllowance());
        archive.setRemark(dto.getRemark());
        archive.setCreateBy(loginUser.getUserId());
        salaryArchiveMapper.insert(archive);
        auditLogUtil.record(CommonConst.MODULE_HR_SALARY, CommonConst.OPER_TYPE_ADD,
                String.valueOf(archive.getId()), null, archive);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArchive(SalaryArchiveDTO dto) {
        if (dto.getId() == null) throw new BizException("薪资档案ID不能为空");
        HrSalaryArchive oldArchive = salaryArchiveMapper.selectById(dto.getId());
        if (oldArchive == null) throw new BizException("薪资档案不存在");
        HrSalaryArchive archive = new HrSalaryArchive();
        archive.setId(dto.getId());
        archive.setBasicSalary(dto.getBasicSalary());
        archive.setPerformanceSalary(dto.getPerformanceSalary());
        archive.setPositionAllowance(dto.getPositionAllowance());
        archive.setOtherAllowance(dto.getOtherAllowance());
        archive.setRemark(dto.getRemark());
        archive.setUpdateBy(UserContext.getLoginUser().getUserId());
        salaryArchiveMapper.updateById(archive);
        auditLogUtil.record(CommonConst.MODULE_HR_SALARY, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(dto.getId()), oldArchive, archive);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArchive(Long id) {
        HrSalaryArchive archive = salaryArchiveMapper.selectById(id);
        if (archive == null) throw new BizException("薪资档案不存在");
        salaryArchiveMapper.deleteById(id);
        auditLogUtil.record(CommonConst.MODULE_HR_SALARY, CommonConst.OPER_TYPE_DELETE,
                String.valueOf(id), archive, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitArchiveAudit(Long archiveId) {
        HrSalaryArchive archive = salaryArchiveMapper.selectById(archiveId);
        if (archive == null) throw new BizException("薪资档案不存在");
        flowEngineService.submit("salary_archive_adjust", "hr_salary_archive", String.valueOf(archiveId),
                "薪资档案变更审批：" + archive.getEmployeeName());
    }

    @Override
    public PageVO<HrSalaryMonthVO> pageMonth(Long pageNum, Long pageSize, Long employeeId, String salaryMonth) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<HrSalaryMonth> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HrSalaryMonth> wrapper = new LambdaQueryWrapper<HrSalaryMonth>()
                .eq(HrSalaryMonth::getCompanyId, loginUser.getCompanyId())
                .eq(employeeId != null, HrSalaryMonth::getEmployeeId, employeeId)
                .eq(salaryMonth != null, HrSalaryMonth::getSalaryMonth, salaryMonth)
                .orderByDesc(HrSalaryMonth::getCreateTime);
        Page<HrSalaryMonth> result = salaryMonthMapper.selectPage(page, wrapper);
        List<HrSalaryMonthVO> voList = result.getRecords().stream().map(this::toMonthVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateMonth(SalaryMonthDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        // 委托给核算服务统一计算社保+公积金分项，生成 hr_salary_month + hr_social_calc_detail
        socialCalcService.generateMonthSalary(dto.getSalaryMonth(), loginUser.getCompanyId());

        // 若启用考勤扣款，追加考勤计算
        boolean syncAttendance = dto.getSyncAttendance() != null && dto.getSyncAttendance() == 1;
        if (!syncAttendance) return;

        String salaryMonth = dto.getSalaryMonth();
        // 聚合当月考勤数据（Map: employeeId -> summary）
        List<Map<String, Object>> summaries = attendanceRecordMapper.selectMonthSummary(loginUser.getCompanyId(), salaryMonth);
        Map<Long, Map<String, Object>> summaryMap = new HashMap<>();
        for (Map<String, Object> row : summaries) {
            Long empId = ((Number) row.get("employee_id")).longValue();
            summaryMap.put(empId, row);
        }

        // 逐条更新 hr_salary_month 考勤扣款
        LambdaQueryWrapper<HrSalaryMonth> monthWrapper = new LambdaQueryWrapper<HrSalaryMonth>()
                .eq(HrSalaryMonth::getCompanyId, loginUser.getCompanyId())
                .eq(HrSalaryMonth::getSalaryMonth, salaryMonth)
                .eq(HrSalaryMonth::getIsDelete, 0);
        List<HrSalaryMonth> months = salaryMonthMapper.selectList(monthWrapper);
        for (HrSalaryMonth month : months) {
            Map<String, Object> att = summaryMap.get(month.getEmployeeId());
            if (att == null) {
                log.warn("generateMonth 考勤聚合无记录: employeeId={}, month={}", month.getEmployeeId(), salaryMonth);
                continue;
            }
            HrSalaryRule rule = salaryRuleMapper.selectOne(new LambdaQueryWrapper<HrSalaryRule>()
                    .eq(HrSalaryRule::getCompanyId, loginUser.getCompanyId())
                    .eq(HrSalaryRule::getId, getRuleId(month.getEmployeeId(), loginUser.getCompanyId())));
            boolean skipAttendance = rule != null && rule.getSkipAttendance() != null && rule.getSkipAttendance() == 1;
            month.setSkipAttendance(skipAttendance ? 1 : 0);
            if (skipAttendance) {
                month.setAttendanceDeduction(BigDecimal.ZERO);
                month.setAbsentDeduction(BigDecimal.ZERO);
                month.setLateDeduction(BigDecimal.ZERO);
                month.setEarlyDeduction(BigDecimal.ZERO);
                month.setUnpaidLeaveDeduction(BigDecimal.ZERO);
                salaryMonthMapper.updateById(month);
                continue;
            }

            BigDecimal dailyWage = (month.getBasicSalary() != null ? month.getBasicSalary() : BigDecimal.ZERO)
                    .divide(new BigDecimal("21.75"), 4, RoundingMode.HALF_UP);
            BigDecimal totalAbsent = att.get("total_absent") != null ? new BigDecimal(att.get("total_absent").toString()) : BigDecimal.ZERO;
            BigDecimal totalLateMin  = att.get("total_late_minutes") != null ? new BigDecimal(att.get("total_late_minutes").toString()) : BigDecimal.ZERO;
            BigDecimal totalEarlyMin = att.get("total_early_minutes") != null ? new BigDecimal(att.get("total_early_minutes").toString()) : BigDecimal.ZERO;
            BigDecimal unpaidLeave   = att.get("unpaid_leave_days") != null ? new BigDecimal(att.get("unpaid_leave_days").toString()) : BigDecimal.ZERO;

            // 默认费率倍数
            BigDecimal lateRate  = rule != null && rule.getLatePenaltyRate() != null ? rule.getLatePenaltyRate() : new BigDecimal("1.00");
            BigDecimal earlyRate = rule != null && rule.getEarlyPenaltyRate() != null ? rule.getEarlyPenaltyRate() : new BigDecimal("1.00");

            BigDecimal absentDed = totalAbsent.multiply(dailyWage).setScale(2, RoundingMode.HALF_UP);
            BigDecimal lateDed   = totalLateMin.multiply(dailyWage).divide(new BigDecimal("480"), 4, RoundingMode.HALF_UP).multiply(lateRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal earlyDed  = totalEarlyMin.multiply(dailyWage).divide(new BigDecimal("480"), 4, RoundingMode.HALF_UP).multiply(earlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal unpaidDed = unpaidLeave.multiply(dailyWage).setScale(2, RoundingMode.HALF_UP);

            BigDecimal attendanceDed = absentDed.add(lateDed).add(earlyDed).add(unpaidDed);
            month.setAbsentDeduction(absentDed);
            month.setLateDeduction(lateDed);
            month.setEarlyDeduction(earlyDed);
            month.setUnpaidLeaveDeduction(unpaidDed);
            month.setAttendanceDeduction(attendanceDed);

            // 最低工资保护
            month.setMinWageProtected(applyMinWageProtection(month, dailyWage));

            // 重新计算 netAmount
            BigDecimal net = month.getGrossAmount()
                    .subtract(month.getSocialSecurity() != null ? month.getSocialSecurity() : BigDecimal.ZERO)
                    .subtract(month.getHousingFund() != null ? month.getHousingFund() : BigDecimal.ZERO)
                    .subtract(month.getTaxAmount() != null ? month.getTaxAmount() : BigDecimal.ZERO)
                    .subtract(attendanceDed)
                    .subtract(month.getDeductionAmount() != null ? month.getDeductionAmount() : BigDecimal.ZERO);
            if (net.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("generateMonth 员工{} {}月netAmount<=0，强制置0.01", month.getEmployeeId(), salaryMonth);
                net = new BigDecimal("0.01");
            }
            month.setNetAmount(net);
            salaryMonthMapper.updateById(month);
        }
    }

    /** 从 employee 关联的薪资档案中取 ruleId（简化：直接查档案） */
    private Long getRuleId(Long employeeId, Long companyId) {
        HrSalaryArchive archive = salaryArchiveMapper.selectOne(new LambdaQueryWrapper<HrSalaryArchive>()
                .eq(HrSalaryArchive::getEmployeeId, employeeId)
                .eq(HrSalaryArchive::getCompanyId, companyId)
                .eq(HrSalaryArchive::getIsCurrent, 1)
                .last("LIMIT 1"));
        return archive != null ? archive.getRuleId() : null;
    }

    /** 最低工资保护逻辑，返回 0 或 1 */
    private int applyMinWageProtection(HrSalaryMonth month, BigDecimal dailyWage) {
        // 获取员工信息判断是否首月
        HrEmployee emp = employeeMapper.selectById(month.getEmployeeId());
        if (emp == null || emp.getEntryDate() == null) return 0;
        // 首月不满整月豁免：entryDate 在当月15日之后
        String[] parts = month.getSalaryMonth().split("-");
        int year = Integer.parseInt(parts[0]);
        int monthNum = Integer.parseInt(parts[1]);
        java.time.LocalDate entry = emp.getEntryDate();
        if (entry.getYear() == year && entry.getMonthValue() == monthNum && entry.getDayOfMonth() >= 15) {
            return 0; // 首月不满整月，豁免
        }
        // 查城市最低工资
        HrCity city = cityMapper.selectOne(new LambdaQueryWrapper<HrCity>()
                .eq(HrCity::getCityCode, emp.getCityCode())
                .eq(HrCity::getIsDelete, 0)
                .last("LIMIT 1"));
        if (city == null || city.getMinWage() == null || city.getMinWage().compareTo(BigDecimal.ZERO) <= 0) return 0;

        BigDecimal social = month.getSocialSecurity() != null ? month.getSocialSecurity() : BigDecimal.ZERO;
        BigDecimal housing = month.getHousingFund() != null ? month.getHousingFund() : BigDecimal.ZERO;
        BigDecimal tax = month.getTaxAmount() != null ? month.getTaxAmount() : BigDecimal.ZERO;
        BigDecimal gross = month.getGrossAmount() != null ? month.getGrossAmount() : BigDecimal.ZERO;
        BigDecimal netBeforeProtection = gross.subtract(social).subtract(housing).subtract(tax)
                .subtract(month.getAttendanceDeduction() != null ? month.getAttendanceDeduction() : BigDecimal.ZERO);
        if (netBeforeProtection.compareTo(city.getMinWage()) >= 0) return 0;

        // 触发保护：允许最大扣款 = gross - social - housing - minWage
        BigDecimal maxAllowedDed = gross.subtract(social).subtract(housing).subtract(city.getMinWage());
        BigDecimal currentDed = month.getAttendanceDeduction() != null ? month.getAttendanceDeduction() : BigDecimal.ZERO;
        if (currentDed.compareTo(maxAllowedDed) > 0) {
            month.setAttendanceDeduction(maxAllowedDed.max(BigDecimal.ZERO));
            // 重新计算 netAmount
            month.setNetAmount(gross.subtract(social).subtract(housing).subtract(tax).subtract(month.getAttendanceDeduction()));
            return 1;
        }
        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payMonth(Long id) {
        HrSalaryMonth month = salaryMonthMapper.selectById(id);
        if (month == null) throw new BizException("薪资核算单不存在");
        if (month.getPayStatus() != null && month.getPayStatus() == 1) {
            throw new BizException("该薪资已发放，不能重复发放");
        }
        month.setPayStatus(1);
        month.setPayTime(java.time.LocalDateTime.now());
        salaryMonthMapper.updateById(month);
        auditLogUtil.record(CommonConst.MODULE_HR_SALARY, CommonConst.OPER_TYPE_PAY,
                String.valueOf(id), null, month);
    }

    @Override
    public List<HrSalaryMonthVO> exportMonth(Long employeeId, String salaryMonth) {
        LambdaQueryWrapper<HrSalaryMonth> wrapper = new LambdaQueryWrapper<HrSalaryMonth>()
                .eq(HrSalaryMonth::getCompanyId, UserContext.getLoginUser().getCompanyId())
                .eq(employeeId != null, HrSalaryMonth::getEmployeeId, employeeId)
                .eq(salaryMonth != null, HrSalaryMonth::getSalaryMonth, salaryMonth);
        return salaryMonthMapper.selectList(wrapper).stream().map(this::toMonthVO).collect(Collectors.toList());
    }

    private HrSalaryArchiveVO toArchiveVO(HrSalaryArchive entity) {
        HrSalaryArchiveVO vo = new HrSalaryArchiveVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setEmployeeId(entity.getEmployeeId());
        vo.setEmployeeName(entity.getEmployeeName());
        vo.setGradeCode(entity.getGradeCode());
        vo.setGradeName(entity.getGradeName());
        vo.setRuleId(entity.getRuleId());
        vo.setRuleName(entity.getRuleName());
        vo.setVersionNo(entity.getVersionNo());
        vo.setSourceType(entity.getSourceType());
        vo.setSourceId(entity.getSourceId());
        vo.setEffectiveDate(entity.getEffectiveDate());
        vo.setIsCurrent(entity.getIsCurrent());
        vo.setBasicSalary(entity.getBasicSalary());
        vo.setPerformanceSalary(entity.getPerformanceSalary());
        vo.setPositionAllowance(entity.getPositionAllowance());
        vo.setOtherAllowance(entity.getOtherAllowance());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private HrSalaryMonthVO toMonthVO(HrSalaryMonth entity) {
        HrSalaryMonthVO vo = new HrSalaryMonthVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setEmployeeId(entity.getEmployeeId());
        vo.setEmployeeName(entity.getEmployeeName());
        vo.setSalaryMonth(entity.getSalaryMonth());
        vo.setBasicSalary(entity.getBasicSalary());
        vo.setPerformanceSalary(entity.getPerformanceSalary());
        vo.setAllowanceAmount(entity.getAllowanceAmount());
        vo.setSocialSecurity(entity.getSocialSecurity());
        vo.setHousingFund(entity.getHousingFund());
        vo.setTaxAmount(entity.getTaxAmount());
        vo.setDeductionAmount(entity.getDeductionAmount());
        vo.setAttendanceDeduction(entity.getAttendanceDeduction());
        vo.setAbsentDeduction(entity.getAbsentDeduction());
        vo.setLateDeduction(entity.getLateDeduction());
        vo.setEarlyDeduction(entity.getEarlyDeduction());
        vo.setUnpaidLeaveDeduction(entity.getUnpaidLeaveDeduction());
        vo.setMinWageProtected(entity.getMinWageProtected());
        vo.setGrossAmount(entity.getGrossAmount());
        vo.setNetAmount(entity.getNetAmount());
        vo.setPayStatus(entity.getPayStatus());
        vo.setPayStatusText(toPayStatusText(entity.getPayStatus()));
        vo.setPayTime(entity.getPayTime());
        vo.setFlowInstanceId(entity.getFlowInstanceId());
        vo.setPlanId(entity.getPlanId());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private String toPayStatusText(Integer status) {
        if (status == null) return null;
        switch (status) {
            case 0: return "未发放";
            case 1: return "已发放";
            case 2: return "发放失败";
            default: return "未知";
        }
    }
}




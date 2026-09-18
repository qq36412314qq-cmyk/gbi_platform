package com.gbi.platform.service.impl.hr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.dto.hr.HrAnnualRecalcDTO;
import com.gbi.platform.entity.hr.*;
import com.gbi.platform.mapper.hr.HrSocialCalcDetailMapper;
import com.gbi.platform.service.hr.HrSocialCalcService;
import com.gbi.platform.vo.hr.HrSocialCalcDetailVO;
import com.gbi.platform.util.AuditLogUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 社保核算服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrSocialCalcServiceImpl implements HrSocialCalcService {

    private final HrSocialCalcDetailMapper calcDetailMapper;
    private final com.gbi.platform.mapper.hr.HrEmployeeMapper employeeMapper;
    private final com.gbi.platform.mapper.hr.HrSalaryMonthMapper salaryMonthMapper;
    private final com.gbi.platform.mapper.hr.HrSocialParamMapper socialParamMapper;
    private final com.gbi.platform.mapper.hr.HrHousingFundConfigMapper housingFundConfigMapper;
    private final com.gbi.platform.mapper.hr.HrSalaryArchiveMapper salaryArchiveMapper;
    private final com.gbi.platform.mapper.hr.HrAttendanceRecordMapper attendanceRecordMapper;
    private final AuditLogUtil auditLogUtil;

    @Override
    public IPage<HrSocialCalcDetailVO> page(Integer pageNum, Integer pageSize, String cityCode,
                                             String salaryMonth, Long companyId) {
        LambdaQueryWrapper<HrSocialCalcDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrSocialCalcDetail::getIsDelete, 0);
        if (companyId != null) {
            wrapper.eq(HrSocialCalcDetail::getCompanyId, companyId);
        }
        if (cityCode != null && !cityCode.isEmpty()) {
            wrapper.eq(HrSocialCalcDetail::getCityCode, cityCode);
        }
        if (salaryMonth != null && !salaryMonth.isEmpty()) {
            wrapper.eq(HrSocialCalcDetail::getSalaryMonth, salaryMonth);
        }
        wrapper.orderByDesc(HrSocialCalcDetail::getCreateTime);
        IPage<HrSocialCalcDetail> detailPage = calcDetailMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);
        return detailPage.convert(item -> {
            HrSocialCalcDetailVO vo = new HrSocialCalcDetailVO();
            vo.setId(item.getId());
            vo.setCompanyId(item.getCompanyId());
            vo.setEmployeeId(item.getEmployeeId());
            vo.setEmployeeName(item.getEmployeeName());
            vo.setCityCode(item.getCityCode());
            vo.setSalaryMonth(item.getSalaryMonth());
            vo.setBaseEffectiveYear(item.getBaseEffectiveYear());
            vo.setSocialBase(item.getSocialBase());
            vo.setHousingFundBase(item.getHousingFundBase());
            vo.setPensionPersonal(item.getPensionPersonal());
            vo.setPensionCompany(item.getPensionCompany());
            vo.setMedicalPersonal(item.getMedicalPersonal());
            vo.setMedicalCompany(item.getMedicalCompany());
            vo.setUnemploymentPersonal(item.getUnemploymentPersonal());
            vo.setUnemploymentCompany(item.getUnemploymentCompany());
            vo.setWorkInjuryCompany(item.getWorkInjuryCompany());
            vo.setMaternityCompany(item.getMaternityCompany());
            vo.setLongCarePersonal(item.getLongCarePersonal());
            vo.setLongCareCompany(item.getLongCareCompany());
            vo.setHousingFundPersonal(item.getHousingFundPersonal());
            vo.setHousingFundCompany(item.getHousingFundCompany());
            vo.setRoundingDiff(item.getRoundingDiff());
            vo.setCreateTime(item.getCreateTime() != null ? item.getCreateTime().toString() : null);
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> triggerAnnualRecalc(HrAnnualRecalcDTO dto, Long companyId) {
        String recalcYear = dto.getRecalcYear();
        if (recalcYear == null || recalcYear.isEmpty()) {
            recalcYear = String.valueOf(java.time.Year.now().getValue() - 1);
        }
        // 基数生效周期：当年7月 ~ 次年6月
        String effectiveYear = recalcYear;

        // 查询该年度前一年的所有发薪记录，按员工汇总
        String prevYearStart = recalcYear + "-01-01";
        String prevYearEnd = recalcYear + "-12-31";

        // 获取需要重算的员工列表（有city_code且未处理过该年度的）
        LambdaQueryWrapper<HrEmployee> empWrapper = new LambdaQueryWrapper<>();
        empWrapper.isNotNull(HrEmployee::getCityCode)
                  .ne(HrEmployee::getBaseEffectiveYear, effectiveYear)
                  .eq(HrEmployee::getIsDelete, 0);
        if (companyId != null && companyId > 0) {
            empWrapper.eq(HrEmployee::getCompanyId, companyId);
        }
        List<HrEmployee> employees = employeeMapper.selectList(empWrapper);

        int totalCount = employees.size();
        int successCount = 0;
        int failCount = 0;
        List<Long> processedIds = new java.util.ArrayList<>();

        for (HrEmployee emp : employees) {
            try {
                // 查询该员工上年度全部薪资记录
                LambdaQueryWrapper<com.gbi.platform.entity.hr.HrSalaryMonth> monthWrapper =
                        new LambdaQueryWrapper<>();
                monthWrapper.eq(com.gbi.platform.entity.hr.HrSalaryMonth::getEmployeeId, emp.getId())
                            .ge(com.gbi.platform.entity.hr.HrSalaryMonth::getSalaryMonth, recalcYear + "-01")
                            .le(com.gbi.platform.entity.hr.HrSalaryMonth::getSalaryMonth, recalcYear + "-12")
                            .eq(com.gbi.platform.entity.hr.HrSalaryMonth::getIsDelete, 0);
                List<com.gbi.platform.entity.hr.HrSalaryMonth> salaryRecords =
                        salaryMonthMapper.selectList(monthWrapper);

                if (salaryRecords.isEmpty()) {
                    failCount++;
                    continue;
                }

                // 计算上年度总收入和月份数
                BigDecimal totalIncome = BigDecimal.ZERO;
                for (com.gbi.platform.entity.hr.HrSalaryMonth record : salaryRecords) {
                    if (record.getGrossAmount() != null) {
                        totalIncome = totalIncome.add(record.getGrossAmount());
                    }
                }
                int monthCount = salaryRecords.size();
                if (monthCount == 0) {
                    failCount++;
                    continue;
                }

                // 计算月均工资
                BigDecimal monthlyAvg = totalIncome.divide(
                        new BigDecimal(monthCount), 2, RoundingMode.HALF_UP);

                // 获取城市社保上下限
                HrSocialParamConfig socialConfig = socialParamMapper.selectOne(
                        new LambdaQueryWrapper<HrSocialParamConfig>()
                                .eq(HrSocialParamConfig::getCityCode, emp.getCityCode())
                                .eq(HrSocialParamConfig::getInsuranceCode, "PENSION")
                                .eq(HrSocialParamConfig::getIsActive, 1)
                                .eq(HrSocialParamConfig::getIsDelete, 0)
                                .last("LIMIT 1"));

                HrHousingFundConfig housingConfig = housingFundConfigMapper.selectOne(
                        new LambdaQueryWrapper<HrHousingFundConfig>()
                                .eq(HrHousingFundConfig::getCityCode, emp.getCityCode())
                                .eq(HrHousingFundConfig::getIsActive, 1)
                                .eq(HrHousingFundConfig::getIsDelete, 0)
                                .last("LIMIT 1"));

                // 截断到上下限
                BigDecimal socialBase = monthlyAvg;
                if (socialConfig != null && socialConfig.getBaseMin() != null) {
                    socialBase = socialBase.max(socialConfig.getBaseMin());
                }
                if (socialConfig != null && socialConfig.getBaseMax() != null) {
                    socialBase = socialBase.min(socialConfig.getBaseMax());
                }

                BigDecimal housingBase = monthlyAvg;
                if (housingConfig != null && housingConfig.getBaseMin() != null) {
                    housingBase = housingBase.max(housingConfig.getBaseMin());
                }
                if (housingConfig != null && housingConfig.getBaseMax() != null) {
                    housingBase = housingBase.min(housingConfig.getBaseMax());
                }

                // 更新员工档案
                HrEmployee update = new HrEmployee();
                update.setId(emp.getId());
                update.setSocialDeclareBase(socialBase);
                update.setHousingFundDeclareBase(housingBase);
                update.setBaseEffectiveYear(effectiveYear);
                employeeMapper.updateById(update);

                // 审计单条记录
                Map<String, Object> before = new HashMap<>();
                before.put("socialBase", emp.getSocialDeclareBase());
                before.put("housingFundBase", emp.getHousingFundDeclareBase());
                before.put("baseEffectiveYear", emp.getBaseEffectiveYear());
                Map<String, Object> after = new HashMap<>();
                after.put("socialBase", socialBase);
                after.put("housingFundBase", housingBase);
                after.put("baseEffectiveYear", effectiveYear);
                auditLogUtil.record(
                        CommonConst.MODULE_HR_SOCIAL_CALC,
                        CommonConst.OPER_TYPE_RECALC_BASE,
                        String.valueOf(emp.getId()),
                        before,
                        after);

                successCount++;
                processedIds.add(emp.getId());
            } catch (Exception e) {
                failCount++;
            }
        }

        // 审计整体执行结果
        Map<String, Object> resultSummary = new HashMap<>();
        resultSummary.put("totalCount", totalCount);
        resultSummary.put("successCount", successCount);
        resultSummary.put("failCount", failCount);
        resultSummary.put("effectiveYear", effectiveYear);
        auditLogUtil.record(
                CommonConst.MODULE_HR_SOCIAL_CALC,
                CommonConst.OPER_TYPE_RECALC_BASE,
                "batch_" + recalcYear,
                null,
                resultSummary);

        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("effectiveYear", effectiveYear);
        result.put("processedIds", processedIds);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateMonthSalary(String salaryMonth, Long companyId) {
        // 1. 查询该司所有当前薪资档案（isCurrent=1）
        LambdaQueryWrapper<HrSalaryArchive> archiveWrapper = new LambdaQueryWrapper<HrSalaryArchive>()
                .eq(HrSalaryArchive::getCompanyId, companyId)
                .eq(HrSalaryArchive::getIsCurrent, 1)
                .eq(HrSalaryArchive::getIsDelete, 0);
        List<HrSalaryArchive> archives = salaryArchiveMapper.selectList(archiveWrapper);
        if (archives == null || archives.isEmpty()) {
            log.warn("generateMonthSalary: 公司{} 无当前薪资档案，跳过核算，月份={}", companyId, salaryMonth);
            return;
        }

        // 2. 预先批量查询费率配置缓存
        Map<String, Map<String, HrSocialParamConfig>> citySocialParamCache = new HashMap<>();
        Map<String, HrHousingFundConfig> cityHousingConfigCache = new HashMap<>();

        // 3. 逐个员工核算
        int successCount = 0, failCount = 0;
        for (HrSalaryArchive archive : archives) {
            try {
                // 查员工信息（城市编码、申报基数）
                HrEmployee employee = employeeMapper.selectById(archive.getEmployeeId());
                if (employee == null) { failCount++; continue; }
                if (!companyId.equals(employee.getCompanyId())) { failCount++; continue; }

                String cityCode = employee.getCityCode();
                BigDecimal declareBase = employee.getSocialDeclareBase() != null
                        ? employee.getSocialDeclareBase()
                        : employee.getBasicSalary() != null ? employee.getBasicSalary().max(BigDecimal.ZERO) : BigDecimal.ZERO;
                BigDecimal housingDeclareBase = employee.getHousingFundDeclareBase() != null
                        ? employee.getHousingFundDeclareBase()
                        : declareBase;

                // 查社保费率配置
                Map<String, HrSocialParamConfig> socialParams = citySocialParamCache.computeIfAbsent(cityCode, k -> {
                    Map<String, HrSocialParamConfig> m = new HashMap<>();
                    LambdaQueryWrapper<HrSocialParamConfig> spw = new LambdaQueryWrapper<HrSocialParamConfig>()
                            .eq(HrSocialParamConfig::getCityCode, k)
                            .eq(HrSocialParamConfig::getIsActive, 1)
                            .eq(HrSocialParamConfig::getIsDelete, 0);
                    socialParamMapper.selectList(spw).forEach(c -> m.put(c.getInsuranceCode(), c));
                    return m;
                });
                // 查公积金费率配置
                HrHousingFundConfig housingConfig = cityHousingConfigCache.computeIfAbsent(cityCode, k -> {
                    LambdaQueryWrapper<HrHousingFundConfig> hw = new LambdaQueryWrapper<HrHousingFundConfig>()
                            .eq(HrHousingFundConfig::getCityCode, k)
                            .eq(HrHousingFundConfig::getIsActive, 1)
                            .eq(HrHousingFundConfig::getIsDelete, 0)
                            .last("LIMIT 1");
                    return housingFundConfigMapper.selectOne(hw);
                });

                // clamp 基数
                BigDecimal socialBase = clampBase(declareBase, socialParams);
                BigDecimal housingBase = clampBase(housingDeclareBase, housingConfig);

                // 计算各险种金额
                HrSocialParamConfig pensionCfg = socialParams.get("PENSION");
                HrSocialParamConfig medicalCfg = socialParams.get("MEDICAL");
                HrSocialParamConfig unemploymentCfg = socialParams.get("UNEMPLOYMENT");
                HrSocialParamConfig workInjuryCfg = socialParams.get("WORK_INJURY");
                HrSocialParamConfig maternityCfg = socialParams.get("MATERNITY");
                HrSocialParamConfig longCareCfg = socialParams.get("LONG_CARE");

                BigDecimal pensionPersonal = calcRate(socialBase, pensionCfg != null ? pensionCfg.getPersonalRate() : null);
                BigDecimal pensionCompany   = calcRate(socialBase, pensionCfg != null ? pensionCfg.getCompanyRate()  : null);
                BigDecimal medicalPersonal  = calcRate(socialBase, medicalCfg != null ? medicalCfg.getPersonalRate() : null);
                BigDecimal medicalCompany   = calcRate(socialBase, medicalCfg != null ? medicalCfg.getCompanyRate()  : null);
                BigDecimal unemploymentPersonal = calcRate(socialBase, unemploymentCfg != null ? unemploymentCfg.getPersonalRate() : null);
                BigDecimal unemploymentCompany  = calcRate(socialBase, unemploymentCfg != null ? unemploymentCfg.getCompanyRate()  : null);
                BigDecimal workInjuryCompany    = calcRate(socialBase, workInjuryCfg != null ? workInjuryCfg.getCompanyRate()  : null);
                BigDecimal maternityCompany     = calcRate(socialBase, maternityCfg != null ? maternityCfg.getCompanyRate()  : null);
                BigDecimal longCarePersonal     = calcRate(socialBase, longCareCfg != null ? longCareCfg.getPersonalRate() : null);
                BigDecimal longCareCompany      = calcRate(socialBase, longCareCfg != null ? longCareCfg.getCompanyRate()  : null);

                BigDecimal housingFundPersonal = calcRate(housingBase, housingConfig != null ? housingConfig.getEmployeeRate() : null);
                BigDecimal housingFundCompany  = calcRate(housingBase, housingConfig != null ? housingConfig.getCompanyRate()  : null);

                // 汇总
                BigDecimal socialTotalPersonal = pensionPersonal.add(medicalPersonal)
                        .add(unemploymentPersonal).add(longCarePersonal);
                BigDecimal socialTotalCompany  = pensionCompany.add(medicalCompany)
                        .add(unemploymentCompany).add(workInjuryCompany).add(maternityCompany).add(longCareCompany);
                BigDecimal housingTotal = housingFundPersonal.add(housingFundCompany);

                // 应发工资
                BigDecimal grossAmount = (archive.getBasicSalary() != null ? archive.getBasicSalary() : BigDecimal.ZERO)
                        .add(archive.getPerformanceSalary() != null ? archive.getPerformanceSalary() : BigDecimal.ZERO)
                        .add(archive.getPositionAllowance() != null ? archive.getPositionAllowance() : BigDecimal.ZERO)
                        .add(archive.getOtherAllowance() != null ? archive.getOtherAllowance() : BigDecimal.ZERO);

                // 个税（累计预扣法简化版：按当月应税所得计算）
                BigDecimal taxableIncome = grossAmount
                        .subtract(socialTotalPersonal)
                        .subtract(housingFundPersonal)
                        .subtract(new BigDecimal("5000"));
                BigDecimal taxAmount = calcTax(taxableIncome.max(BigDecimal.ZERO));

                // 实发
                BigDecimal netAmount = grossAmount
                        .subtract(socialTotalPersonal)
                        .subtract(housingFundPersonal)
                        .subtract(taxAmount)
                        .subtract(BigDecimal.ZERO); // attendanceDeduction 由后续逻辑补充

                // --- 写入 hr_social_calc_detail ---
                HrSocialCalcDetail detail = new HrSocialCalcDetail();
                detail.setCompanyId(companyId);
                detail.setEmployeeId(archive.getEmployeeId());
                detail.setEmployeeName(archive.getEmployeeName());
                detail.setCityCode(cityCode);
                detail.setSalaryMonth(salaryMonth);
                detail.setBaseEffectiveYear(determineBaseEffectiveYear(salaryMonth));
                detail.setSocialBase(socialBase);
                detail.setHousingFundBase(housingBase);
                detail.setPensionPersonal(pensionPersonal);  detail.setPensionRatePersonal(pensionCfg != null ? pensionCfg.getPersonalRate() : null);
                detail.setPensionCompany(pensionCompany);    detail.setPensionRateCompany(pensionCfg != null ? pensionCfg.getCompanyRate() : null);
                detail.setMedicalPersonal(medicalPersonal);  detail.setMedicalRatePersonal(medicalCfg != null ? medicalCfg.getPersonalRate() : null);
                detail.setMedicalCompany(medicalCompany);    detail.setMedicalRateCompany(medicalCfg != null ? medicalCfg.getCompanyRate() : null);
                detail.setUnemploymentPersonal(unemploymentPersonal); detail.setUnemploymentRatePersonal(unemploymentCfg != null ? unemploymentCfg.getPersonalRate() : null);
                detail.setUnemploymentCompany(unemploymentCompany);   detail.setUnemploymentRateCompany(unemploymentCfg != null ? unemploymentCfg.getCompanyRate() : null);
                detail.setWorkInjuryCompany(workInjuryCompany);     detail.setWorkInjuryRate(workInjuryCfg != null ? workInjuryCfg.getCompanyRate() : null);
                detail.setMaternityCompany(maternityCompany);       detail.setMaternityRate(maternityCfg != null ? maternityCfg.getCompanyRate() : null);
                detail.setLongCarePersonal(longCarePersonal);       detail.setLongCareRatePersonal(longCareCfg != null ? longCareCfg.getPersonalRate() : null);
                detail.setLongCareCompany(longCareCompany);         detail.setLongCareRateCompany(longCareCfg != null ? longCareCfg.getCompanyRate() : null);
                detail.setHousingFundPersonal(housingFundPersonal); detail.setHousingFundRate(housingConfig != null ? housingConfig.getEmployeeRate() : null);
                detail.setHousingFundCompany(housingFundCompany);
                detail.setRoundingDiff(BigDecimal.ZERO);
                calcDetailMapper.insert(detail);

                // --- 写入 / 更新 hr_salary_month ---
                LambdaQueryWrapper<HrSalaryMonth> monthWrapper = new LambdaQueryWrapper<HrSalaryMonth>()
                        .eq(HrSalaryMonth::getEmployeeId, archive.getEmployeeId())
                        .eq(HrSalaryMonth::getSalaryMonth, salaryMonth)
                        .eq(HrSalaryMonth::getCompanyId, companyId)
                        .eq(HrSalaryMonth::getIsDelete, 0);
                HrSalaryMonth existing = salaryMonthMapper.selectOne(monthWrapper);
                HrSalaryMonth month = existing != null ? existing : new HrSalaryMonth();
                month.setCompanyId(companyId);
                month.setEmployeeId(archive.getEmployeeId());
                month.setEmployeeName(archive.getEmployeeName());
                month.setSalaryMonth(salaryMonth);
                month.setBasicSalary(archive.getBasicSalary());
                month.setPerformanceSalary(archive.getPerformanceSalary());
                month.setAllowanceAmount((archive.getPositionAllowance() != null ? archive.getPositionAllowance() : BigDecimal.ZERO)
                        .add(archive.getOtherAllowance() != null ? archive.getOtherAllowance() : BigDecimal.ZERO));
                month.setSocialSecurity(socialTotalPersonal);
                month.setHousingFund(housingFundPersonal);
                month.setTaxAmount(taxAmount);
                month.setDeductionAmount(BigDecimal.ZERO);
                month.setGrossAmount(grossAmount);
                month.setNetAmount(netAmount);
                month.setPayStatus(0);
                // 分项快照
                month.setPensionPersonal(pensionPersonal);  month.setPensionCompany(pensionCompany);
                month.setMedicalPersonal(medicalPersonal);  month.setMedicalCompany(medicalCompany);
                month.setUnemploymentPersonal(unemploymentPersonal); month.setUnemploymentCompany(unemploymentCompany);
                month.setWorkInjuryCompany(workInjuryCompany);
                month.setMaternityCompany(maternityCompany);
                month.setLongCarePersonal(longCarePersonal); month.setLongCareCompany(longCareCompany);
                month.setHousingFundPersonal(housingFundPersonal); month.setHousingFundCompany(housingFundCompany);
                month.setSocialBase(socialBase);
                month.setHousingFundBase(housingBase);
                month.setBaseEffectiveYear(detail.getBaseEffectiveYear());
                month.setAttendanceDeduction(BigDecimal.ZERO); // 默认 0，后续考勤逻辑补充
                month.setAbsentDeduction(BigDecimal.ZERO);
                month.setLateDeduction(BigDecimal.ZERO);
                month.setEarlyDeduction(BigDecimal.ZERO);
                month.setUnpaidLeaveDeduction(BigDecimal.ZERO);
                month.setMinWageProtected(0);
                month.setSkipAttendance(0);
                month.setCreateBy(existing != null ? existing.getCreateBy() : null);
                if (existing == null) {
                    salaryMonthMapper.insert(month);
                } else {
                    salaryMonthMapper.updateById(month);
                }
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("generateMonthSalary 员工核算失败: employeeId={}, salaryMonth={}", archive.getEmployeeId(), salaryMonth, e);
            }
        }
        log.info("generateMonthSalary 完成: companyId={}, salaryMonth={}, total={}, success={}, fail={}",
                companyId, salaryMonth, archives.size(), successCount, failCount);
    }

    /** clamp 基数到配置上下限 */
    private BigDecimal clampBase(BigDecimal declareBase, Map<String, HrSocialParamConfig> params) {
        if (params == null || declareBase == null) return BigDecimal.ZERO;
        HrSocialParamConfig pensionCfg = params.get("PENSION");
        if (pensionCfg == null) return declareBase;
        BigDecimal base = declareBase;
        if (pensionCfg.getBaseMin() != null) base = base.max(pensionCfg.getBaseMin());
        if (pensionCfg.getBaseMax() != null) base = base.min(pensionCfg.getBaseMax());
        return base.setScale(2, RoundingMode.HALF_UP);
    }

    /** clamp 基数到公积金配置上下限 */
    private BigDecimal clampBase(BigDecimal declareBase, HrHousingFundConfig config) {
        if (config == null || declareBase == null) return BigDecimal.ZERO;
        BigDecimal base = declareBase;
        if (config.getBaseMin() != null) base = base.max(config.getBaseMin());
        if (config.getBaseMax() != null) base = base.min(config.getBaseMax());
        return base.setScale(2, RoundingMode.HALF_UP);
    }

    /** 按比例计算金额 */
    private BigDecimal calcRate(BigDecimal base, BigDecimal rate) {
        if (base == null || base.compareTo(BigDecimal.ZERO) <= 0 || rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return base.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    /** 个税累计预扣法（简化单月版） */
    private BigDecimal calcTax(BigDecimal taxableIncome) {
        if (taxableIncome.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        BigDecimal amount = taxableIncome;
        BigDecimal tax = BigDecimal.ZERO;
        // 税率表（年累计应税所得）
        // 税率表（年累计应税所得）：{上限, 税率, 速算扣除数}
        Object[][] brackets = {
                {36000L,  0.03,  0L},
                {144000L, 0.10,  2520L},
                {300000L, 0.20,  16920L},
                {420000L, 0.25,  31920L},
                {660000L, 0.30,  52920L},
                {960000L, 0.35,  85920L},
                {Long.MAX_VALUE, 0.45, 181920L}
        };
        for (Object[] b : brackets) {
            long upper = ((Number) b[0]).longValue();
            BigDecimal rate = new BigDecimal(b[1].toString());
            BigDecimal deduction = new BigDecimal(((Number) b[2]).longValue());
            if (amount.longValue() <= upper) {
                tax = tax.add(amount.multiply(rate)).subtract(deduction);
                break;
            }
            amount = amount.subtract(new BigDecimal(upper));
        }
        return tax.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    /** 根据月份推导基数生效年度（7月起效当年，6月及之前起效上一年） */
    private String determineBaseEffectiveYear(String salaryMonth) {
        int month = Integer.parseInt(salaryMonth.substring(5));
        int year = Integer.parseInt(salaryMonth.substring(0, 4));
        return month >= 7 ? String.valueOf(year) : String.valueOf(year - 1);
    }
}

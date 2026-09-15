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
@Service
@RequiredArgsConstructor
public class HrSocialCalcServiceImpl implements HrSocialCalcService {

    private final HrSocialCalcDetailMapper calcDetailMapper;
    private final com.gbi.platform.mapper.hr.HrEmployeeMapper employeeMapper;
    private final com.gbi.platform.mapper.hr.HrSalaryMonthMapper salaryMonthMapper;
    private final com.gbi.platform.mapper.hr.HrSocialParamMapper socialParamMapper;
    private final com.gbi.platform.mapper.hr.HrHousingFundConfigMapper housingFundConfigMapper;
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
        // TODO: 实现月度薪资核算逻辑
        // 1. 查询该月所有薪资档案员工
        // 2. 根据城市+险种查询对应费率
        // 3. 计算各险种个人/单位金额
        // 4. 计算个税
        // 5. 写入hr_salary_month和hr_social_calc_detail
        // 6. 审计日志
    }
}

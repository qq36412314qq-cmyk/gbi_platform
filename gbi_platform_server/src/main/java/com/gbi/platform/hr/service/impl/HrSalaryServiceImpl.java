package com.gbi.platform.hr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.hr.dto.AttendanceQueryDTO;
import com.gbi.platform.hr.dto.SalaryArchiveDTO;
import com.gbi.platform.hr.dto.SalaryMonthDTO;
import com.gbi.platform.hr.entity.*;
import com.gbi.platform.hr.mapper.*;
import com.gbi.platform.hr.service.HrSalaryService;
import com.gbi.platform.hr.vo.*;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HrSalaryServiceImpl implements HrSalaryService {

    private final HrSalaryArchiveMapper salaryArchiveMapper;
    private final HrSalaryMonthMapper salaryMonthMapper;
    private final HrEmployeeMapper employeeMapper;
    private final AuditLogUtil auditLogUtil;

    @Override
    public PageVO<HrSalaryArchiveVO> pageArchive(Long pageNum, Long pageSize, Long employeeId) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<HrSalaryArchive> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HrSalaryArchive> wrapper = new LambdaQueryWrapper<HrSalaryArchive>()
                .eq(HrSalaryArchive::getCompanyId, loginUser.getCompanyId())
                .eq(employeeId != null, HrSalaryArchive::getEmployeeId, employeeId)
                .orderByDesc(HrSalaryArchive::getCreateTime);
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
        archive.setBasicSalary(dto.getBasicSalary());
        archive.setPerformanceSalary(dto.getPerformanceSalary());
        archive.setPositionAllowance(dto.getPositionAllowance());
        archive.setOtherAllowance(dto.getOtherAllowance());
        archive.setSocialSecurityPersonal(dto.getSocialSecurityPersonal());
        archive.setHousingFundPersonal(dto.getHousingFundPersonal());
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
        archive.setSocialSecurityPersonal(dto.getSocialSecurityPersonal());
        archive.setHousingFundPersonal(dto.getHousingFundPersonal());
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
        for (Long empId : dto.getEmployeeIds()) {
            HrEmployee employee = employeeMapper.selectById(empId);
            if (employee == null || !loginUser.getCompanyId().equals(employee.getCompanyId())) continue;
            HrSalaryArchive archive = salaryArchiveMapper.selectOne(
                    new LambdaQueryWrapper<HrSalaryArchive>()
                            .eq(HrSalaryArchive::getEmployeeId, empId)
                            .eq(HrSalaryArchive::getCompanyId, loginUser.getCompanyId()));
            HrSalaryMonth month = new HrSalaryMonth();
            month.setCompanyId(loginUser.getCompanyId());
            month.setEmployeeId(empId);
            month.setEmployeeName(employee.getName());
            month.setSalaryMonth(dto.getSalaryMonth());
            month.setBasicSalary(archive != null ? archive.getBasicSalary() : employee.getBasicSalary());
            month.setPerformanceSalary(archive != null ? archive.getPerformanceSalary() : BigDecimal.ZERO);
            month.setAllowanceAmount(archive != null ? archive.getPositionAllowance().add(archive.getOtherAllowance()) : BigDecimal.ZERO);
            month.setSocialSecurity(archive != null ? archive.getSocialSecurityPersonal() : BigDecimal.ZERO);
            month.setHousingFund(archive != null ? archive.getHousingFundPersonal() : BigDecimal.ZERO);
            month.setGrossAmount(month.getBasicSalary().add(month.getPerformanceSalary()).add(month.getAllowanceAmount()));
            month.setNetAmount(month.getGrossAmount().subtract(month.getSocialSecurity()).subtract(month.getHousingFund()));
            month.setPayStatus(0);
            month.setCreateBy(loginUser.getUserId());
            salaryMonthMapper.insert(month);
        }
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
        vo.setBasicSalary(entity.getBasicSalary());
        vo.setPerformanceSalary(entity.getPerformanceSalary());
        vo.setPositionAllowance(entity.getPositionAllowance());
        vo.setOtherAllowance(entity.getOtherAllowance());
        vo.setSocialSecurityPersonal(entity.getSocialSecurityPersonal());
        vo.setHousingFundPersonal(entity.getHousingFundPersonal());
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




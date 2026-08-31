package com.gbi.platform.hr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.hr.dto.EmployeeDTO;
import com.gbi.platform.hr.entity.HrEmployee;
import com.gbi.platform.hr.mapper.HrEmployeeMapper;
import com.gbi.platform.hr.service.HrEmployeeService;
import com.gbi.platform.hr.vo.HrEmployeeVO;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.PageVO;
import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HrEmployeeServiceImpl implements HrEmployeeService {

    private final HrEmployeeMapper employeeMapper;
    private final AuditLogUtil auditLogUtil;

    @Override
    public PageVO<HrEmployeeVO> page(Long pageNum, Long pageSize, String name, String employeeNo, Integer employeeStatus) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<HrEmployee> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HrEmployee> wrapper = new LambdaQueryWrapper<HrEmployee>()
                .eq(HrEmployee::getCompanyId, loginUser.getCompanyId())
                .like(StringUtils.hasText(name), HrEmployee::getName, name)
                .like(StringUtils.hasText(employeeNo), HrEmployee::getEmployeeNo, employeeNo)
                .eq(employeeStatus != null, HrEmployee::getEmployeeStatus, employeeStatus)
                .orderByDesc(HrEmployee::getCreateTime);
        Page<HrEmployee> result = employeeMapper.selectPage(page, wrapper);
        List<HrEmployeeVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public HrEmployeeVO get(Long id) {
        HrEmployee entity = employeeMapper.selectById(id);
        if (entity == null) throw new BizException("员工不存在");
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(EmployeeDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        HrEmployee employee = new HrEmployee();
        employee.setCompanyId(loginUser.getCompanyId());
        employee.setEmployeeNo(dto.getEmployeeNo());
        employee.setName(dto.getName());
        employee.setIdCardNo(dto.getIdCardNo());
        employee.setPhone(dto.getPhone());
        employee.setEmail(dto.getEmail());
        employee.setGender(dto.getGender());
        employee.setBirthdate(dto.getBirthdate());
        employee.setEntryDate(dto.getEntryDate());
        employee.setEmploymentType(dto.getEmploymentType() != null ? dto.getEmploymentType() : 1);
        employee.setEmployeeStatus(CommonConst.STATUS_ENABLED);
        employee.setOrgId(dto.getOrgId());
        employee.setPostId(dto.getPostId());
        employee.setBankAccount(dto.getBankAccount());
        employee.setSocialSecurityBase(dto.getSocialSecurityBase());
        employee.setBasicSalary(dto.getBasicSalary());
        employee.setRemark(dto.getRemark());
        employee.setCreateBy(loginUser.getUserId());
        employeeMapper.insert(employee);
        auditLogUtil.record(CommonConst.MODULE_HR_EMPLOYEE, CommonConst.OPER_TYPE_ADD,
                String.valueOf(employee.getId()), null, employee);
        return employee.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(EmployeeDTO dto) {
        if (dto.getId() == null) throw new BizException("员工ID不能为空");
        HrEmployee oldEmployee = employeeMapper.selectById(dto.getId());
        if (oldEmployee == null) throw new BizException("员工不存在");
        HrEmployee employee = new HrEmployee();
        employee.setId(dto.getId());
        employee.setName(dto.getName());
        employee.setIdCardNo(dto.getIdCardNo());
        employee.setPhone(dto.getPhone());
        employee.setEmail(dto.getEmail());
        employee.setGender(dto.getGender());
        employee.setBirthdate(dto.getBirthdate());
        employee.setEntryDate(dto.getEntryDate());
        employee.setEmploymentType(dto.getEmploymentType());
        employee.setOrgId(dto.getOrgId());
        employee.setPostId(dto.getPostId());
        employee.setBankAccount(dto.getBankAccount());
        employee.setSocialSecurityBase(dto.getSocialSecurityBase());
        employee.setBasicSalary(dto.getBasicSalary());
        employee.setRemark(dto.getRemark());
        employee.setUpdateBy(UserContext.getLoginUser().getUserId());
        employeeMapper.updateById(employee);
        auditLogUtil.record(CommonConst.MODULE_HR_EMPLOYEE, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(dto.getId()), oldEmployee, employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HrEmployee employee = employeeMapper.selectById(id);
        if (employee == null) throw new BizException("员工不存在");
        employeeMapper.deleteById(id);
        auditLogUtil.record(CommonConst.MODULE_HR_EMPLOYEE, CommonConst.OPER_TYPE_DELETE,
                String.valueOf(id), employee, null);
    }

    @Override
    public List<HrEmployeeVO> export(List<Long> ids) {
        List<HrEmployee> list = employeeMapper.selectBatchIds(ids);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createFromEntry(Long entryApplyId) {
        // 由HrTransferService调用，入职审批通过后创建员工档案
        log.info("createFromEntry called with entryApplyId={}", entryApplyId);
    }

    private HrEmployeeVO toVO(HrEmployee entity) {
        HrEmployeeVO vo = new HrEmployeeVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setUserId(entity.getUserId());
        vo.setEmployeeNo(entity.getEmployeeNo());
        vo.setName(entity.getName());
        vo.setIdCardNo(decodeSensitive(entity.getIdCardNo()));
        vo.setPhone(decodeSensitive(entity.getPhone()));
        vo.setEmail(entity.getEmail());
        vo.setGender(entity.getGender());
        vo.setGenderText(entity.getGender() != null ? (entity.getGender() == 1 ? "男" : "女") : null);
        vo.setBirthdate(entity.getBirthdate());
        vo.setEntryDate(entity.getEntryDate());
        vo.setRegularDate(entity.getRegularDate());
        vo.setResignDate(entity.getResignDate());
        vo.setEmploymentType(entity.getEmploymentType());
        vo.setEmploymentTypeText(toEmploymentTypeText(entity.getEmploymentType()));
        vo.setEmployeeStatus(entity.getEmployeeStatus());
        vo.setEmployeeStatusText(toEmployeeStatusText(entity.getEmployeeStatus()));
        vo.setOrgId(entity.getOrgId());
        vo.setPostId(entity.getPostId());
        vo.setPostLevel(entity.getPostLevel());
        vo.setOrgName(entity.getOrgName());
        vo.setSupervisorId(entity.getSupervisorId());
        vo.setBankAccount(decodeSensitive(entity.getBankAccount()));
        vo.setSocialSecurityBase(entity.getSocialSecurityBase());
        vo.setBasicSalary(entity.getBasicSalary());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private String toEmployeeStatusText(Integer status) {
        if (status == null) return null;
        switch (status) {
            case 0: return "待入职";
            case 1: return "在职";
            case 2: return "试用期";
            case 3: return "离职";
            case 4: return "终止合同";
            default: return "未知";
        }
    }

    private String toEmploymentTypeText(Integer type) {
        if (type == null) return null;
        switch (type) {
            case 1: return "正式";
            case 2: return "试用期";
            case 3: return "劳务派遣";
            case 4: return "临时工";
            default: return "未知";
        }
    }

    private String decodeSensitive(String value) {
        if (value == null) return null;
        return value;
    }
}


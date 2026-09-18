package com.gbi.platform.service.impl.hr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.hr.EmployeeDTO;
import com.gbi.platform.dto.hr.EduExpDTO;
import com.gbi.platform.dto.hr.WorkExpDTO;
import com.gbi.platform.entity.hr.HrEmployee;
import com.gbi.platform.entity.hr.HrEntryApply;
import com.gbi.platform.entity.hr.HrEmployeeEduExp;
import com.gbi.platform.entity.hr.HrEmployeeWorkExp;
import com.gbi.platform.mapper.hr.HrEmployeeMapper;
import com.gbi.platform.mapper.hr.HrEntryApplyMapper;
import com.gbi.platform.mapper.hr.HrEmployeeEduExpMapper;
import com.gbi.platform.mapper.hr.HrEmployeeWorkExpMapper;
import com.gbi.platform.mapper.SysUserMapper;
import com.gbi.platform.entity.SysUser;
import com.gbi.platform.service.hr.HrEmployeeService;
import com.gbi.platform.vo.hr.EduExpVO;
import com.gbi.platform.vo.hr.HrEmployeeVO;
import com.gbi.platform.vo.hr.WorkExpVO;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作经历和学业经历JSON格式示例：
 * {
 *   "workExps": [
 *     {
 *       "companyName": "某某科技有限公司",
 *       "position": "软件工程师",
 *       "department": "研发部",
 *       "startDate": "2020-03-01",
 *       "endDate": "2023-06-30",
 *       "isCurrent": 0,
 *       "reasonForLeaving": "个人发展",
 *       "remark": ""
 *     }
 *   ],
 *   "eduExps": [
 *     {
 *       "schoolName": "某某大学",
 *       "degree": "本科",
 *       "major": "计算机科学与技术",
 *       "educationLevel": "全日制",
 *       "startDate": "2014-09-01",
 *       "graduationDate": "2018-06-30",
 *       "isGraduated": 1,
 *       "certificateNo": "12345678",
 *       "remark": ""
 *     }
 *   ]
 * }
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrEmployeeServiceImpl implements HrEmployeeService {

    private final HrEmployeeMapper employeeMapper;
    private final HrEntryApplyMapper entryApplyMapper;
    private final HrEmployeeWorkExpMapper workExpMapper;
    private final HrEmployeeEduExpMapper eduExpMapper;
    private final AuditLogUtil auditLogUtil;
    private final ObjectMapper objectMapper;
    private final SysUserMapper sysUserMapper;

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
        workExpMapper.delete(new LambdaQueryWrapper<HrEmployeeWorkExp>()
                .eq(HrEmployeeWorkExp::getEmployeeId, id));
        eduExpMapper.delete(new LambdaQueryWrapper<HrEmployeeEduExp>()
                .eq(HrEmployeeEduExp::getEmployeeId, id));
        employeeMapper.deleteById(id);
        auditLogUtil.record(CommonConst.MODULE_HR_EMPLOYEE, CommonConst.OPER_TYPE_DELETE,
                String.valueOf(id), employee, null);
    }

    @Override
    public List<HrEmployeeVO> export(List<Long> ids) {
        List<HrEmployee> list = employeeMapper.selectBatchIds(ids);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 入职申请审批通过后，从申请单数据创建员工档案
     * 同时写入工作经历和学业经历子表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createFromEntry(Long entryApplyId) {
        HrEntryApply apply = entryApplyMapper.selectById(entryApplyId);
        if (apply == null) {
            log.warn("createFromEntry: 入职申请不存在，ID={}", entryApplyId);
            return;
        }
        if (!Integer.valueOf(CommonConst.APPLY_STATUS_PASS).equals(apply.getStatus())) {
            log.warn("createFromEntry: 入职申请尚未审批通过，ID={} 当前状态={}", entryApplyId, String.valueOf(apply.getStatus()));
            return;
        }
        // 防重：同一 company_id + employee_no 已存在则跳过
        LambdaQueryWrapper<HrEmployee> dupCheck = new LambdaQueryWrapper<HrEmployee>()
                .eq(HrEmployee::getCompanyId, apply.getCompanyId())
                .eq(HrEmployee::getEmployeeNo, apply.getEmployeeNo());
        Long count = employeeMapper.selectCount(dupCheck);
        if (count != null && count > 0) {
            log.info("createFromEntry: 员工档案已存在，跳过建档：company_id={}, employee_no={}", apply.getCompanyId(), apply.getEmployeeNo());
            return;
        }
        HrEmployee employee = new HrEmployee();
        employee.setCompanyId(apply.getCompanyId());
        employee.setEmployeeNo(apply.getEmployeeNo());
        employee.setName(apply.getName());
        employee.setIdCardNo(apply.getIdCardNo());
        employee.setPhone(apply.getPhone());
        employee.setGender(apply.getGender());
        employee.setBirthdate(apply.getBirthdate());
        employee.setEntryDate(apply.getEntryDate());
        employee.setEmploymentType(apply.getEmploymentType() != null ? apply.getEmploymentType() : 1);
        employee.setEmployeeStatus(CommonConst.STATUS_ENABLED);
        employee.setOrgId(apply.getOrgId());
        employee.setPostId(apply.getPostId());
        employee.setBankAccount(apply.getBankAccount());
        employee.setBasicSalary(apply.getBasicSalary());
        employee.setRemark(apply.getRemark());
        employee.setCreateBy(apply.getCreateBy());
        employeeMapper.insert(employee);
        auditLogUtil.record(CommonConst.MODULE_HR_EMPLOYEE, CommonConst.OPER_TYPE_ADD,
                String.valueOf(employee.getId()), null, employee);
        log.info("入职审批通过，自动建档成功：employeeId={}, employeeNo={}, name={}",
                employee.getId(), employee.getEmployeeNo(), employee.getName());

        // 写入工作经历和学业经历
        writeExperienceData(apply, employee.getId());

        // 自动创建系统账号
        if (Integer.valueOf(1).equals(apply.getAutoCreateUser())) {
            createSysUser(apply, employee);
        }
    }

    /**
     * 根据入职申请自动创建系统登录账号
     */
    private void createSysUser(HrEntryApply apply, HrEmployee employee) {
        try {
            SysUser user = new SysUser();
            user.setCompanyId(apply.getCompanyId());
            user.setUsername(apply.getEmployeeNo()); // 工号作为登录账号
            user.setRealName(apply.getName());
            user.setPhone(apply.getPhone());
            user.setStatus(CommonConst.STATUS_ENABLED);
            user.setEmployeeId(employee.getId()); // 关联员工档案
            // 默认密码：123456（加密存储）
            user.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi");
            sysUserMapper.insert(user);
            employee.setUserId(user.getId());
            employeeMapper.updateById(employee);
            log.info("自动创建系统账号成功：userId={}, employeeId={}, employeeNo={}", user.getId(), employee.getId(), apply.getEmployeeNo());
        } catch (Exception e) {
            log.error("自动创建系统账号失败：entryApplyId={}, employeeId={}, error={}", apply.getId(), employee.getId(), e.getMessage(), e);
        }
    }

    /**
     * 解析入职申请中的JSON经历数据并写入子表
     */
    private void writeExperienceData(HrEntryApply apply, Long employeeId) {
        String experienceData = apply.getExperienceData();
        if (!StringUtils.hasText(experienceData)) {
            log.info("createFromEntry: 入职申请无经历数据，跳过：entryApplyId={}", apply.getId());
            return;
        }
        try {
            Map<String, Object> dataMap = objectMapper.readValue(experienceData, new TypeReference<Map<String, Object>>() {});

            // 写入工作经历
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> workExps = (List<Map<String, Object>>) dataMap.get("workExps");
            if (workExps != null && !workExps.isEmpty()) {
                Long companyId = apply.getCompanyId();
                Long createBy = apply.getCreateBy();
                for (Map<String, Object> item : workExps) {
                    HrEmployeeWorkExp exp = new HrEmployeeWorkExp();
                    exp.setCompanyId(companyId);
                    exp.setEmployeeId(employeeId);
                    exp.setCompanyName((String) item.get("companyName"));
                    exp.setPosition((String) item.get("position"));
                    exp.setDepartment((String) item.get("department"));
                    exp.setStartDate(parseLocalDate(item.get("startDate")));
                    exp.setEndDate(parseLocalDate(item.get("endDate")));
                    exp.setIsCurrent(item.get("isCurrent") != null ? ((Number) item.get("isCurrent")).intValue() : 0);
                    exp.setReasonForLeaving((String) item.get("reasonForLeaving"));
                    exp.setRemark((String) item.get("remark"));
                    exp.setCreateBy(createBy);
                    workExpMapper.insert(exp);
                }
                log.info("createFromEntry: 写入工作经历成功：employeeId={}, count={}", employeeId, workExps.size());
            }

            // 写入学业经历
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> eduExps = (List<Map<String, Object>>) dataMap.get("eduExps");
            if (eduExps != null && !eduExps.isEmpty()) {
                Long companyId = apply.getCompanyId();
                Long createBy = apply.getCreateBy();
                for (Map<String, Object> item : eduExps) {
                    HrEmployeeEduExp exp = new HrEmployeeEduExp();
                    exp.setCompanyId(companyId);
                    exp.setEmployeeId(employeeId);
                    exp.setSchoolName((String) item.get("schoolName"));
                    exp.setDegree((String) item.get("degree"));
                    exp.setMajor((String) item.get("major"));
                    exp.setEducationLevel((String) item.get("educationLevel"));
                    exp.setStartDate(parseLocalDate(item.get("startDate")));
                    exp.setGraduationDate(parseLocalDate(item.get("graduationDate")));
                    exp.setIsGraduated(item.get("isGraduated") != null ? ((Number) item.get("isGraduated")).intValue() : 0);
                    exp.setCertificateNo((String) item.get("certificateNo"));
                    exp.setRemark((String) item.get("remark"));
                    exp.setCreateBy(createBy);
                    eduExpMapper.insert(exp);
                }
                log.info("createFromEntry: 写入学业经历成功：employeeId={}, count={}", employeeId, eduExps.size());
            }
        } catch (Exception e) {
            log.error("createFromEntry: 解析经历数据失败：entryApplyId={}, error={}", apply.getId(), e.getMessage(), e);
            // JSON解析失败不影响员工档案创建，仅记录日志
        }
    }

    /**
     * 安全解析日期字符串
     */
    private java.time.LocalDate parseLocalDate(Object value) {
        if (value == null) return null;
        String str = value.toString();
        if (!StringUtils.hasText(str)) return null;
        try {
            return java.time.LocalDate.parse(str);
        } catch (Exception e) {
            log.warn("parseLocalDate: 日期解析失败: {}", str);
            return null;
        }
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

    private WorkExpVO toWorkExpVO(HrEmployeeWorkExp entity) {
        WorkExpVO vo = new WorkExpVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setEmployeeId(entity.getEmployeeId());
        vo.setCompanyName(entity.getCompanyName());
        vo.setPosition(entity.getPosition());
        vo.setDepartment(entity.getDepartment());
        vo.setStartDate(entity.getStartDate());
        vo.setEndDate(entity.getEndDate());
        vo.setIsCurrent(entity.getIsCurrent());
        vo.setIsCurrentText(entity.getIsCurrent() != null && entity.getIsCurrent() == 1 ? "在职" : "离职");
        vo.setReasonForLeaving(entity.getReasonForLeaving());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private EduExpVO toEduExpVO(HrEmployeeEduExp entity) {
        EduExpVO vo = new EduExpVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setEmployeeId(entity.getEmployeeId());
        vo.setSchoolName(entity.getSchoolName());
        vo.setDegree(entity.getDegree());
        vo.setMajor(entity.getMajor());
        vo.setEducationLevel(entity.getEducationLevel());
        vo.setStartDate(entity.getStartDate());
        vo.setGraduationDate(entity.getGraduationDate());
        vo.setIsGraduated(entity.getIsGraduated());
        vo.setIsGraduatedText(entity.getIsGraduated() != null && entity.getIsGraduated() == 1 ? "已毕业" : "在读");
        vo.setCertificateNo(entity.getCertificateNo());
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
    // ==================== 工作经历 ====================
    @Override
    public List<WorkExpVO> getWorkExps(Long employeeId) {
        List<HrEmployeeWorkExp> list = workExpMapper.selectList(
                new LambdaQueryWrapper<HrEmployeeWorkExp>()
                        .eq(HrEmployeeWorkExp::getEmployeeId, employeeId)
                        .orderByDesc(HrEmployeeWorkExp::getCreateTime));
        return list.stream().map(this::toWorkExpVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addWorkExp(WorkExpDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        HrEmployeeWorkExp exp = new HrEmployeeWorkExp();
        exp.setCompanyId(loginUser.getCompanyId());
        exp.setEmployeeId(dto.getEmployeeId());
        exp.setCompanyName(dto.getCompanyName());
        exp.setPosition(dto.getPosition());
        exp.setDepartment(dto.getDepartment());
        exp.setStartDate(dto.getStartDate());
        exp.setEndDate(dto.getEndDate());
        exp.setIsCurrent(dto.getIsCurrent() != null ? dto.getIsCurrent() : 0);
        exp.setReasonForLeaving(dto.getReasonForLeaving());
        exp.setRemark(dto.getRemark());
        exp.setCreateBy(loginUser.getUserId());
        workExpMapper.insert(exp);
        auditLogUtil.record(CommonConst.MODULE_HR_WORK_EXP, CommonConst.OPER_TYPE_ADD,
                String.valueOf(exp.getId()), null, exp);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkExp(WorkExpDTO dto) {
        if (dto.getId() == null) throw new BizException("工作经历ID不能为空");
        HrEmployeeWorkExp old = workExpMapper.selectById(dto.getId());
        if (old == null) throw new BizException("工作经历不存在");
        HrEmployeeWorkExp exp = new HrEmployeeWorkExp();
        exp.setId(dto.getId());
        exp.setCompanyName(dto.getCompanyName());
        exp.setPosition(dto.getPosition());
        exp.setDepartment(dto.getDepartment());
        exp.setStartDate(dto.getStartDate());
        exp.setEndDate(dto.getEndDate());
        exp.setIsCurrent(dto.getIsCurrent());
        exp.setReasonForLeaving(dto.getReasonForLeaving());
        exp.setRemark(dto.getRemark());
        exp.setUpdateBy(UserContext.getLoginUser().getUserId());
        workExpMapper.updateById(exp);
        auditLogUtil.record(CommonConst.MODULE_HR_WORK_EXP, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(dto.getId()), old, exp);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkExp(Long id) {
        HrEmployeeWorkExp exp = workExpMapper.selectById(id);
        if (exp == null) throw new BizException("工作经历不存在");
        workExpMapper.deleteById(id);
        auditLogUtil.record(CommonConst.MODULE_HR_WORK_EXP, CommonConst.OPER_TYPE_DELETE,
                String.valueOf(id), exp, null);
    }

    // ==================== 学业经历 ====================
    @Override
    public List<EduExpVO> getEduExps(Long employeeId) {
        List<HrEmployeeEduExp> list = eduExpMapper.selectList(
                new LambdaQueryWrapper<HrEmployeeEduExp>()
                        .eq(HrEmployeeEduExp::getEmployeeId, employeeId)
                        .orderByDesc(HrEmployeeEduExp::getCreateTime));
        return list.stream().map(this::toEduExpVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addEduExp(EduExpDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        HrEmployeeEduExp exp = new HrEmployeeEduExp();
        exp.setCompanyId(loginUser.getCompanyId());
        exp.setEmployeeId(dto.getEmployeeId());
        exp.setSchoolName(dto.getSchoolName());
        exp.setDegree(dto.getDegree());
        exp.setMajor(dto.getMajor());
        exp.setEducationLevel(dto.getEducationLevel());
        exp.setStartDate(dto.getStartDate());
        exp.setGraduationDate(dto.getGraduationDate());
        exp.setIsGraduated(dto.getIsGraduated() != null ? dto.getIsGraduated() : 0);
        exp.setCertificateNo(dto.getCertificateNo());
        exp.setRemark(dto.getRemark());
        exp.setCreateBy(loginUser.getUserId());
        eduExpMapper.insert(exp);
        auditLogUtil.record(CommonConst.MODULE_HR_EDU_EXP, CommonConst.OPER_TYPE_ADD,
                String.valueOf(exp.getId()), null, exp);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEduExp(EduExpDTO dto) {
        if (dto.getId() == null) throw new BizException("学业经历ID不能为空");
        HrEmployeeEduExp old = eduExpMapper.selectById(dto.getId());
        if (old == null) throw new BizException("学业经历不存在");
        HrEmployeeEduExp exp = new HrEmployeeEduExp();
        exp.setId(dto.getId());
        exp.setSchoolName(dto.getSchoolName());
        exp.setDegree(dto.getDegree());
        exp.setMajor(dto.getMajor());
        exp.setEducationLevel(dto.getEducationLevel());
        exp.setStartDate(dto.getStartDate());
        exp.setGraduationDate(dto.getGraduationDate());
        exp.setIsGraduated(dto.getIsGraduated());
        exp.setCertificateNo(dto.getCertificateNo());
        exp.setRemark(dto.getRemark());
        exp.setUpdateBy(UserContext.getLoginUser().getUserId());
        eduExpMapper.updateById(exp);
        auditLogUtil.record(CommonConst.MODULE_HR_EDU_EXP, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(dto.getId()), old, exp);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEduExp(Long id) {
        HrEmployeeEduExp exp = eduExpMapper.selectById(id);
        if (exp == null) throw new BizException("学业经历不存在");
        eduExpMapper.deleteById(id);
        auditLogUtil.record(CommonConst.MODULE_HR_EDU_EXP, CommonConst.OPER_TYPE_DELETE,
                String.valueOf(id), exp, null);
    }
}

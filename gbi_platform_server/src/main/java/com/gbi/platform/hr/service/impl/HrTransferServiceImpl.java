package com.gbi.platform.hr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.hr.dto.*;
import com.gbi.platform.hr.entity.*;
import com.gbi.platform.hr.mapper.*;
import com.gbi.platform.hr.service.HrTransferService;
import com.gbi.platform.hr.vo.*;
import com.gbi.platform.service.FlowEngineService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HrTransferServiceImpl implements HrTransferService {

    private final HrEntryApplyMapper entryApplyMapper;
    private final HrRegularApplyMapper regularApplyMapper;
    private final HrTransferApplyMapper transferApplyMapper;
    private final HrResignApplyMapper resignApplyMapper;
    private final HrEmployeeMapper employeeMapper;
    private final FlowEngineService flowEngineService;
    private final AuditLogUtil auditLogUtil;

    @Override
    public PageVO<HrEntryApplyVO> pageEntry(Long pageNum, Long pageSize, Integer status) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<HrEntryApply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HrEntryApply> wrapper = new LambdaQueryWrapper<HrEntryApply>()
                .eq(HrEntryApply::getCompanyId, loginUser.getCompanyId())
                .eq(status != null, HrEntryApply::getStatus, status)
                .orderByDesc(HrEntryApply::getCreateTime);
        Page<HrEntryApply> result = entryApplyMapper.selectPage(page, wrapper);
        List<HrEntryApplyVO> voList = result.getRecords().stream().map(this::toEntryVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitEntry(EntryApplyDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        HrEntryApply apply = new HrEntryApply();
        apply.setCompanyId(loginUser.getCompanyId());
        apply.setEmployeeNo(dto.getEmployeeNo());
        apply.setName(dto.getName());
        apply.setIdCardNo(dto.getIdCardNo());
        apply.setPhone(dto.getPhone());
        apply.setGender(dto.getGender());
        apply.setBirthdate(dto.getBirthdate());
        apply.setEntryDate(dto.getEntryDate());
        apply.setEmploymentType(dto.getEmploymentType() != null ? dto.getEmploymentType() : 1);
        apply.setOrgId(dto.getOrgId());
        apply.setPostId(dto.getPostId());
        apply.setBasicSalary(dto.getBasicSalary());
        apply.setBankAccount(dto.getBankAccount());
        apply.setAutoCreateUser(dto.getAutoCreateUser() != null ? dto.getAutoCreateUser() : 0);
        apply.setStatus(CommonConst.APPLY_STATUS_DRAFT);
        apply.setRemark(dto.getRemark());
        apply.setCreateBy(loginUser.getUserId());
        entryApplyMapper.insert(apply);

        Long instanceId = flowEngineService.submit(CommonConst.FLOW_DEF_HR_ENTRY, "hr_entry_apply",
                String.valueOf(apply.getId()),
                dto.getName() + " 入职申请");
        apply.setFlowInstanceId(instanceId);
        apply.setStatus(CommonConst.APPLY_STATUS_AUDITING);
        entryApplyMapper.updateById(apply);
        auditLogUtil.record(CommonConst.MODULE_HR_TRANSFER, CommonConst.OPER_TYPE_SUBMIT,
                String.valueOf(apply.getId()), null, apply);
        return apply.getId();
    }

    @Override
    public void revokeEntry(Long id) {
        HrEntryApply apply = entryApplyMapper.selectById(id);
        if (apply == null) throw new BizException("入职申请不存在");
        if (apply.getStatus() != CommonConst.APPLY_STATUS_DRAFT && apply.getStatus() != CommonConst.APPLY_STATUS_AUDITING) {
            throw new BizException("只有草稿或审批中的申请可以撤回");
        }
        apply.setStatus(CommonConst.APPLY_STATUS_VOID);
        entryApplyMapper.updateById(apply);
    }

    @Override
    public PageVO<HrRegularApplyVO> pageRegular(Long pageNum, Long pageSize, Integer status) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<HrRegularApply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HrRegularApply> wrapper = new LambdaQueryWrapper<HrRegularApply>()
                .eq(HrRegularApply::getCompanyId, loginUser.getCompanyId())
                .eq(status != null, HrRegularApply::getStatus, status)
                .orderByDesc(HrRegularApply::getCreateTime);
        Page<HrRegularApply> result = regularApplyMapper.selectPage(page, wrapper);
        List<HrRegularApplyVO> voList = result.getRecords().stream().map(this::toRegularVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitRegular(RegularApplyDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        HrEmployee employee = employeeMapper.selectOne(new LambdaQueryWrapper<HrEmployee>()
                .eq(HrEmployee::getId, dto.getEmployeeId())
                .eq(HrEmployee::getCompanyId, loginUser.getCompanyId()));
        if (employee == null) throw new BizException("员工不存在");
        HrRegularApply apply = new HrRegularApply();
        apply.setCompanyId(loginUser.getCompanyId());
        apply.setEmployeeId(dto.getEmployeeId());
        apply.setEmployeeName(employee.getName());
        apply.setRegularDate(dto.getRegularDate());
        apply.setRemark(dto.getRemark());
        apply.setStatus(CommonConst.APPLY_STATUS_DRAFT);
        apply.setCreateBy(loginUser.getUserId());
        regularApplyMapper.insert(apply);
        Long instanceId = flowEngineService.submit(CommonConst.FLOW_DEF_HR_REGULAR, "hr_regular_apply",
                String.valueOf(apply.getId()), employee.getName() + " 转正申请");
        apply.setFlowInstanceId(instanceId);
        apply.setStatus(CommonConst.APPLY_STATUS_AUDITING);
        regularApplyMapper.updateById(apply);
        return apply.getId();
    }

    @Override
    public void revokeRegular(Long id) {
        HrRegularApply apply = regularApplyMapper.selectById(id);
        if (apply == null) throw new BizException("转正申请不存在");
        if (apply.getStatus() != CommonConst.APPLY_STATUS_DRAFT && apply.getStatus() != CommonConst.APPLY_STATUS_AUDITING) {
            throw new BizException("只有草稿或审批中的申请可以撤回");
        }
        apply.setStatus(CommonConst.APPLY_STATUS_VOID);
        regularApplyMapper.updateById(apply);
    }

    @Override
    public PageVO<HrTransferApplyVO> pageTransfer(Long pageNum, Long pageSize, Integer status) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<HrTransferApply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HrTransferApply> wrapper = new LambdaQueryWrapper<HrTransferApply>()
                .eq(HrTransferApply::getCompanyId, loginUser.getCompanyId())
                .eq(status != null, HrTransferApply::getStatus, status)
                .orderByDesc(HrTransferApply::getCreateTime);
        Page<HrTransferApply> result = transferApplyMapper.selectPage(page, wrapper);
        List<HrTransferApplyVO> voList = result.getRecords().stream().map(this::toTransferVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitTransfer(TransferApplyDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        HrEmployee employee = employeeMapper.selectOne(new LambdaQueryWrapper<HrEmployee>()
                .eq(HrEmployee::getId, dto.getEmployeeId())
                .eq(HrEmployee::getCompanyId, loginUser.getCompanyId()));
        if (employee == null) throw new BizException("员工不存在");
        HrTransferApply apply = new HrTransferApply();
        apply.setCompanyId(loginUser.getCompanyId());
        apply.setEmployeeId(dto.getEmployeeId());
        apply.setEmployeeName(employee.getName());
        apply.setOldOrgId(employee.getOrgId());
        apply.setOldPostId(employee.getPostId());
        apply.setNewOrgId(dto.getNewOrgId());
        apply.setNewPostId(dto.getNewPostId());
        apply.setTransferDate(dto.getTransferDate());
        apply.setReason(dto.getReason());
        apply.setStatus(CommonConst.APPLY_STATUS_DRAFT);
        apply.setCreateBy(loginUser.getUserId());
        transferApplyMapper.insert(apply);
        Long instanceId = flowEngineService.submit(CommonConst.FLOW_DEF_HR_TRANSFER, "hr_transfer_apply",
                String.valueOf(apply.getId()), employee.getName() + " 调岗申请");
        apply.setFlowInstanceId(instanceId);
        apply.setStatus(CommonConst.APPLY_STATUS_AUDITING);
        transferApplyMapper.updateById(apply);
        return apply.getId();
    }

    @Override
    public void revokeTransfer(Long id) {
        HrTransferApply apply = transferApplyMapper.selectById(id);
        if (apply == null) throw new BizException("调岗申请不存在");
        if (apply.getStatus() != CommonConst.APPLY_STATUS_DRAFT && apply.getStatus() != CommonConst.APPLY_STATUS_AUDITING) {
            throw new BizException("只有草稿或审批中的申请可以撤回");
        }
        apply.setStatus(CommonConst.APPLY_STATUS_VOID);
        transferApplyMapper.updateById(apply);
    }

    @Override
    public PageVO<HrResignApplyVO> pageResign(Long pageNum, Long pageSize, Integer status) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<HrResignApply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HrResignApply> wrapper = new LambdaQueryWrapper<HrResignApply>()
                .eq(HrResignApply::getCompanyId, loginUser.getCompanyId())
                .eq(status != null, HrResignApply::getStatus, status)
                .orderByDesc(HrResignApply::getCreateTime);
        Page<HrResignApply> result = resignApplyMapper.selectPage(page, wrapper);
        List<HrResignApplyVO> voList = result.getRecords().stream().map(this::toResignVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitResign(ResignApplyDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        HrEmployee employee = employeeMapper.selectOne(new LambdaQueryWrapper<HrEmployee>()
                .eq(HrEmployee::getId, dto.getEmployeeId())
                .eq(HrEmployee::getCompanyId, loginUser.getCompanyId()));
        if (employee == null) throw new BizException("员工不存在");
        HrResignApply apply = new HrResignApply();
        apply.setCompanyId(loginUser.getCompanyId());
        apply.setEmployeeId(dto.getEmployeeId());
        apply.setEmployeeName(employee.getName());
        apply.setResignDate(dto.getResignDate());
        apply.setResignType(dto.getResignType());
        apply.setReason(dto.getReason());
        apply.setHandoverRemark(dto.getHandoverRemark());
        apply.setStatus(CommonConst.APPLY_STATUS_DRAFT);
        apply.setCreateBy(loginUser.getUserId());
        resignApplyMapper.insert(apply);
        Long instanceId = flowEngineService.submit(CommonConst.FLOW_DEF_HR_RESIGN, "hr_resign_apply",
                String.valueOf(apply.getId()), employee.getName() + " 离职申请");
        apply.setFlowInstanceId(instanceId);
        apply.setStatus(CommonConst.APPLY_STATUS_AUDITING);
        resignApplyMapper.updateById(apply);
        auditLogUtil.record(CommonConst.MODULE_HR_TRANSFER, CommonConst.OPER_TYPE_SUBMIT,
                String.valueOf(apply.getId()), null, apply);
        return apply.getId();
    }

    @Override
    public void revokeResign(Long id) {
        HrResignApply apply = resignApplyMapper.selectById(id);
        if (apply == null) throw new BizException("离职申请不存在");
        if (apply.getStatus() != CommonConst.APPLY_STATUS_DRAFT && apply.getStatus() != CommonConst.APPLY_STATUS_AUDITING) {
            throw new BizException("只有草稿或审批中的申请可以撤回");
        }
        apply.setStatus(CommonConst.APPLY_STATUS_VOID);
        resignApplyMapper.updateById(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onEntryApproved(Long entryApplyId) {
        HrEntryApply apply = entryApplyMapper.selectById(entryApplyId);
        if (apply == null) throw new BizException("入职申请不存在");
        HrEmployee employee = new HrEmployee();
        employee.setCompanyId(apply.getCompanyId());
        employee.setEmployeeNo(apply.getEmployeeNo());
        employee.setName(apply.getName());
        employee.setIdCardNo(apply.getIdCardNo());
        employee.setPhone(apply.getPhone());
        employee.setGender(apply.getGender());
        employee.setBirthdate(apply.getBirthdate());
        employee.setEntryDate(apply.getEntryDate());
        employee.setEmploymentType(apply.getEmploymentType());
        employee.setEmployeeStatus(CommonConst.STATUS_ENABLED);
        employee.setOrgId(apply.getOrgId());
        employee.setPostId(apply.getPostId());
        employee.setBasicSalary(apply.getBasicSalary());
        employee.setBankAccount(apply.getBankAccount());
        employee.setCreateBy(apply.getCreateBy());
        employeeMapper.insert(employee);
        if (apply.getAutoCreateUser() != null && apply.getAutoCreateUser() == 1) {
            log.info("自动创建系统账号: employeeId={}, name={}", employee.getId(), apply.getName());
        }
        apply.setStatus(CommonConst.APPLY_STATUS_PASS);
        entryApplyMapper.updateById(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onRegularApproved(Long regularApplyId) {
        HrRegularApply apply = regularApplyMapper.selectById(regularApplyId);
        if (apply == null) throw new BizException("转正申请不存在");
        HrEmployee employee = employeeMapper.selectById(apply.getEmployeeId());
        if (employee != null) {
            employee.setRegularDate(apply.getRegularDate());
            employee.setEmployeeStatus(CommonConst.STATUS_ENABLED);
            employeeMapper.updateById(employee);
        }
        apply.setStatus(CommonConst.APPLY_STATUS_PASS);
        regularApplyMapper.updateById(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onTransferApproved(Long transferApplyId) {
        HrTransferApply apply = transferApplyMapper.selectById(transferApplyId);
        if (apply == null) throw new BizException("调岗申请不存在");
        HrEmployee employee = employeeMapper.selectById(apply.getEmployeeId());
        if (employee != null) {
            employee.setOrgId(apply.getNewOrgId());
            employee.setPostId(apply.getNewPostId());
            employee.setPostLevel(apply.getNewPostLevel());
            employeeMapper.updateById(employee);
        }
        apply.setStatus(CommonConst.APPLY_STATUS_PASS);
        transferApplyMapper.updateById(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onResignApproved(Long resignApplyId) {
        HrResignApply apply = resignApplyMapper.selectById(resignApplyId);
        if (apply == null) throw new BizException("离职申请不存在");
        HrEmployee employee = employeeMapper.selectById(apply.getEmployeeId());
        if (employee != null) {
            employee.setEmployeeStatus(3);
            employee.setResignDate(apply.getResignDate());
            employeeMapper.updateById(employee);
        }
        apply.setStatus(CommonConst.APPLY_STATUS_PASS);
        resignApplyMapper.updateById(apply);
    }

    private HrEntryApplyVO toEntryVO(HrEntryApply entity) {
        HrEntryApplyVO vo = new HrEntryApplyVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setEmployeeNo(entity.getEmployeeNo());
        vo.setName(entity.getName());
        vo.setIdCardNo(decodeSensitive(entity.getIdCardNo()));
        vo.setPhone(decodeSensitive(entity.getPhone()));
        vo.setGender(entity.getGender());
        vo.setBirthdate(entity.getBirthdate());
        vo.setEntryDate(entity.getEntryDate());
        vo.setEmploymentType(entity.getEmploymentType());
        vo.setOrgId(entity.getOrgId());
        vo.setPostId(entity.getPostId());
        vo.setBasicSalary(entity.getBasicSalary());
        vo.setBankAccount(decodeSensitive(entity.getBankAccount()));
        vo.setAutoCreateUser(entity.getAutoCreateUser());
        vo.setFlowInstanceId(entity.getFlowInstanceId());
        vo.setStatus(entity.getStatus());
        vo.setStatusText(toStatusText(entity.getStatus()));
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private HrRegularApplyVO toRegularVO(HrRegularApply entity) {
        HrRegularApplyVO vo = new HrRegularApplyVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setEmployeeId(entity.getEmployeeId());
        vo.setEmployeeName(entity.getEmployeeName());
        vo.setRegularDate(entity.getRegularDate());
        vo.setRemark(entity.getRemark());
        vo.setFlowInstanceId(entity.getFlowInstanceId());
        vo.setStatus(entity.getStatus());
        vo.setStatusText(toStatusText(entity.getStatus()));
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private HrTransferApplyVO toTransferVO(HrTransferApply entity) {
        HrTransferApplyVO vo = new HrTransferApplyVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setEmployeeId(entity.getEmployeeId());
        vo.setEmployeeName(entity.getEmployeeName());
        vo.setOldOrgId(entity.getOldOrgId());
        vo.setOldPostId(entity.getOldPostId());
        vo.setNewOrgId(entity.getNewOrgId());
        vo.setNewPostId(entity.getNewPostId());
        vo.setNewPostLevel(entity.getNewPostLevel());
        vo.setTransferDate(entity.getTransferDate());
        vo.setReason(entity.getReason());
        vo.setFlowInstanceId(entity.getFlowInstanceId());
        vo.setStatus(entity.getStatus());
        vo.setStatusText(toStatusText(entity.getStatus()));
        return vo;
    }

    private HrResignApplyVO toResignVO(HrResignApply entity) {
        HrResignApplyVO vo = new HrResignApplyVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setEmployeeId(entity.getEmployeeId());
        vo.setEmployeeName(entity.getEmployeeName());
        vo.setResignDate(entity.getResignDate());
        vo.setResignType(entity.getResignType());
        vo.setResignTypeText(toResignTypeText(entity.getResignType()));
        vo.setReason(entity.getReason());
        vo.setHandoverRemark(entity.getHandoverRemark());
        vo.setFlowInstanceId(entity.getFlowInstanceId());
        vo.setStatus(entity.getStatus());
        vo.setStatusText(toStatusText(entity.getStatus()));
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private String toStatusText(Integer status) {
        if (status == null) return null;
        switch (status) {
            case 0: return "草稿";
            case 1: return "审批中";
            case 2: return "已通过";
            case 3: return "已驳回";
            case 4: return "已撤回";
            default: return "未知";
        }
    }

    private String toResignTypeText(Integer type) {
        if (type == null) return null;
        switch (type) {
            case 1: return "主动辞职";
            case 2: return "合同到期";
            case 3: return "辞退";
            case 4: return "终止合同";
            default: return "未知";
        }
    }

    private String decodeSensitive(String value) {
        if (value == null) return null;
        return value;
    }
}

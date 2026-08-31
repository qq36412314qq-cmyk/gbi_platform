package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.PermissionAuditDTO;
import com.gbi.platform.dto.PermissionAuditQueryDTO;
import com.gbi.platform.entity.SysPermissionAudit;
import com.gbi.platform.mapper.SysPermissionAuditMapper;
import com.gbi.platform.service.PermissionAuditService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.PermissionAuditVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 权限二级复核服务实现：敏感权限变更复核（对齐《后端编码规范》十三）
 * 一期记录来源：初始化 SQL 预置待审核申请；二期角色授权变更时自动写入
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionAuditServiceImpl implements PermissionAuditService {

    private final SysPermissionAuditMapper permissionAuditMapper;

    private final AuditLogUtil auditLogUtil;

    @Override
    public PageVO<PermissionAuditVO> page(PermissionAuditQueryDTO dto) {
        Page<PermissionAuditVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LoginUser loginUser = UserContext.getLoginUser();
        // 非超管只看本公司复核记录
        Long companyId = loginUser.isSuperAdmin() ? null : loginUser.getCompanyId();
        Page<PermissionAuditVO> result = permissionAuditMapper.selectPageVO(
                page, dto.getAuditStatus(), dto.getApplyUserName(), companyId);
        return new PageVO<>(result.getRecords(), result.getTotal(),
                result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(PermissionAuditDTO dto) {
        SysPermissionAudit audit = permissionAuditMapper.selectById(dto.getId());
        if (audit == null) {
            throw new BizException("复核记录不存在");
        }
        if (!Objects.equals(audit.getAuditStatus(), CommonConst.AUDIT_STATUS_PENDING)) {
            throw new BizException("该记录已处理，请勿重复操作");
        }
        LoginUser loginUser = UserContext.getLoginUser();
        audit.setAuditStatus(dto.getAuditStatus());
        audit.setAuditComment(dto.getAuditComment());
        audit.setAuditUserId(loginUser.getUserId());
        audit.setAuditTime(LocalDateTime.now());
        permissionAuditMapper.updateById(audit);
        // 复核操作本身记审计
        auditLogUtil.record(CommonConst.MODULE_SYS, CommonConst.OPER_TYPE_AUDIT,
                String.valueOf(audit.getId()), null, audit);
    }
}
package com.gbi.platform.service;

import com.gbi.platform.dto.PermissionAuditDTO;
import com.gbi.platform.dto.PermissionAuditQueryDTO;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.PermissionAuditVO;

/**
 * 权限二级复核服务：敏感权限变更复核（对齐《后端编码规范》十三）
 *
 * @author gbi
 */
public interface PermissionAuditService {

    /**
     * 复核记录分页
     */
    PageVO<PermissionAuditVO> page(PermissionAuditQueryDTO dto);

    /**
     * 复核处理：通过/驳回（仅待审核可操作，操作写入审计日志）
     */
    void audit(PermissionAuditDTO dto);
}

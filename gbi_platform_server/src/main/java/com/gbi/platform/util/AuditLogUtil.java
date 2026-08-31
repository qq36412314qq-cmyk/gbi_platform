package com.gbi.platform.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.entity.SysAuditLog;
import com.gbi.platform.mapper.SysAuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 审计日志工具：新增/编辑/删除/审核等关键操作统一记录（对齐《后端编码规范》十六）
 * 日志永久存储，禁止删除
 *
 * @author gbi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogUtil {

    private final SysAuditLogMapper auditLogMapper;

    private final ObjectMapper objectMapper;

    /**
     * 记录审计日志
     *
     * @param module    操作模块，如 org/sys（见 {@link CommonConst#MODULE_ORG}）
     * @param operType  操作类型：新增/编辑/删除/导出/审核/登录/退出
     * @param bizId     关联业务单据ID（可空）
     * @param beforeObj 操作前数据快照（可空）
     * @param afterObj  操作后数据快照（可空）
     */
    public void record(String module, String operType, String bizId, Object beforeObj, Object afterObj) {
        try {
            SysAuditLog auditLog = new SysAuditLog();
            LoginUser loginUser = com.gbi.platform.common.security.UserContext.getLoginUserOrNull();
            if (loginUser != null) {
                auditLog.setCompanyId(loginUser.getCompanyId());
                auditLog.setOperUserId(loginUser.getUserId());
                auditLog.setOperUserName(loginUser.getRealName() == null ? loginUser.getUsername() : loginUser.getRealName());
            } else {
                auditLog.setCompanyId(CommonConst.COMPANY_ROOT);
                auditLog.setOperUserId(0L);
                auditLog.setOperUserName("system");
            }
            auditLog.setOperModule(module);
            auditLog.setOperType(operType);
            auditLog.setBizId(bizId);
            auditLog.setBeforeJson(beforeObj == null ? null : objectMapper.writeValueAsString(beforeObj));
            auditLog.setAfterJson(afterObj == null ? null : objectMapper.writeValueAsString(afterObj));
            auditLog.setCreateTime(LocalDateTime.now());
            // 操作 IP
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
                auditLog.setOperIp(IpUtil.getClientIp(attrs.getRequest()));
            }
            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            // 审计失败不影响主流程，仅记录日志
            log.error("审计日志写入失败: module={}, type={}, bizId={}", module, operType, bizId, e);
        }
    }
}

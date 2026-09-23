package com.gbi.platform.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.entity.SysAuditLog;
import com.gbi.platform.mapper.SysAuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AuditLogUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final SysAuditLogMapper auditLogMapper;

    public AuditLogUtil(SysAuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    public void record(String module, String operType, String bizId, Object beforeObj, Object afterObj) {
        try {
            SysAuditLog log = new SysAuditLog();
            log.setOperModule(module);
            log.setOperType(operType);
            log.setBizId(bizId);
            log.setBeforeJson(beforeObj != null ? OBJECT_MAPPER.writeValueAsString(beforeObj) : null);
            log.setAfterJson(afterObj != null ? OBJECT_MAPPER.writeValueAsString(afterObj) : null);
            log.setOperIp(getClientIp());
            log.setOperUserName(getCurrentUserName());
            auditLogMapper.insert(log);
        } catch (Exception e) {
            System.err.println("[AuditLogUtil] 记录审计日志失败: " + e.getMessage());
        }
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isBlank()) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isBlank()) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception ignored) {
        }
        return "unknown";
    }

    private String getCurrentUserName() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                Object attr = request.getAttribute("loginUser");
                if (attr != null) {
                    com.gbi.platform.common.security.LoginUser user =
                            (com.gbi.platform.common.security.LoginUser) attr;
                    return user.getRealName() != null ? user.getRealName() : user.getUsername();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}

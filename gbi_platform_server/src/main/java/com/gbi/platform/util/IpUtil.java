package com.gbi.platform.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * 客户端 IP 获取工具：优先取 X-Forwarded-For（Nginx 反代场景）
 *
 * @author gbi
 */
public final class IpUtil {

    private static final String UNKNOWN = "unknown";

    private IpUtil() {
    }

    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            // 多级代理取第一个
            int index = ip.indexOf(',');
            return index > 0 ? ip.substring(0, index).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        ip = request.getRemoteAddr();
        return ip;
    }
}

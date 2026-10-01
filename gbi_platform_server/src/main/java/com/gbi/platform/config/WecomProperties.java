package com.gbi.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 企业微信基础设施配置属性
 * 对应 application.yml 中的 wecom.* 前缀配置项
 * token / encodingAesKey / callbackUrl 必须通过环境变量注入，禁止硬编码
 *
 * @author gbi
 */
@Data
@ConfigurationProperties(prefix = "wecom")
public class WecomProperties {

    /** 消息回调 Token（安全敏感，从环境变量 WECOM_TOKEN 注入） */
    private String token;

    /** 加密密钥（安全敏感，从环境变量 WECOM_AES_KEY 注入） */
    private String encodingAesKey;

    /** 消息回调 URL（需 HTTPS、公网可访问） */
    private String callbackUrl;

    /** 系统 Web 地址，用于消息内跳转链接 */
    private String webUrl;

    /** API 连接超时（毫秒） */
    private int connectTimeout = 5000;

    /** API 读取超时（毫秒） */
    private int readTimeout = 10000;

    /** 失败最大重试次数 */
    private int maxRetryCount = 3;
}

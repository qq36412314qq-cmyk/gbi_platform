package com.gbi.platform.controller.hr;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * 企业微信回调接口（无需登录认证，由企微服务器推送）
 * - GET：URL验证（回调配置时企微发送，返回 echostr）
 * - POST：消息/事件接收（XML 加密消息体，内部验签后解密处理）
 *
 * @author gbi
 */
@Slf4j
@Tag(name = "企微回调接口")
@RestController
@RequestMapping("/api/wecom/callback")
@RequiredArgsConstructor
public class WecomCallbackController {

    /** 企微回调 Token，从环境变量 WECOM_TOKEN 注入 */
    @Value("${wecom.token}")
    private String token;

    /** 加密密钥，从环境变量 WECOM_AES_KEY 注入（43位 Base64 编码） */
    @Value("${wecom.encoding-aes-key}")
    private String encodingAesKey;

    @Operation(summary = "企微URL验证（GET）")
    @GetMapping
    public String verifyUrl(
            @RequestParam String timestamp,
            @RequestParam String nonce,
            @RequestParam String signature,
            @RequestParam String echostr) {
        if (!checkSignature(timestamp, nonce, signature)) {
            log.warn("企微URL验证签名不匹配，拒绝验证请求，timestamp={}, nonce={}, signature={}",
                    timestamp, nonce, signature);
            return "signature invalid";
        }
        log.info("企微URL验证通过，回调地址配置成功");
        return echostr;
    }

    @Operation(summary = "企微消息/事件接收（POST）")
    @PostMapping
    public String receiveMessage(@RequestBody String xmlBody) {
        log.info("收到企微回调消息，body前200字符={}",
                xmlBody.length() > 200 ? xmlBody.substring(0, 200) + "..." : xmlBody);

        // TODO: Phase 3 实现 AES-256-CBC 解密和消息分发逻辑
        // 1. 从 XML 提取 Encrypt 字段
        // 2. 使用 WecomCryptoUtil.decrypt(encryptMsg, encodingAesKey, corpId) 解密
        // 3. 解析 XML/JSON 消息类型（text/event/image）
        // 4. 按消息类型分发到对应 Handler（审批回调/考勤同步/通讯录变更等）

        // 快速返回 success，避免企微重复推送
        return "success";
    }

    /**
     * 企微请求签名验证
     * 将 token + timestamp + nonce 字典序拼接后 SHA1 加密，与 signature 比对
     */
    private boolean checkSignature(String timestamp, String nonce, String signature) {
        try {
            String[] array = new String[]{token, timestamp, nonce};
            Arrays.sort(array);
            StringBuilder sb = new StringBuilder();
            for (String s : array) {
                sb.append(s);
            }
            String plaintext = sb.toString();
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(plaintext.getBytes(StandardCharsets.UTF_8));
            String computed = bytesToHex(digest);
            boolean match = computed.equalsIgnoreCase(signature);
            if (!match) {
                log.warn("企微签名验证失败，computed={}, signature={}", computed, signature);
            }
            return match;
        } catch (Exception e) {
            log.error("企微签名验证异常", e);
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

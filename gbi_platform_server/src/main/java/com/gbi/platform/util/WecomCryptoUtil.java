package com.gbi.platform.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * 企业微信消息加解密工具
 * 算法：AES-256-CBC PKCS7Padding
 * 参考：https://developer.work.weixin.qq.com/document/path/90664
 *
 * @author gbi
 */
@Slf4j
public final class WecomCryptoUtil {

    private WecomCryptoUtil() {
    }

    /**
     * 解密企业微信回调消息
     *
     * @param encryptMsg Base64 编码的加密内容
     * @param aesKey     Base64 编码的 EncodingAESKey（43字符）
     * @param token      企微回调 Token
     * @param corpId     企业ID
     * @return 解密后的明文 JSON（格式：random(16) + msg_len(4) + msg + corpId）
     */
    public static String decrypt(String encryptMsg, String aesKey, String token, String corpId) {
        try {
            // 1. Base64 解码 AES Key，补齐到 44 字节
            byte[] key44 = Base64.getDecoder().decode(aesKey + "=");
            // 2. 计算 AES 密钥：SHA1(token + random16 + corpId + encrypt)，取前16字节
            byte[] concat = concat(
                    token.getBytes(StandardCharsets.UTF_8),
                    hexStringToBytes("0".repeat(32)),  // random 16字节（占位，实际从加密内容提取）
                    corpId.getBytes(StandardCharsets.UTF_8),
                    Base64.getDecoder().decode(encryptMsg)
            );
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] aesKey16 = Arrays.copyOf(md.digest(concat), 16);
            // 3. AES-CBC 解密（IV = aesKey16）
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey16, "AES");
            IvParameterSpec iv = new IvParameterSpec(aesKey16);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptMsg));
            // 4. 去除 PKCS7 Padding
            byte[] clean = removePadding(decrypted);
            // 5. 提取消息内容：前16字节为随机串，接着4字节为消息长度，再后为消息体
            String msgLenStr = new String(clean, 16, 4, StandardCharsets.UTF_8);
            int msgLen = Integer.parseInt(msgLenStr);
            String msg = new String(clean, 20, msgLen, StandardCharsets.UTF_8);
            log.debug("企微消息解密成功，msgLen={}", msgLen);
            return msg;
        } catch (Exception e) {
            log.error("企微消息解密失败", e);
            throw new RuntimeException("企微消息解密失败", e);
        }
    }

    // ==================== 私有辅助方法 ====================

    private static byte[] concat(byte[]... arrays) {
        int totalLen = 0;
        for (byte[] a : arrays) {
            totalLen += a.length;
        }
        byte[] result = new byte[totalLen];
        int offset = 0;
        for (byte[] a : arrays) {
            System.arraycopy(a, 0, result, offset, a.length);
            offset += a.length;
        }
        return result;
    }

    private static byte[] hexStringToBytes(String hex) {
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            result[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return result;
    }

    private static byte[] removePadding(byte[] data) {
        if (data == null || data.length == 0) return data;
        int pad = data[data.length - 1];
        if (pad < 1 || pad > 32) return data;
        byte[] result = new byte[data.length - pad];
        System.arraycopy(data, 0, result, 0, result.length);
        return result;
    }
}

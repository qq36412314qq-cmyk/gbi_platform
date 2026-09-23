package com.gbi.platform.util;

import com.gbi.platform.common.constant.FileStorageConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件存储工具：MD5计算、MIME校验、文件名清洗、AES加解密、file_key生成
 *
 * @author gbi
 */
@Slf4j
@Component
public class FileStorageUtil {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 计算文件MD5（小写32位）
     */
    public String md5(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = file.getBytes();
            byte[] md5Bytes = digest.digest(bytes);
            StringBuilder sb = new StringBuilder(32);
            for (byte b : md5Bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("MD5计算失败", e);
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    /**
     * 校验MIME类型是否在允许范围内
     */
    public boolean isAllowedMime(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        String lower = contentType.toLowerCase();
        // 图片
        if (FileStorageConst.ALLOWED_IMAGE_MIME.contains(lower)) {
            return true;
        }
        // 文档
        return lower.contains("pdf")
                || lower.contains("ms-word")
                || lower.contains("ms-excel")
                || lower.contains("ms-powerpoint")
                || lower.contains("openxmlformats-officedocument")
                || lower.contains("text/plain")
                || lower.contains("zip")
                || lower.contains("rar");
    }

    /**
     * 从文件名提取后缀（小写，不含点）
     */
    public String extractExt(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return ext.length() > 16 ? ext.substring(0, 16) : ext;
    }

    /**
     * 校验后缀是否在白名单内
     */
    public boolean isAllowedExt(String ext) {
        if (ext == null || ext.isBlank()) {
            return false;
        }
        return FileStorageConst.FILE_WHITELIST.contains(ext.toLowerCase());
    }

    /**
     * 清洗文件名：去除危险字符，保留中英文、数字、下划线、连字符、点
     */
    public String sanitizeFileName(String originalName) {
        if (originalName == null) {
            return "unnamed";
        }
        String sanitized = originalName.replaceAll("[^\\p{L}\\p{N}_\\-\\.]", "_");
        return sanitized.length() > 128 ? sanitized.substring(0, 128) : sanitized;
    }

    /**
     * 生成 file_key：{company_id}/{biz_type}/{yyyyMMdd}/{UUID}.{ext}
     */
    public String generateFileKey(long companyId, String bizType, String ext) {
        String dateStr = LocalDate.now().format(DATE_FMT);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return companyId + FileStorageConst.KEY_SEPARATOR
                + bizType + FileStorageConst.KEY_SEPARATOR
                + dateStr + FileStorageConst.KEY_SEPARATOR
                + uuid + (ext.startsWith(".") ? ext : "." + ext);
    }

    /**
     * AES-128-ECB 加密（用于加密 sys_config 中的 AK/SK）
     * 密钥长度固定 16 字节，不足补零，超出截断
     */
    public String encryptAES(String plaintext, String aesKey) {
        if (plaintext == null || plaintext.isBlank()) {
            return plaintext;
        }
        try {
            byte[] keyBytes = aesKey.getBytes(StandardCharsets.UTF_8);
            byte[] key16 = new byte[16];
            System.arraycopy(keyBytes, 0, key16, 0, Math.min(keyBytes.length, 16));
            SecretKeySpec keySpec = new SecretKeySpec(key16, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encrypted);
        } catch (Exception e) {
            log.error("AES加密失败", e);
            throw new RuntimeException("AES加密失败", e);
        }
    }

    /**
     * AES-128-ECB 解密
     */
    public String decryptAES(String ciphertext, String aesKey) {
        if (ciphertext == null || ciphertext.isBlank()) {
            return ciphertext;
        }
        try {
            byte[] keyBytes = aesKey.getBytes(StandardCharsets.UTF_8);
            byte[] key16 = new byte[16];
            System.arraycopy(keyBytes, 0, key16, 0, Math.min(keyBytes.length, 16));
            SecretKeySpec keySpec = new SecretKeySpec(key16, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(hexToBytes(ciphertext));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密失败", e);
            throw new RuntimeException("AES解密失败", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}

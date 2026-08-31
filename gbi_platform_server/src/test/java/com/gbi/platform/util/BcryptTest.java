package com.gbi.platform.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 测试工具 - 仅用于本地验证密码哈希格式
 * 注意：不要在此文件中提交任何明文密码或真实哈希值
 */
public class BcryptTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // 输出示例格式（无实际明文对应），仅用于验证哈希格式正确性
        String exampleHash = encoder.encode("placeholder");
        System.out.println("Example hash format: " + exampleHash);
        System.out.println("Hash length: " + exampleHash.length());
    }
}
package com.gbi.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密配置：独立配置类
 * （放 SecurityConfig 会与 JwtAuthFilter -> UserService -> AuthService -> PasswordEncoder 形成循环依赖）
 *
 * @author gbi
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
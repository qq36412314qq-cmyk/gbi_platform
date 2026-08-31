package com.gbi.platform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.common.result.ResultCode;
import com.gbi.platform.common.security.JwtAuthFilter;
import com.gbi.platform.common.security.MyPermissionEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置：JWT 无状态认证 + 方法级权限校验（@PreAuthorize）
 * PasswordEncoder 定义在 PasswordConfig（独立配置类，避免与 JwtAuthFilter 形成循环依赖）
 *
 * @author gbi
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /** 无需登录的接口白名单 */
    private static final String[] PERMIT_ALL = {
            "/base/login",
            "/doc.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/upload/**",
            "/error",
            "/finance/discount/**"
    };

    private final JwtAuthFilter jwtAuthFilter;

    private final ObjectMapper objectMapper;

    /**
     * 方法级权限校验使用自定义 PermissionEvaluator（hasPermission），
     * 不注册则 Spring 默认使用 DenyAllPermissionEvaluator 导致全部 403
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(MyPermissionEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_ALL).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(handler -> handler
                        // 未登录返回 401 统一 JSON
                        .authenticationEntryPoint((request, response, e) -> {
                            response.setStatus(200);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(objectMapper.writeValueAsString(Result.error(ResultCode.UNAUTHORIZED)));
                        })
                        // 无权限返回 403 统一 JSON
                        .accessDeniedHandler((request, response, e) -> {
                            response.setStatus(200);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(objectMapper.writeValueAsString(Result.error(ResultCode.FORBIDDEN)));
                        }))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

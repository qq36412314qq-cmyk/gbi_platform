package com.gbi.platform.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：签发与解析 Token
 *
 * @author gbi
 */
@Component
public class JwtUtil {

    /** 密钥（长度必须 >= 32 字节，来自 application.yml） */
    private final SecretKey secretKey;

    /** 过期时间（小时） */
    private final long expireHours;

    public JwtUtil(@Value("${gbi.jwt.secret}") String secret,
                   @Value("${gbi.jwt.expire-hours}") long expireHours) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireHours = expireHours;
    }

    /**
     * 签发 Token：subject 为用户ID
     */
    public String createToken(Long userId) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + expireHours * 3600 * 1000L);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expireTime)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析 Token，返回用户ID；无效/过期抛出 JwtException
     */
    public Long parseUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }
}

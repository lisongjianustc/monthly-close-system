package com.mc.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * <p>
 * 密钥优先级: 环境变量/JVM系统属性 > application.yml配置 > 内置默认值
 * </p>
 */
public class JwtUtil {

    private static final String DEFAULT_SECRET = "mc-monthly-close-system-secret-key-2024-very-long-key-for-hs256";

    /** 密钥: 支持通过 -Djwt.secret=xxx 或环境变量 JWT_SECRET 覆盖 */
    private static final SecretKey KEY;

    private static final long EXPIRE_MS = 7 * 24 * 60 * 60 * 1000L; // 7天

    static {
        String secret = System.getProperty("jwt.secret", System.getenv("JWT_SECRET"));
        if (!StringUtils.hasText(secret)) {
            secret = DEFAULT_SECRET;
        }
        KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_MS))
                .signWith(KEY)
                .compact();
    }

    public static Map<String, Object> parseToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            JwtParser parser = Jwts.parser().verifyWith(KEY).build();
            Jws<Claims> jws = parser.parseSignedClaims(token);
            Claims claims = jws.getPayload();
            Map<String, Object> result = new HashMap<>();
            result.put("userId", claims.get("userId", Long.class));
            result.put("username", claims.get("username", String.class));
            return result;
        } catch (JwtException e) {
            return null;
        }
    }

    public static boolean validateToken(String token) {
        return parseToken(token) != null;
    }
}

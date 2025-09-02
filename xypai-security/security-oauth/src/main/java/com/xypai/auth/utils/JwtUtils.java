package com.xypai.auth.utils;

import com.xypai.auth.feign.dto.AuthUserDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * JWT工具类
 *
 * @author xypai
 * @date 2025-01-01
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${auth.jwt.secret:xypai-auth-jwt-secret-key-2025-implementation-64-characters-long}")
    private String secret;

    @Value("${auth.jwt.access-token-validity:86400}")
    private long accessTokenValidity;

    @Value("${auth.jwt.refresh-token-validity:604800}")
    private long refreshTokenValidity;

    @Value("${auth.jwt.issuer:xypai-auth}")
    private String issuer;

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(AuthUserDTO user, String clientType, String deviceId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", user.getId());
        claims.put("username", user.getUsername());
        claims.put("nickname", user.getNickname());
        claims.put("mobile", user.getMobile());
        claims.put("status", user.getStatus());
        claims.put("roles", user.getRoles());
        claims.put("permissions", user.getPermissions());
        claims.put("token_type", "access");
        claims.put("client_type", clientType);
        claims.put("device_id", deviceId);

        return generateToken(claims, user.getUsername(), accessTokenValidity);
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(AuthUserDTO user, String clientType, String deviceId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", user.getId());
        claims.put("username", user.getUsername());
        claims.put("token_type", "refresh");
        claims.put("client_type", clientType);
        claims.put("device_id", deviceId);

        return generateToken(claims, user.getUsername(), refreshTokenValidity);
    }

    /**
     * 生成令牌
     */
    private String generateToken(Map<String, Object> claims, String subject, long validity) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validity * 1000);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
    }

    /**
     * 验证令牌
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 解析令牌
     */
    public Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        
        return Jwts.parser()
                .setSigningKey(key)
                .requireIssuer(issuer)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从令牌中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("user_id", Long.class);
    }

    /**
     * 从令牌中获取所有声明
     */
    public Map<String, Object> getAllClaimsFromToken(String token) {
        Claims claims = parseToken(token);
        return new HashMap<>(claims);
    }

    /**
     * 检查令牌是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    /**
     * 获取令牌剩余有效时间(秒)
     */
    public long getTokenRemainingTime(String token) {
        try {
            Claims claims = parseToken(token);
            long expiration = claims.getExpiration().getTime();
            long now = System.currentTimeMillis();
            return Math.max(0, (expiration - now) / 1000);
        } catch (JwtException e) {
            return 0;
        }
    }

    /**
     * 检查是否为访问令牌
     */
    public boolean isAccessToken(String token) {
        try {
            Claims claims = parseToken(token);
            return "access".equals(claims.get("token_type"));
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 检查是否为刷新令牌
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            return "refresh".equals(claims.get("token_type"));
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 从令牌中提取用户信息
     */
    public AuthUserDTO extractUserInfo(String token) {
        Claims claims = parseToken(token);
        
        @SuppressWarnings("unchecked")
        Set<String> roles = (Set<String>) claims.get("roles");
        
        @SuppressWarnings("unchecked")
        Set<String> permissions = (Set<String>) claims.get("permissions");

        return AuthUserDTO.builder()
                .id(claims.get("user_id", Long.class))
                .username(claims.get("username", String.class))
                .nickname((String) claims.get("nickname"))
                .mobile((String) claims.get("mobile"))
                .status(claims.get("status", Integer.class))
                .roles(roles)
                .permissions(permissions)
                .lastLoginTime(LocalDateTime.now())
                .build();
    }
}

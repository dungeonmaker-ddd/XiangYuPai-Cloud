package com.xypai.security.oauth.service.util;

import com.xypai.security.oauth.auth.dto.response.AuthResponse;
import com.xypai.security.oauth.common.exception.AuthException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 🔑 现代化JWT工具类
 * <p>
 * XV03:11 SERVICE层 - 现代化JWT令牌处理
 * 支持异步处理、类型安全、函数式编程等现代特性
 *
 * @author xypai
 * @since 3.0.0
 */
@Slf4j
@Component
public class ModernJwtUtil {

    // JWT声明常量
    private static final String CLAIM_USER_ID = "user_id";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_DISPLAY_NAME = "display_name";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_MOBILE = "mobile";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_PERMISSIONS = "permissions";
    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String CLAIM_CLIENT_TYPE = "client_type";
    private static final String CLAIM_DEVICE_ID = "device_id";
    // 令牌类型常量
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    private final SecretKey secretKey;
    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;
    private final String issuer;
    private final Set<String> audiences;

    public ModernJwtUtil(
            @Value("${auth.jwt.secret:xypai-security-jwt-secret-key-2025-modern-implementation}") String secret,
            @Value("${auth.jwt.access-token-validity:86400}") long accessExpiration,
            @Value("${auth.jwt.refresh-token-validity:604800}") long refreshExpiration,
            @Value("${auth.jwt.issuer:xypai-security-oauth}") String issuer,
            @Value("${auth.jwt.audiences:xypai-client,xypai-web,xypai-app}") String audiencesStr) {

        // 现代化密钥生成 - 确保足够的安全性
        if (secret.length() < 64) {
            throw new IllegalArgumentException("JWT密钥长度不能少于64字符，当前长度: %d".formatted(secret.length()));
        }

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiration = Duration.ofSeconds(accessExpiration);
        this.refreshTokenExpiration = Duration.ofSeconds(refreshExpiration);
        this.issuer = issuer;
        this.audiences = Set.of(audiencesStr.split(","));

        log.info("现代化JWT工具类初始化完成: accessExpiration={}s, refreshExpiration={}s, issuer={}",
                accessExpiration, refreshExpiration, issuer);
    }

    /**
     * 🔐 生成访问令牌
     */
    public String generateAccessToken(AuthResponse.UserInfo userInfo, String clientType, String deviceId) {
        return generateToken(userInfo, TOKEN_TYPE_ACCESS, accessTokenExpiration, clientType, deviceId);
    }

    /**
     * 🔄 生成刷新令牌
     */
    public String generateRefreshToken(AuthResponse.UserInfo userInfo, String clientType, String deviceId) {
        return generateToken(userInfo, TOKEN_TYPE_REFRESH, refreshTokenExpiration, clientType, deviceId);
    }

    /**
     * 🔐 异步生成访问令牌
     */
    public CompletableFuture<String> generateAccessTokenAsync(AuthResponse.UserInfo userInfo, String clientType, String deviceId) {
        return CompletableFuture.supplyAsync(() -> generateAccessToken(userInfo, clientType, deviceId));
    }

    /**
     * 🔄 异步生成刷新令牌
     */
    public CompletableFuture<String> generateRefreshTokenAsync(AuthResponse.UserInfo userInfo, String clientType, String deviceId) {
        return CompletableFuture.supplyAsync(() -> generateRefreshToken(userInfo, clientType, deviceId));
    }

    /**
     * 🔨 现代化令牌生成核心方法
     */
    private String generateToken(AuthResponse.UserInfo userInfo, String tokenType, Duration expiration,
                                 String clientType, String deviceId) {
        try {
            var now = Instant.now();
            var expirationTime = now.plus(expiration);

            // 使用现代化的构建器模式
            var claimsBuilder = Jwts.claims()
                    .subject(userInfo.username())
                    .issuer(issuer)
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(expirationTime))
                    .add(CLAIM_USER_ID, userInfo.id())
                    .add(CLAIM_USERNAME, userInfo.username())
                    .add(CLAIM_DISPLAY_NAME, userInfo.displayName())
                    .add(CLAIM_ROLES, userInfo.roles())
                    .add(CLAIM_PERMISSIONS, userInfo.permissions())
                    .add(CLAIM_TOKEN_TYPE, tokenType);

            // 添加可选字段
            if (userInfo.email() != null) {
                claimsBuilder.add(CLAIM_EMAIL, userInfo.email());
            }
            if (userInfo.mobile() != null) {
                claimsBuilder.add(CLAIM_MOBILE, userInfo.mobile());
            }
            if (clientType != null) {
                claimsBuilder.add(CLAIM_CLIENT_TYPE, clientType);
            }
            if (deviceId != null) {
                claimsBuilder.add(CLAIM_DEVICE_ID, deviceId);
            }

            // 添加受众
            audiences.forEach(audience -> claimsBuilder.audience().add(audience));

            return Jwts.builder()
                    .claims(claimsBuilder.build())
                    .signWith(secretKey, Jwts.SIG.HS512)  // 使用更强的签名算法
                    .compact();

        } catch (Exception e) {
            log.error("生成JWT令牌失败: tokenType={}, userId={}, error={}",
                    tokenType, userInfo.id(), e.getMessage(), e);
            throw new AuthException.InvalidTokenException(tokenType, "令牌生成失败: " + e.getMessage());
        }
    }

    /**
     * ✅ 验证令牌有效性
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.debug("令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * ✅ 异步验证令牌
     */
    public CompletableFuture<Boolean> validateTokenAsync(String token) {
        return CompletableFuture.supplyAsync(() -> validateToken(token));
    }

    /**
     * 📝 解析令牌获取Claims - 现代化错误处理
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            var expiredTime = e.getClaims().getExpiration().toInstant();
            throw AuthException.tokenExpired("JWT", expiredTime);
        } catch (MalformedJwtException e) {
            throw AuthException.invalidToken("JWT", "令牌格式错误");
        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw AuthException.invalidToken("JWT", "令牌签名无效");
        } catch (UnsupportedJwtException e) {
            throw AuthException.invalidToken("JWT", "不支持的令牌格式");
        } catch (IllegalArgumentException e) {
            throw AuthException.invalidToken("JWT", "令牌参数无效");
        } catch (Exception e) {
            log.error("令牌解析异常: {}", e.getMessage(), e);
            throw AuthException.invalidToken("JWT", "令牌解析失败");
        }
    }

    /**
     * 👤 从令牌中提取用户信息 - 现代化实现
     */
    public AuthResponse.UserInfo extractUserInfo(String token) {
        try {
            var claims = parseToken(token);

            // 使用现代化的Optional处理
            var email = Optional.ofNullable((String) claims.get(CLAIM_EMAIL)).orElse(null);
            var mobile = Optional.ofNullable((String) claims.get(CLAIM_MOBILE)).orElse(null);

            // 安全的类型转换
            @SuppressWarnings("unchecked")
            var roles = Optional.ofNullable((java.util.List<String>) claims.get(CLAIM_ROLES))
                    .map(Set::copyOf)
                    .orElse(Set.of());

            @SuppressWarnings("unchecked")
            var permissions = Optional.ofNullable((java.util.List<String>) claims.get(CLAIM_PERMISSIONS))
                    .map(Set::copyOf)
                    .orElse(Set.of());

            // 使用工厂方法创建适当的用户类型
            var userId = claims.get(CLAIM_USER_ID, Long.class);
            var username = claims.get(CLAIM_USERNAME, String.class);
            var displayName = (String) claims.get(CLAIM_DISPLAY_NAME);
            var issuedAt = claims.getIssuedAt().toInstant();

            // 根据角色创建不同类型的用户
            if (roles.contains("ADMIN")) {
                return new AuthResponse.AdminUser(userId, username, displayName, email, mobile,
                        roles, permissions, issuedAt);
            } else if (roles.contains("GUEST")) {
                return new AuthResponse.GuestUser(userId, username, displayName, email, mobile,
                        roles, permissions, issuedAt);
            } else {
                return new AuthResponse.StandardUser(userId, username, displayName, email, mobile,
                        roles, permissions, issuedAt);
            }

        } catch (Exception e) {
            log.error("提取用户信息失败: {}", e.getMessage(), e);
            throw AuthException.invalidToken("JWT", "无法提取用户信息");
        }
    }

    /**
     * 🏷️ 获取令牌类型
     */
    public String getTokenType(String token) {
        var claims = parseToken(token);
        return claims.get(CLAIM_TOKEN_TYPE, String.class);
    }

    /**
     * ⏰ 检查令牌是否即将过期
     */
    public boolean isTokenExpiringSoon(String token, Duration threshold) {
        try {
            var claims = parseToken(token);
            var expiration = claims.getExpiration().toInstant();
            var timeToExpire = Duration.between(Instant.now(), expiration);
            return timeToExpire.compareTo(threshold) <= 0;
        } catch (Exception e) {
            return true; // 解析失败认为即将过期
        }
    }

    /**
     * 🔍 验证是否为访问令牌
     */
    public boolean isAccessToken(String token) {
        try {
            return TOKEN_TYPE_ACCESS.equals(getTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 🔄 验证是否为刷新令牌
     */
    public boolean isRefreshToken(String token) {
        try {
            return TOKEN_TYPE_REFRESH.equals(getTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 📊 获取令牌详细信息
     */
    public TokenInfo getTokenInfo(String token) {
        try {
            var claims = parseToken(token);

            return new TokenInfo(
                    token,
                    claims.get(CLAIM_TOKEN_TYPE, String.class),
                    claims.get(CLAIM_USER_ID, Long.class),
                    claims.get(CLAIM_USERNAME, String.class),
                    claims.getIssuedAt().toInstant(),
                    claims.getExpiration().toInstant(),
                    (String) claims.get(CLAIM_CLIENT_TYPE),
                    (String) claims.get(CLAIM_DEVICE_ID),
                    Map.copyOf(claims)
            );
        } catch (Exception e) {
            throw AuthException.invalidToken("JWT", "无法获取令牌信息");
        }
    }

    /**
     * 📊 令牌信息Record
     */
    public record TokenInfo(
            String token,
            String tokenType,
            Long userId,
            String username,
            Instant issuedAt,
            Instant expiresAt,
            String clientType,
            String deviceId,
            Map<String, Object> allClaims
    ) {
        public Duration getRemainingTime() {
            return Duration.between(Instant.now(), expiresAt);
        }

        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }

        public boolean isExpiringSoon(Duration threshold) {
            return getRemainingTime().compareTo(threshold) <= 0;
        }

        public String getMaskedToken() {
            if (token.length() < 20) return "***";
            return token.substring(0, 10) + "***" + token.substring(token.length() - 6);
        }
    }
}

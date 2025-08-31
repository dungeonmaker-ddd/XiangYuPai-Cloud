package com.xypai.security.oauth.service.business.impl;

import com.xypai.security.oauth.auth.dto.response.AuthResponse;
import com.xypai.security.oauth.service.business.TokenBusiness;
import com.xypai.security.oauth.service.util.ModernJwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 🔑 现代化Token业务实现
 * <p>
 * XV03:13 SERVICE层 - 现代化Token管理实现
 * 支持JWT令牌、异步处理、批量操作
 *
 * @author xypai
 * @since 3.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModernTokenBusinessImpl implements TokenBusiness {

    private final ModernJwtUtil jwtUtil;

    // MVP版本：内存存储活跃令牌（生产环境应使用Redis）
    private final Map<String, TokenDetails> activeTokens = new ConcurrentHashMap<>();
    private final Map<Long, UserTokenStatistics> userTokenStats = new ConcurrentHashMap<>();

    @Override
    public AuthResponse generateTokens(AuthResponse.UserInfo userInfo) {
        try {
            // 生成JWT令牌
            var accessToken = jwtUtil.generateAccessToken(userInfo, "web", null);
            var refreshToken = jwtUtil.generateRefreshToken(userInfo, "web", null);

            // 存储令牌详情
            var tokenDetails = new TokenDetails(
                    accessToken, TokenType.ACCESS, userInfo.id(), userInfo.username(),
                    Instant.now(), Instant.now().plus(Duration.ofHours(24)),
                    Duration.ofHours(24), TokenStatus.VALID,
                    "web", null, Map.of()
            );

            activeTokens.put(accessToken, tokenDetails);
            updateUserTokenStats(userInfo.id(), userInfo.username(), 1, 0);

            log.info("令牌生成成功: userId={}, username={}", userInfo.id(), userInfo.username());

            return AuthResponse.create(accessToken, refreshToken, 86400L, userInfo);

        } catch (Exception e) {
            log.error("令牌生成失败: userId={}, error={}", userInfo.id(), e.getMessage(), e);
            throw new RuntimeException("令牌生成失败", e);
        }
    }

    @Override
    public CompletableFuture<AuthResponse> generateTokensAsync(AuthResponse.UserInfo userInfo) {
        return CompletableFuture.supplyAsync(() -> generateTokens(userInfo));
    }

    @Override
    public Optional<AuthResponse> refreshTokens(String refreshToken) {
        try {
            // 验证刷新令牌
            if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
                log.warn("无效的刷新令牌");
                return Optional.empty();
            }

            // 提取用户信息
            var userInfo = jwtUtil.extractUserInfo(refreshToken);

            // 生成新的令牌对
            var newResponse = generateTokens(userInfo);

            log.info("令牌刷新成功: username={}", userInfo.username());
            return Optional.of(newResponse);

        } catch (Exception e) {
            log.error("令牌刷新失败: error={}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public CompletableFuture<Optional<AuthResponse>> refreshTokensAsync(String refreshToken) {
        return CompletableFuture.supplyAsync(() -> refreshTokens(refreshToken));
    }

    @Override
    public Optional<Map<String, Object>> verifyToken(String accessToken) {
        try {
            // JWT验证
            if (!jwtUtil.validateToken(accessToken) || !jwtUtil.isAccessToken(accessToken)) {
                return Optional.empty();
            }

            // 检查令牌是否在黑名单中
            var tokenDetails = activeTokens.get(accessToken);
            if (tokenDetails != null && !tokenDetails.isValid()) {
                return Optional.empty();
            }

            // 提取令牌信息
            var tokenInfo = jwtUtil.getTokenInfo(accessToken);
            var userInfo = jwtUtil.extractUserInfo(accessToken);

            var result = Map.<String, Object>of(
                    "valid", true,
                    "user_id", userInfo.id(),
                    "username", userInfo.username(),
                    "roles", userInfo.roles(),
                    "permissions", userInfo.permissions(),
                    "expires_at", tokenInfo.expiresAt(),
                    "remaining_time", tokenInfo.getRemainingTime().toSeconds()
            );

            return Optional.of(result);

        } catch (Exception e) {
            log.debug("令牌验证失败: error={}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public CompletableFuture<Optional<Map<String, Object>>> verifyTokenAsync(String accessToken) {
        return CompletableFuture.supplyAsync(() -> verifyToken(accessToken));
    }

    @Override
    public Optional<AuthResponse.UserInfo> getUserInfoFromToken(String accessToken) {
        try {
            if (!jwtUtil.validateToken(accessToken)) {
                return Optional.empty();
            }

            var userInfo = jwtUtil.extractUserInfo(accessToken);
            return Optional.of(userInfo);

        } catch (Exception e) {
            log.debug("从令牌获取用户信息失败: error={}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean invalidateToken(String accessToken) {
        try {
            // 将令牌标记为无效
            var tokenDetails = activeTokens.get(accessToken);
            if (tokenDetails != null) {
                var invalidatedToken = new TokenDetails(
                        tokenDetails.token(), tokenDetails.type(), tokenDetails.userId(),
                        tokenDetails.username(), tokenDetails.issuedAt(), tokenDetails.expiresAt(),
                        tokenDetails.remainingTime(), TokenStatus.REVOKED,
                        tokenDetails.clientType(), tokenDetails.deviceId(), tokenDetails.claims()
                );

                activeTokens.put(accessToken, invalidatedToken);

                // 更新用户统计
                updateUserTokenStats(tokenDetails.userId(), tokenDetails.username(), 0, 1);

                log.info("令牌注销成功: username={}", tokenDetails.username());
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("令牌注销失败: error={}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public CompletableFuture<Boolean> invalidateTokenAsync(String accessToken) {
        return CompletableFuture.supplyAsync(() -> invalidateToken(accessToken));
    }

    @Override
    public int invalidateAllUserTokens(Long userId) {
        var count = 0;

        for (var entry : activeTokens.entrySet()) {
            var tokenDetails = entry.getValue();
            if (tokenDetails.userId().equals(userId) && tokenDetails.isValid()) {
                invalidateToken(entry.getKey());
                count++;
            }
        }

        log.info("用户所有令牌注销完成: userId={}, count={}", userId, count);
        return count;
    }

    @Override
    public boolean extendTokenExpiry(String accessToken, Duration extension) {
        // JWT令牌无法延期，这里只是记录
        log.info("JWT令牌无法延期，令牌: {}", maskToken(accessToken));
        return false;
    }

    @Override
    public Optional<TokenDetails> getTokenDetails(String accessToken) {
        try {
            if (!jwtUtil.validateToken(accessToken)) {
                return Optional.empty();
            }

            var tokenInfo = jwtUtil.getTokenInfo(accessToken);
            var tokenDetails = new TokenDetails(
                    accessToken, TokenType.ACCESS, tokenInfo.userId(), tokenInfo.username(),
                    tokenInfo.issuedAt(), tokenInfo.expiresAt(), tokenInfo.getRemainingTime(),
                    tokenInfo.isExpired() ? TokenStatus.EXPIRED : TokenStatus.VALID,
                    tokenInfo.clientType(), tokenInfo.deviceId(), tokenInfo.allClaims()
            );

            return Optional.of(tokenDetails);

        } catch (Exception e) {
            log.debug("获取令牌详情失败: error={}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean isTokenExpiringSoon(String accessToken, Duration threshold) {
        return jwtUtil.isTokenExpiringSoon(accessToken, threshold);
    }

    @Override
    public Map<String, TokenValidationResult> batchVerifyTokens(java.util.List<String> accessTokens) {
        return accessTokens.stream()
                .collect(Collectors.toMap(
                        token -> token,
                        this::validateSingleToken
                ));
    }

    @Override
    public CompletableFuture<Map<String, TokenValidationResult>> batchVerifyTokensAsync(java.util.List<String> accessTokens) {
        return CompletableFuture.supplyAsync(() -> batchVerifyTokens(accessTokens));
    }

    @Override
    public Map<String, Optional<AuthResponse>> batchRefreshTokens(java.util.List<String> refreshTokens) {
        return refreshTokens.stream()
                .collect(Collectors.toMap(
                        token -> token,
                        this::refreshTokens
                ));
    }

    @Override
    public UserTokenStatistics getUserTokenStatistics(Long userId) {
        return userTokenStats.getOrDefault(userId,
                new UserTokenStatistics(userId, "unknown", 0, 0, 0, null, Map.of(), Map.of()));
    }

    @Override
    public TokenCleanupResult cleanupExpiredTokens() {
        var startTime = Instant.now();
        var totalScanned = activeTokens.size();
        var expiredCount = 0;
        var revokedCount = 0;

        var iterator = activeTokens.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            var tokenDetails = entry.getValue();

            if (tokenDetails.isExpired()) {
                iterator.remove();
                expiredCount++;
            } else if (tokenDetails.status() == TokenStatus.REVOKED) {
                iterator.remove();
                revokedCount++;
            }
        }

        var cleanupDuration = Duration.between(startTime, Instant.now());

        log.info("令牌清理完成: scanned={}, expired={}, revoked={}, duration={}ms",
                totalScanned, expiredCount, revokedCount, cleanupDuration.toMillis());

        return new TokenCleanupResult(
                totalScanned, expiredCount, revokedCount, cleanupDuration, Instant.now(),
                Map.of(TokenType.ACCESS, expiredCount + revokedCount)
        );
    }

    // ================================
    // 私有辅助方法
    // ================================

    private TokenValidationResult validateSingleToken(String accessToken) {
        try {
            var verifyResult = verifyToken(accessToken);
            if (verifyResult.isPresent()) {
                var result = verifyResult.get();
                return TokenValidationResult.valid(
                        (Long) result.get("user_id"),
                        result.get("permissions") instanceof java.util.Set<?> permissions ?
                                permissions.stream()
                                        .filter(String.class::isInstance)
                                        .map(String.class::cast)
                                        .collect(java.util.stream.Collectors.toSet()) :
                                java.util.Set.of()
                );
            } else {
                return TokenValidationResult.invalid(TokenStatus.INVALID, "令牌无效或已过期");
            }
        } catch (Exception e) {
            return TokenValidationResult.invalid(TokenStatus.INVALID, e.getMessage());
        }
    }

    private void updateUserTokenStats(Long userId, String username, int activeIncrement, int expiredIncrement) {
        userTokenStats.compute(userId, (id, existing) -> {
            if (existing == null) {
                return new UserTokenStatistics(
                        userId, username, activeIncrement, activeIncrement, expiredIncrement,
                        Instant.now(), Map.of("ACCESS", 1), Map.of("web", 1)
                );
            } else {
                return new UserTokenStatistics(
                        existing.userId(), existing.username(),
                        existing.totalTokens() + activeIncrement,
                        existing.activeTokens() + activeIncrement,
                        existing.expiredTokens() + expiredIncrement,
                        Instant.now(),
                        existing.tokensByType(),
                        existing.tokensByClient()
                );
            }
        });
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 8) + "***" + token.substring(token.length() - 4);
    }
}

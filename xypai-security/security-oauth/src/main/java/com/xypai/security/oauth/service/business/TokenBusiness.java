package com.xypai.security.oauth.service.business;

import com.xypai.security.oauth.auth.dto.response.AuthResponse;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 🔑 现代化Token业务接口
 * <p>
 * XV03:10 SERVICE层 - 现代化Token管理业务
 * 支持异步处理、批量操作、智能缓存等现代特性
 *
 * @author xypai
 * @since 3.0.0
 */
public interface TokenBusiness {

    /**
     * 🔨 生成认证令牌对
     */
    AuthResponse generateTokens(AuthResponse.UserInfo userInfo);

    /**
     * 🔨 异步生成认证令牌对
     */
    CompletableFuture<AuthResponse> generateTokensAsync(AuthResponse.UserInfo userInfo);

    /**
     * 🔄 刷新访问令牌
     */
    Optional<AuthResponse> refreshTokens(String refreshToken);

    /**
     * 🔄 异步刷新访问令牌
     */
    CompletableFuture<Optional<AuthResponse>> refreshTokensAsync(String refreshToken);

    /**
     * ✅ 验证访问令牌
     */
    Optional<Map<String, Object>> verifyToken(String accessToken);

    /**
     * ✅ 异步验证访问令牌
     */
    CompletableFuture<Optional<Map<String, Object>>> verifyTokenAsync(String accessToken);

    /**
     * 👤 从令牌中获取用户信息
     */
    Optional<AuthResponse.UserInfo> getUserInfoFromToken(String accessToken);

    /**
     * 🚪 使令牌失效
     */
    boolean invalidateToken(String accessToken);

    /**
     * 🚪 异步使令牌失效
     */
    CompletableFuture<Boolean> invalidateTokenAsync(String accessToken);

    /**
     * 🚪 使用户的所有令牌失效
     */
    int invalidateAllUserTokens(Long userId);

    /**
     * ⏰ 延长令牌过期时间
     */
    boolean extendTokenExpiry(String accessToken, java.time.Duration extension);

    /**
     * 📊 获取令牌详细信息
     */
    Optional<TokenDetails> getTokenDetails(String accessToken);

    /**
     * 🔍 检查令牌是否即将过期
     */
    boolean isTokenExpiringSoon(String accessToken, java.time.Duration threshold);

    /**
     * 📈 批量验证令牌 - 现代化批量操作
     */
    Map<String, TokenValidationResult> batchVerifyTokens(java.util.List<String> accessTokens);

    /**
     * 📈 异步批量验证令牌
     */
    CompletableFuture<Map<String, TokenValidationResult>> batchVerifyTokensAsync(java.util.List<String> accessTokens);

    /**
     * 🔄 批量刷新令牌
     */
    Map<String, Optional<AuthResponse>> batchRefreshTokens(java.util.List<String> refreshTokens);

    /**
     * 📊 获取用户令牌统计
     */
    UserTokenStatistics getUserTokenStatistics(Long userId);

    /**
     * 🧹 清理过期令牌
     */
    TokenCleanupResult cleanupExpiredTokens();

    // =================================
    // 现代化数据结构
    // =================================

    /**
     * 🏷️ 令牌类型
     */
    enum TokenType {
        ACCESS("访问令牌"),
        REFRESH("刷新令牌"),
        VERIFICATION("验证令牌");

        private final String description;

        TokenType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 📋 令牌状态
     */
    enum TokenStatus {
        VALID("有效"),
        EXPIRED("已过期"),
        REVOKED("已撤销"),
        INVALID("无效"),
        BLACKLISTED("已列入黑名单");

        private final String description;

        TokenStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public boolean isUsable() {
            return this == VALID;
        }
    }

    /**
     * 📊 令牌详细信息
     */
    record TokenDetails(
            String token,
            TokenType type,
            Long userId,
            String username,
            java.time.Instant issuedAt,
            java.time.Instant expiresAt,
            java.time.Duration remainingTime,
            TokenStatus status,
            String clientType,
            String deviceId,
            java.util.Map<String, Object> claims
    ) {
        public TokenDetails {
            java.util.Objects.requireNonNull(token, "令牌不能为空");
            java.util.Objects.requireNonNull(type, "令牌类型不能为空");
            java.util.Objects.requireNonNull(status, "令牌状态不能为空");
            claims = claims == null ? java.util.Map.of() : java.util.Map.copyOf(claims);
        }

        public boolean isExpired() {
            return status == TokenStatus.EXPIRED ||
                    (expiresAt != null && java.time.Instant.now().isAfter(expiresAt));
        }

        public boolean isValid() {
            return status == TokenStatus.VALID && !isExpired();
        }

        public boolean isExpiringSoon(java.time.Duration threshold) {
            return remainingTime != null && remainingTime.compareTo(threshold) <= 0;
        }
    }

    /**
     * ✅ 令牌验证结果
     */
    record TokenValidationResult(
            boolean valid,
            TokenStatus status,
            String reason,
            java.time.Instant validatedAt,
            Long userId,
            java.util.Set<String> permissions
    ) {
        public static TokenValidationResult valid(Long userId, java.util.Set<String> permissions) {
            return new TokenValidationResult(true, TokenStatus.VALID, null,
                    java.time.Instant.now(), userId, permissions);
        }

        public static TokenValidationResult invalid(TokenStatus status, String reason) {
            return new TokenValidationResult(false, status, reason,
                    java.time.Instant.now(), null, java.util.Set.of());
        }
    }

    /**
     * 📊 用户令牌统计
     */
    record UserTokenStatistics(
            Long userId,
            String username,
            int totalTokens,
            int activeTokens,
            int expiredTokens,
            java.time.Instant lastTokenGeneration,
            java.util.Map<String, Integer> tokensByType,
            java.util.Map<String, Integer> tokensByClient
    ) {
        public double getActiveTokenRatio() {
            return totalTokens > 0 ? (double) activeTokens / totalTokens : 0.0;
        }

        public boolean hasActiveTokens() {
            return activeTokens > 0;
        }
    }

    /**
     * 🧹 令牌清理结果
     */
    record TokenCleanupResult(
            int totalScanned,
            int expiredTokensRemoved,
            int revokedTokensRemoved,
            java.time.Duration cleanupDuration,
            java.time.Instant cleanupTime,
            java.util.Map<TokenType, Integer> removedByType
    ) {
        public int getTotalRemoved() {
            return expiredTokensRemoved + revokedTokensRemoved;
        }

        public double getCleanupEfficiency() {
            return totalScanned > 0 ? (double) getTotalRemoved() / totalScanned : 0.0;
        }
    }
}

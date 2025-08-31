package com.xypai.security.oauth.service.business;

import com.xypai.security.oauth.auth.dto.request.AuthRequest;
import com.xypai.security.oauth.auth.dto.response.AuthResponse;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 🔐 现代化认证业务接口
 * <p>
 * XV03:09 SERVICE层 - 现代化认证业务抽象
 * 支持同步/异步、响应式编程、多种认证策略
 *
 * @author xypai
 * @since 3.0.0
 */
public interface AuthBusiness {

    /**
     * 🔐 同步认证
     */
    Optional<AuthResponse> authenticate(AuthRequest authRequest);

    /**
     * 🔐 异步认证 - 现代化特性
     */
    CompletableFuture<Optional<AuthResponse>> authenticateAsync(AuthRequest authRequest);

    /**
     * 🔍 验证用户凭据
     */
    boolean validateCredentials(AuthRequest authRequest);

    /**
     * 🔍 异步验证用户凭据
     */
    CompletableFuture<Boolean> validateCredentialsAsync(AuthRequest authRequest);

    /**
     * 👤 根据用户标识获取用户信息
     */
    Optional<AuthResponse.UserInfo> getUserByUsername(String username);

    /**
     * 📋 检查用户账户状态
     */
    AccountStatus checkAccountStatus(String username);

    /**
     * 🔒 检查认证类型是否支持
     */
    boolean supportsAuthType(String authType);

    /**
     * 📊 获取支持的认证类型列表
     */
    java.util.Set<String> getSupportedAuthTypes();

    /**
     * 📈 认证统计信息
     */
    AuthStatistics getAuthStatistics(String username);

    /**
     * 📱 发送短信验证码
     */
    Optional<SmsCodeResult> sendSmsCode(String mobile, String clientType);

    /**
     * 📱 异步发送短信验证码
     */
    CompletableFuture<Optional<SmsCodeResult>> sendSmsCodeAsync(String mobile, String clientType);

    // =================================
    // 现代化数据结构
    // =================================

    /**
     * 📋 账户状态枚举
     */
    enum AccountStatus {
        ACTIVE("正常"),
        DISABLED("已禁用"),
        LOCKED("已锁定"),
        PASSWORD_EXPIRED("密码过期"),
        PENDING_VERIFICATION("待验证");

        private final String description;

        AccountStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public boolean isUsable() {
            return this == ACTIVE;
        }
    }

    /**
     * 📊 认证统计信息
     */
    record AuthStatistics(
            String username,
            int totalAttempts,
            int failedAttempts,
            int successfulAttempts,
            java.time.Instant lastLoginTime,
            java.time.Instant lastFailedTime,
            AccountStatus currentStatus,
            java.time.Duration lockRemainingTime
    ) {
        /**
         * 是否接近锁定阈值
         */
        public boolean isNearLockThreshold(int maxAttempts) {
            return failedAttempts >= maxAttempts * 0.8;
        }

        /**
         * 失败率
         */
        public double getFailureRate() {
            return totalAttempts > 0 ? (double) failedAttempts / totalAttempts : 0.0;
        }
    }

    /**
     * 🎯 认证上下文
     */
    record AuthContext(
            String clientType,
            String deviceId,
            String userAgent,
            String clientIp,
            java.time.Instant requestTime,
            java.util.Map<String, String> additionalData
    ) {
        public AuthContext {
            java.util.Objects.requireNonNull(requestTime, "请求时间不能为空");
            additionalData = additionalData == null ? java.util.Map.of() : java.util.Map.copyOf(additionalData);
        }

        public static AuthContext create(String clientType, String clientIp) {
            return new AuthContext(clientType, null, null, clientIp, java.time.Instant.now(), null);
        }
    }

    /**
     * 📱 短信验证码结果
     */
    record SmsCodeResult(
            String mobile,
            String codeId,
            int expiresIn,
            java.time.Instant sentTime,
            boolean success,
            String message
    ) {
        public static SmsCodeResult success(String mobile, String codeId, int expiresIn) {
            return new SmsCodeResult(mobile, codeId, expiresIn, java.time.Instant.now(), true, "验证码发送成功");
        }

        public static SmsCodeResult failure(String mobile, String message) {
            return new SmsCodeResult(mobile, null, 0, java.time.Instant.now(), false, message);
        }
    }
}

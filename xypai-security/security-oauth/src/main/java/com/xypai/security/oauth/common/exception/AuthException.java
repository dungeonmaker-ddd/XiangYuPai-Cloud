package com.xypai.security.oauth.common.exception;

import java.util.Map;

/**
 * 🚨 现代化认证异常
 * <p>
 * XV03:08 COMMON层 - 现代化异常处理
 * 使用Sealed类 + Records + 结构化错误信息
 *
 * @author xypai
 * @since 3.0.0
 */
public sealed class AuthException extends RuntimeException
        permits AuthException.InvalidCredentialsException,
        AuthException.AccountDisabledException,
        AuthException.AccountLockedException,
        AuthException.PasswordExpiredException,
        AuthException.UnsupportedAuthTypeException,
        AuthException.InvalidTokenException,
        AuthException.TokenExpiredException,
        AuthException.TooManyAttemptsException,
        AuthException.PermissionDeniedException {

    private final String errorCode;
    private final Map<String, Object> details;
    private final String traceId;

    protected AuthException(String errorCode, String message, Map<String, Object> details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details == null ? Map.of() : Map.copyOf(details);
        this.traceId = java.util.UUID.randomUUID().toString();
    }

    protected AuthException(String errorCode, String message, Map<String, Object> details) {
        this(errorCode, message, details, null);
    }

    protected AuthException(String errorCode, String message) {
        this(errorCode, message, Map.of(), null);
    }

    public static InvalidCredentialsException invalidCredentials(String username) {
        return new InvalidCredentialsException(username);
    }

    public static InvalidCredentialsException invalidCredentials(String username, String authType) {
        return new InvalidCredentialsException(username, authType);
    }

    public static AccountDisabledException accountDisabled(String username) {
        return new AccountDisabledException(username);
    }

    public static AccountDisabledException accountDisabled(String username, String reason) {
        return new AccountDisabledException(username, reason);
    }

    public static AccountLockedException accountLocked(String username, java.time.Duration lockDuration) {
        return new AccountLockedException(username, lockDuration);
    }

    // =================================
    // 具体异常类型 - 使用Sealed类限制继承
    // =================================

    public static PasswordExpiredException passwordExpired(String username, java.time.Instant expiredDate) {
        return new PasswordExpiredException(username, expiredDate);
    }

    public static UnsupportedAuthTypeException unsupportedAuthType(String authType, java.util.Set<String> supportedTypes) {
        return new UnsupportedAuthTypeException(authType, supportedTypes);
    }

    public static InvalidTokenException invalidToken(String tokenType) {
        return new InvalidTokenException(tokenType);
    }

    public static InvalidTokenException invalidToken(String tokenType, String reason) {
        return new InvalidTokenException(tokenType, reason);
    }

    public static TokenExpiredException tokenExpired(String tokenType, java.time.Instant expiredTime) {
        return new TokenExpiredException(tokenType, expiredTime);
    }

    public static TooManyAttemptsException tooManyAttempts(String operation, int maxAttempts, java.time.Duration retryAfter) {
        return new TooManyAttemptsException(operation, maxAttempts, retryAfter);
    }

    public static PermissionDeniedException permissionDenied(String username, String resource, String action) {
        return new PermissionDeniedException(username, resource, action);
    }

    public static PermissionDeniedException permissionDenied(String username, java.util.Set<String> requiredPermissions) {
        return new PermissionDeniedException(username, requiredPermissions);
    }

    // Getters
    public String getErrorCode() {
        return errorCode;
    }

    // =================================
    // 现代化静态工厂方法
    // =================================

    public Map<String, Object> getDetails() {
        return details;
    }

    public String getTraceId() {
        return traceId;
    }

    /**
     * 获取完整的错误信息
     */
    public ErrorInfo getErrorInfo() {
        return new ErrorInfo(
                errorCode,
                getMessage(),
                details,
                traceId,
                java.time.Instant.now()
        );
    }

    /**
     * 错误信息Record
     */
    public record ErrorInfo(
            String errorCode,
            String message,
            Map<String, Object> details,
            String traceId,
            java.time.Instant timestamp
    ) {
    }

    /**
     * 🔐 无效凭据异常
     */
    public static final class InvalidCredentialsException extends AuthException {
        public InvalidCredentialsException(String username) {
            super("INVALID_CREDENTIALS",
                    "用户名或密码错误",
                    Map.of("username", username));
        }

        public InvalidCredentialsException(String username, String authType) {
            super("INVALID_CREDENTIALS",
                    "认证凭据无效: %s".formatted(authType),
                    Map.of("username", username, "auth_type", authType));
        }
    }

    /**
     * 🚫 账户禁用异常
     */
    public static final class AccountDisabledException extends AuthException {
        public AccountDisabledException(String username) {
            super("ACCOUNT_DISABLED",
                    "用户账户已禁用",
                    Map.of("username", username));
        }

        public AccountDisabledException(String username, String reason) {
            super("ACCOUNT_DISABLED",
                    "用户账户已禁用: %s".formatted(reason),
                    Map.of("username", username, "reason", reason));
        }
    }

    /**
     * 🔒 账户锁定异常
     */
    public static final class AccountLockedException extends AuthException {
        public AccountLockedException(String username, java.time.Duration lockDuration) {
            super("ACCOUNT_LOCKED",
                    "用户账户已锁定，请%d分钟后重试".formatted(lockDuration.toMinutes()),
                    Map.of("username", username,
                            "lock_duration_minutes", lockDuration.toMinutes(),
                            "unlock_time", java.time.Instant.now().plus(lockDuration)));
        }
    }

    /**
     * ⏰ 密码过期异常
     */
    public static final class PasswordExpiredException extends AuthException {
        public PasswordExpiredException(String username, java.time.Instant expiredDate) {
            super("PASSWORD_EXPIRED",
                    "密码已过期，请更新密码",
                    Map.of("username", username,
                            "expired_date", expiredDate));
        }
    }

    /**
     * ❌ 不支持的认证类型异常
     */
    public static final class UnsupportedAuthTypeException extends AuthException {
        public UnsupportedAuthTypeException(String authType, java.util.Set<String> supportedTypes) {
            super("UNSUPPORTED_AUTH_TYPE",
                    "不支持的认证类型: %s".formatted(authType),
                    Map.of("auth_type", authType,
                            "supported_types", supportedTypes));
        }
    }

    /**
     * 🎫 无效令牌异常
     */
    public static final class InvalidTokenException extends AuthException {
        public InvalidTokenException(String tokenType) {
            super("INVALID_TOKEN",
                    "无效的%s令牌".formatted(tokenType),
                    Map.of("token_type", tokenType));
        }

        public InvalidTokenException(String tokenType, String reason) {
            super("INVALID_TOKEN",
                    "无效的%s令牌: %s".formatted(tokenType, reason),
                    Map.of("token_type", tokenType, "reason", reason));
        }
    }

    /**
     * ⌛ 令牌过期异常
     */
    public static final class TokenExpiredException extends AuthException {
        public TokenExpiredException(String tokenType, java.time.Instant expiredTime) {
            super("TOKEN_EXPIRED",
                    "%s令牌已过期".formatted(tokenType),
                    Map.of("token_type", tokenType,
                            "expired_time", expiredTime));
        }
    }

    /**
     * 🚦 尝试次数过多异常
     */
    public static final class TooManyAttemptsException extends AuthException {
        public TooManyAttemptsException(String operation, int maxAttempts, java.time.Duration retryAfter) {
            super("TOO_MANY_ATTEMPTS",
                    "%s尝试次数过多，请%d分钟后重试".formatted(operation, retryAfter.toMinutes()),
                    Map.of("operation", operation,
                            "max_attempts", maxAttempts,
                            "retry_after_minutes", retryAfter.toMinutes(),
                            "retry_time", java.time.Instant.now().plus(retryAfter)));
        }
    }

    /**
     * 🛡️ 权限拒绝异常
     */
    public static final class PermissionDeniedException extends AuthException {
        public PermissionDeniedException(String username, String resource, String action) {
            super("PERMISSION_DENIED",
                    "用户%s没有权限对资源%s执行%s操作".formatted(username, resource, action),
                    Map.of("username", username,
                            "resource", resource,
                            "action", action));
        }

        public PermissionDeniedException(String username, java.util.Set<String> requiredPermissions) {
            super("PERMISSION_DENIED",
                    "用户%s缺少必要权限".formatted(username),
                    Map.of("username", username,
                            "required_permissions", requiredPermissions));
        }
    }
}
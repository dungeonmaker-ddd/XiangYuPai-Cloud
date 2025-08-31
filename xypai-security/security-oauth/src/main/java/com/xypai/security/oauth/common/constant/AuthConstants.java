package com.xypai.security.oauth.common.constant;

/**
 * 🔐 认证常量
 * <p>
 * XV02:28 认证相关的常量定义
 * 统一管理认证过程中使用的常量
 *
 * @author xypai
 * @since 2.0.0
 */
public final class AuthConstants {

    // ========== 认证类型 ==========
    public static final String AUTH_TYPE_PASSWORD = "password";
    public static final String AUTH_TYPE_SMS = "sms";
    public static final String AUTH_TYPE_WECHAT = "wechat";
    // ========== 客户端类型 ==========
    public static final String CLIENT_TYPE_WEB = "web";
    public static final String CLIENT_TYPE_APP = "app";
    public static final String CLIENT_TYPE_MINI = "mini";
    // ========== 令牌类型 ==========
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";
    public static final String TOKEN_TYPE_BEARER = "Bearer";
    // ========== 默认角色 ==========
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_GUEST = "GUEST";
    // ========== 默认权限 ==========
    public static final String PERMISSION_USER_READ = "user:read";
    public static final String PERMISSION_USER_WRITE = "user:write";
    public static final String PERMISSION_SYSTEM_CONFIG = "system:config";
    public static final String PERMISSION_ADMIN_ALL = "admin:all";
    public static final String PERMISSION_PROFILE_EDIT = "profile:edit";
    // ========== 缓存键前缀 ==========
    public static final String CACHE_ACCESS_TOKEN_PREFIX = "auth:access_token:";
    public static final String CACHE_REFRESH_TOKEN_PREFIX = "auth:refresh_token:";
    public static final String CACHE_USER_TOKEN_PREFIX = "auth:user_tokens:";
    public static final String CACHE_USER_PREFIX = "auth:user:";
    public static final String CACHE_SMS_CODE_PREFIX = "auth:sms:";
    // ========== 请求头 ==========
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CLIENT_TYPE = "X-Client-Type";
    public static final String HEADER_DEVICE_ID = "X-Device-Id";
    public static final String HEADER_USER_AGENT = "User-Agent";
    // ========== 认证配置 ==========
    public static final int DEFAULT_PASSWORD_MIN_LENGTH = 6;
    public static final int DEFAULT_PASSWORD_MAX_LENGTH = 128;
    public static final int DEFAULT_USERNAME_MIN_LENGTH = 3;
    public static final int DEFAULT_USERNAME_MAX_LENGTH = 50;
    public static final int DEFAULT_SMS_CODE_LENGTH = 6;
    public static final int DEFAULT_SMS_CODE_EXPIRE_MINUTES = 5;
    public static final int DEFAULT_MAX_LOGIN_ATTEMPTS = 5;
    public static final int DEFAULT_ACCOUNT_LOCK_MINUTES = 30;
    // ========== 令牌配置 ==========
    public static final long DEFAULT_ACCESS_TOKEN_EXPIRE_SECONDS = 86400L; // 24小时
    public static final long DEFAULT_REFRESH_TOKEN_EXPIRE_SECONDS = 604800L; // 7天
    public static final long DEFAULT_WEB_TOKEN_EXPIRE_SECONDS = 7200L; // 2小时
    public static final long DEFAULT_APP_TOKEN_EXPIRE_SECONDS = 86400L; // 24小时
    public static final long DEFAULT_MINI_TOKEN_EXPIRE_SECONDS = 86400L; // 24小时
    // ========== 业务配置 ==========
    public static final String DEFAULT_DISPLAY_NAME_SUFFIX = "用户";
    public static final String DEFAULT_AVATAR_URL = "/images/default-avatar.png";
    public static final String SYSTEM_USER = "system";
    public static final String ANONYMOUS_USER = "anonymous";
    // ========== 正则表达式 ==========
    public static final String REGEX_MOBILE = "^1[3-9]\\d{9}$";
    public static final String REGEX_EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static final String REGEX_USERNAME = "^[a-zA-Z0-9_-]{3,20}$";
    public static final String REGEX_SMS_CODE = "^\\d{6}$";
    // ========== 错误码 ==========
    public static final String ERROR_CODE_INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String ERROR_CODE_ACCOUNT_DISABLED = "ACCOUNT_DISABLED";
    public static final String ERROR_CODE_ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
    public static final String ERROR_CODE_PASSWORD_EXPIRED = "PASSWORD_EXPIRED";
    public static final String ERROR_CODE_INVALID_TOKEN = "INVALID_TOKEN";
    public static final String ERROR_CODE_TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String ERROR_CODE_UNSUPPORTED_AUTH_TYPE = "UNSUPPORTED_AUTH_TYPE";
    public static final String ERROR_CODE_TOO_MANY_ATTEMPTS = "TOO_MANY_ATTEMPTS";
    // 私有构造函数，防止实例化
    private AuthConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
}

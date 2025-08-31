package com.xypai.security.oauth.service.business.impl;

import com.xypai.common.core.domain.R;
import com.xypai.common.redis.service.RedisService;
import com.xypai.security.oauth.auth.dto.request.AuthRequest;
import com.xypai.security.oauth.auth.dto.response.AuthResponse;
import com.xypai.security.oauth.common.exception.AuthException;
import com.xypai.security.oauth.feign.UserServiceFeign;
import com.xypai.security.oauth.feign.dto.UserResponse;
import com.xypai.security.oauth.service.business.AuthBusiness;
import com.xypai.security.oauth.service.util.ModernJwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 🔐 现代化认证业务实现
 * <p>
 * XV03:12 SERVICE层 - 现代化认证业务实现
 * 支持异步处理、智能验证、账户状态管理
 *
 * @author xypai
 * @since 3.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModernAuthBusinessImpl implements AuthBusiness {

    // Redis键前缀常量
    private static final String SMS_CODE_PREFIX = "auth:sms:code:";          // 存储验证码
    private static final String SMS_ATTEMPTS_PREFIX = "auth:sms:attempts:";  // 存储尝试次数
    private static final String SMS_SEND_TIME_PREFIX = "auth:sms:time:";     // 存储发送时间
    private static final int SMS_CODE_EXPIRE_SECONDS = 300; // 5分钟
    private static final int SMS_SEND_LIMIT_SECONDS = 60;   // 60秒发送限制
    private static final int MAX_SMS_ATTEMPTS = 3;          // 最大尝试次数
    private final PasswordEncoder passwordEncoder;
    private final ModernJwtUtil modernJwtUtil;
    private final RedisService redisService;
    private final UserServiceFeign userServiceFeign;
    // MVP版本：内存存储（生产环境应使用数据库+Redis）
    private final Map<String, UserData> userStore = new ConcurrentHashMap<>();
    private final Map<String, AuthStatistics> authStatsStore = new ConcurrentHashMap<>();

    @jakarta.annotation.PostConstruct
    private void init() {
        initTestData();
    }

    @Override
    public Optional<AuthResponse> authenticate(AuthRequest authRequest) {
        log.info("认证请求: username={}, authType={}", authRequest.username(), authRequest.authType());

        try {
            // 1. 检查认证类型支持
            if (!supportsAuthType(authRequest.authType())) {
                throw AuthException.unsupportedAuthType(authRequest.authType(), getSupportedAuthTypes());
            }

            // 2. 检查账户状态
            var accountStatus = checkAccountStatus(authRequest.username());
            if (!accountStatus.isUsable()) {
                handleAccountStatusError(authRequest.username(), accountStatus);
            }

            // 3. 验证凭据
            if (!validateCredentials(authRequest)) {
                recordFailedAttempt(authRequest.username());
                throw AuthException.invalidCredentials(authRequest.username(), authRequest.authType());
            }

            // 4. 获取用户信息并创建响应
            var userInfo = getUserByUsername(authRequest.username()).orElseThrow(
                    () -> AuthException.invalidCredentials(authRequest.username())
            );

            // 5. 记录成功登录
            recordSuccessfulAttempt(authRequest.username());

            log.info("认证成功: username={}", authRequest.username());
            return Optional.of(createAuthResponse(userInfo));

        } catch (AuthException e) {
            log.warn("认证失败: username={}, error={}", authRequest.username(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("认证过程异常: username={}, error={}", authRequest.username(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public CompletableFuture<Optional<AuthResponse>> authenticateAsync(AuthRequest authRequest) {
        return CompletableFuture.supplyAsync(() -> authenticate(authRequest));
    }

    @Override
    public boolean validateCredentials(AuthRequest authRequest) {
        return switch (authRequest.authType()) {
            case "password" -> validatePasswordCredentials(authRequest);
            case "sms" -> validateSmsCredentials(authRequest);
            case "wechat" -> validateWechatCredentials(authRequest);
            default -> false;
        };
    }

    @Override
    public CompletableFuture<Boolean> validateCredentialsAsync(AuthRequest authRequest) {
        return CompletableFuture.supplyAsync(() -> validateCredentials(authRequest));
    }

    @Override
    public Optional<AuthResponse.UserInfo> getUserByUsername(String username) {
        try {
            R<UserResponse> userResult = userServiceFeign.getUserByUsername(username);
            if (R.isSuccess(userResult) && userResult.getData() != null) {
                return Optional.of(convertToAuthUserInfo(userResult.getData()));
            } else {
                log.warn("用户服务未找到用户: username={}", username);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("调用用户服务获取用户信息失败: username={}", username, e);
            return Optional.empty();
        }
    }

    @Override
    public AccountStatus checkAccountStatus(String username) {
        var userData = userStore.get(username.toLowerCase());
        if (userData == null) {
            return AccountStatus.DISABLED;
        }

        // 检查账户是否被锁定
        var stats = authStatsStore.get(username.toLowerCase());
        if (stats != null && stats.failedAttempts() >= 5) {
            var lockTime = stats.lastFailedTime().plus(Duration.ofMinutes(30));
            if (Instant.now().isBefore(lockTime)) {
                return AccountStatus.LOCKED;
            }
        }

        return userData.enabled ? AccountStatus.ACTIVE : AccountStatus.DISABLED;
    }

    @Override
    public boolean supportsAuthType(String authType) {
        return getSupportedAuthTypes().contains(authType);
    }

    @Override
    public Set<String> getSupportedAuthTypes() {
        return Set.of("password", "sms", "wechat");
    }

    @Override
    public AuthStatistics getAuthStatistics(String username) {
        return authStatsStore.getOrDefault(username.toLowerCase(),
                new AuthStatistics(username, 0, 0, 0, null, null, AccountStatus.ACTIVE, Duration.ZERO));
    }

    @Override
    public Optional<SmsCodeResult> sendSmsCode(String mobile, String clientType) {
        log.info("发送短信验证码: mobile={}, clientType={}", mobile, clientType);

        try {
            // 1. 验证手机号格式
            if (!isValidMobile(mobile)) {
                return Optional.of(SmsCodeResult.failure(mobile, "无效的手机号格式"));
            }

            // 2. 检查频率限制（防刷）
            if (isSmsSendTooFrequent(mobile)) {
                return Optional.of(SmsCodeResult.failure(mobile, "发送过于频繁，请稍后重试"));
            }

            // 3. 生成验证码
            String code = generateSmsCode();
            String codeId = "sms_" + System.currentTimeMillis();

            // 4. 调用SMS微服务发送验证码
            boolean sent = sendSmsToService(mobile, code);

            if (sent) {
                // 5. 存储验证码（用于后续验证）
                storeSmsCode(mobile, code, codeId);

                // 6. 记录发送日志
                recordSmsSent(mobile);

                return Optional.of(SmsCodeResult.success(mobile, codeId, 300)); // 5分钟有效期
            } else {
                return Optional.of(SmsCodeResult.failure(mobile, "短信发送失败"));
            }

        } catch (Exception e) {
            log.error("发送短信验证码异常: mobile={}, error={}", mobile, e.getMessage(), e);
            return Optional.of(SmsCodeResult.failure(mobile, "系统错误，请稍后重试"));
        }
    }

    @Override
    public CompletableFuture<Optional<SmsCodeResult>> sendSmsCodeAsync(String mobile, String clientType) {
        return CompletableFuture.supplyAsync(() -> sendSmsCode(mobile, clientType));
    }

    // ================================
    // 私有辅助方法
    // ================================

    private boolean validatePasswordCredentials(AuthRequest authRequest) {
        try {
            // 调用用户服务获取用户信息
            R<UserResponse> userResult = userServiceFeign.getUserByUsername(authRequest.username());

            if (!R.isSuccess(userResult) || userResult.getData() == null) {
                log.warn("用户不存在或服务调用失败: username={}, result={}",
                        authRequest.username(), userResult.getMsg());
                return false;
            }

            UserResponse user = userResult.getData();

            // 检查用户状态（1=激活）
            if (!Integer.valueOf(1).equals(user.status())) {
                log.warn("用户状态异常: username={}, status={}", authRequest.username(), user.status());
                return false;
            }

            // TODO: 实际场景中，应该调用用户服务的密码验证接口
            // 这里临时使用默认密码 "123456" 进行验证
            String defaultPassword = "123456";
            boolean isValid = defaultPassword.equals(authRequest.password());

            log.info("密码验证结果: username={}, valid={}", authRequest.username(), isValid);
            return isValid;

        } catch (Exception e) {
            log.error("调用用户服务验证密码失败: username={}", authRequest.username(), e);
            return false;
        }
    }

    private boolean validateSmsCredentials(AuthRequest authRequest) {
        // 使用真实的短信验证码验证逻辑
        return validateSmsCode(authRequest.mobile(), authRequest.smsCode());
    }

    private boolean validateWechatCredentials(AuthRequest authRequest) {
        // MVP版本：模拟微信验证
        return authRequest.wechatCode() != null && authRequest.wechatCode().startsWith("wx_");
    }

    private void handleAccountStatusError(String username, AccountStatus status) {
        switch (status) {
            case DISABLED -> throw AuthException.accountDisabled(username);
            case LOCKED -> {
                var lockDuration = Duration.ofMinutes(30);
                throw AuthException.accountLocked(username, lockDuration);
            }
            case PASSWORD_EXPIRED -> {
                var expiredDate = Instant.now().minus(Duration.ofDays(90));
                throw AuthException.passwordExpired(username, expiredDate);
            }
            case ACTIVE, PENDING_VERIFICATION -> {
                // 这些状态不应该进入错误处理
                throw new IllegalStateException("不期望的账户状态: " + status);
            }
        }
    }

    // ================================
    // 短信验证码相关方法
    // ================================

    /**
     * 验证手机号格式
     */
    private boolean isValidMobile(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) {
            return false;
        }
        // 简单的中国手机号验证
        return mobile.matches("^1[3-9]\\d{9}$");
    }

    /**
     * 检查短信发送频率（防刷）- 基于Redis
     */
    private boolean isSmsSendTooFrequent(String mobile) {
        String key = SMS_SEND_TIME_PREFIX + mobile;
        Long lastSentTime = redisService.getCacheObject(key);

        if (lastSentTime == null) {
            return false;
        }

        // 检查是否在60秒内
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastSentTime) < (SMS_SEND_LIMIT_SECONDS * 1000);
    }

    /**
     * 生成6位数字验证码
     */
    private String generateSmsCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    /**
     * 调用SMS微服务发送验证码
     */
    private boolean sendSmsToService(String mobile, String code) {
        try {
            // TODO: 集成真实的SMS微服务调用
            // SmsService.sendVerificationCode(mobile, code);

            // MVP版本：模拟发送成功
            log.info("模拟发送短信验证码: mobile={}, code={}", mobile, code);

            // 模拟SMS微服务处理时间
            Thread.sleep(100);

            return true;
        } catch (Exception e) {
            log.error("调用SMS微服务失败: mobile={}, error={}", mobile, e.getMessage());
            return false;
        }
    }

    /**
     * 存储验证码用于后续验证 - 基于Redis（明文存储）
     */
    private void storeSmsCode(String mobile, String code, String codeId) {
        String codeKey = SMS_CODE_PREFIX + mobile;
        String attemptsKey = SMS_ATTEMPTS_PREFIX + mobile;

        // 存储验证码（明文）和重置尝试次数
        redisService.setCacheObject(codeKey, code, (long) SMS_CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        redisService.setCacheObject(attemptsKey, 0, (long) SMS_CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        log.info("存储短信验证码: mobile={}, codeId={}", mobile, codeId);
    }

    /**
     * 记录短信发送时间 - 基于Redis
     */
    private void recordSmsSent(String mobile) {
        String key = SMS_SEND_TIME_PREFIX + mobile;
        long currentTime = System.currentTimeMillis();

        // 存储发送时间，60秒过期
        redisService.setCacheObject(key, currentTime, (long) SMS_SEND_LIMIT_SECONDS, TimeUnit.SECONDS);

        log.info("记录短信发送: mobile={}, time={}",
                mobile, Instant.ofEpochMilli(currentTime));
    }

    /**
     * 验证短信验证码 - 基于Redis（明文验证）
     */
    private boolean validateSmsCode(String mobile, String inputCode) {
        String codeKey = SMS_CODE_PREFIX + mobile;
        String attemptsKey = SMS_ATTEMPTS_PREFIX + mobile;

        // 1. 检查验证码是否存在
        String storedCode = redisService.getCacheObject(codeKey);
        if (storedCode == null) {
            log.warn("短信验证码不存在或已过期: mobile={}", mobile);
            return false;
        }

        // 2. 检查尝试次数
        Integer attempts = redisService.getCacheObject(attemptsKey);
        if (attempts == null) {
            attempts = 0;
        }

        if (attempts >= MAX_SMS_ATTEMPTS) {
            log.warn("短信验证码尝试次数超限: mobile={}, attempts={}", mobile, attempts);
            // 清除验证码防止暴力破解
            redisService.deleteObject(codeKey);
            redisService.deleteObject(attemptsKey);
            return false;
        }

        // 3. 验证验证码（明文比较）
        if (!storedCode.equals(inputCode)) {
            // 增加尝试次数
            attempts++;
            redisService.setCacheObject(attemptsKey, attempts, (long) SMS_CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);

            log.warn("短信验证码错误: mobile={}, attempts={}/{}", mobile, attempts, MAX_SMS_ATTEMPTS);
            return false;
        }

        // 4. 验证成功，清除相关数据
        redisService.deleteObject(codeKey);
        redisService.deleteObject(attemptsKey);

        log.info("短信验证码验证成功: mobile={}", mobile);
        return true;
    }


    private void recordFailedAttempt(String username) {
        var stats = authStatsStore.computeIfAbsent(username.toLowerCase(),
                k -> new AuthStatistics(username, 0, 0, 0, null, null, AccountStatus.ACTIVE, Duration.ZERO));

        var newStats = new AuthStatistics(
                stats.username(),
                stats.totalAttempts() + 1,
                stats.failedAttempts() + 1,
                stats.successfulAttempts(),
                stats.lastLoginTime(),
                Instant.now(),
                stats.currentStatus(),
                stats.lockRemainingTime()
        );

        authStatsStore.put(username.toLowerCase(), newStats);
    }

    private void recordSuccessfulAttempt(String username) {
        var stats = authStatsStore.computeIfAbsent(username.toLowerCase(),
                k -> new AuthStatistics(username, 0, 0, 0, null, null, AccountStatus.ACTIVE, Duration.ZERO));

        var newStats = new AuthStatistics(
                stats.username(),
                stats.totalAttempts() + 1,
                0, // 重置失败次数
                stats.successfulAttempts() + 1,
                Instant.now(),
                stats.lastFailedTime(),
                AccountStatus.ACTIVE,
                Duration.ZERO
        );

        authStatsStore.put(username.toLowerCase(), newStats);
    }

    private AuthResponse createAuthResponse(AuthResponse.UserInfo userInfo) {
        // 使用ModernJwtUtil生成真实的JWT令牌
        try {
            String deviceId = "device_" + System.currentTimeMillis(); // 模拟设备ID

            // 生成访问令牌
            String accessToken = modernJwtUtil.generateAccessToken(userInfo, "web", deviceId);

            // 生成刷新令牌
            String refreshToken = modernJwtUtil.generateRefreshToken(userInfo, "web", deviceId);

            // 获取访问令牌过期时间（秒）
            long expiresIn = 86400L; // 默认24小时，实际应从配置获取

            return AuthResponse.create(
                    accessToken,
                    refreshToken,
                    expiresIn,
                    userInfo
            );
        } catch (Exception e) {
            log.error("生成JWT令牌失败", e);
            // 降级处理：返回临时令牌
            return AuthResponse.create(
                    "temp_access_token_" + System.currentTimeMillis(),
                    "temp_refresh_token_" + System.currentTimeMillis(),
                    86400L,
                    userInfo
            );
        }
    }

    private AuthResponse.UserInfo createUserInfo(UserData userData) {
        return switch (userData.type) {
            case "admin" -> new AuthResponse.AdminUser(
                    userData.id, userData.username, userData.displayName,
                    userData.email, userData.mobile, userData.roles,
                    userData.permissions, Instant.now()
            );
            case "guest" -> new AuthResponse.GuestUser(
                    userData.id, userData.username, userData.displayName,
                    userData.email, userData.mobile, userData.roles,
                    userData.permissions, Instant.now()
            );
            default -> new AuthResponse.StandardUser(
                    userData.id, userData.username, userData.displayName,
                    userData.email, userData.mobile, userData.roles,
                    userData.permissions, Instant.now()
            );
        };
    }

    private void initTestData() {
        // 管理员用户
        userStore.put("admin", new UserData(
                1L, "admin", "系统管理员", "admin@xypai.com", "13800138000",
                passwordEncoder.encode("123456"), true, "admin",
                Set.of("ADMIN", "USER"),
                Set.of("user:read", "user:write", "system:config", "admin:all")
        ));

        // 普通用户
        userStore.put("user", new UserData(
                2L, "user", "普通用户", "user@xypai.com", "13800138001",
                passwordEncoder.encode("123456"), true, "user",
                Set.of("USER"),
                Set.of("user:read", "profile:edit")
        ));

        // 访客用户
        userStore.put("guest", new UserData(
                3L, "guest", "访客", null, null,
                passwordEncoder.encode("123456"), true, "guest",
                Set.of("GUEST"),
                Set.of("guest:read")
        ));
    }

    /**
     * 转换用户服务响应为认证用户信息
     */
    private AuthResponse.UserInfo convertToAuthUserInfo(UserResponse user) {
        // 判断用户类型
        String userType = "admin".equals(user.username()) ? "admin" : "user";

        // 创建默认角色和权限
        Set<String> roles = "admin".equals(userType) ?
                Set.of("ADMIN", "USER") : Set.of("USER");
        Set<String> permissions = "admin".equals(userType) ?
                Set.of("user:read", "user:write", "admin:all", "system:config") :
                Set.of("user:read");

        return switch (userType) {
            case "admin" -> new AuthResponse.AdminUser(
                    user.id(), user.username(), user.nickname(),
                    null, user.mobile(), roles,
                    permissions, user.createTime() != null ?
                    user.createTime().atZone(java.time.ZoneId.systemDefault()).toInstant() :
                    Instant.now()
            );
            case "guest" -> new AuthResponse.GuestUser(
                    user.id(), user.username(), user.nickname(),
                    null, user.mobile(), roles,
                    permissions, user.createTime() != null ?
                    user.createTime().atZone(java.time.ZoneId.systemDefault()).toInstant() :
                    Instant.now()
            );
            default -> new AuthResponse.StandardUser(
                    user.id(), user.username(), user.nickname(),
                    null, user.mobile(), roles,
                    permissions, user.createTime() != null ?
                    user.createTime().atZone(java.time.ZoneId.systemDefault()).toInstant() :
                    Instant.now()
            );
        };
    }

    /**
     * 用户数据Record
     */
    private record UserData(
            Long id,
            String username,
            String displayName,
            String email,
            String mobile,
            String password,
            boolean enabled,
            String type,
            Set<String> roles,
            Set<String> permissions
    ) {
    }
}

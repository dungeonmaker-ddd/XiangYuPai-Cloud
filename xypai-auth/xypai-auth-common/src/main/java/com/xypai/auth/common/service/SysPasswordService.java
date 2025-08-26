package com.xypai.auth.common.service;

import com.xypai.common.core.constant.Constants;
import com.xypai.common.core.exception.ServiceException;
import com.xypai.common.core.utils.StringUtils;
import com.xypai.common.security.utils.SecurityUtils;
import com.xypai.system.api.domain.SysUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 🔐 系统密码服务
 * <p>
 * 提供密码相关的验证和管理功能：
 * - 密码验证
 * - 密码复杂度检查
 * - 密码错误次数统计
 * - 账户锁定控制
 *
 * @author xypai
 * @version 4.1.0
 */
@Service
public class SysPasswordService {

    private static final Logger logger = LoggerFactory.getLogger(SysPasswordService.class);
    // 最大密码错误次数
    private static final int MAX_RETRY_COUNT = 5;
    // 账户锁定时间（30分钟）
    private static final long LOCK_TIME_MILLIS = 30 * 60 * 1000L;
    private final SysRecordLogService recordLogService;
    // 密码错误次数缓存 (用户名 -> 错误次数)
    private final ConcurrentHashMap<String, AtomicInteger> passwordRetryCache = new ConcurrentHashMap<>();
    // 账户锁定缓存 (用户名 -> 锁定时间)
    private final ConcurrentHashMap<String, Long> accountLockCache = new ConcurrentHashMap<>();

    public SysPasswordService(SysRecordLogService recordLogService) {
        this.recordLogService = recordLogService;
    }

    /**
     * 🔍 验证密码
     */
    public void validate(SysUser user, String password) {
        String username = user.getUserName();

        // 检查账户是否被锁定
        if (isAccountLocked(username)) {
            long lockTime = accountLockCache.get(username);
            long remainingTime = (lockTime + LOCK_TIME_MILLIS - System.currentTimeMillis()) / 1000 / 60;
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL,
                    String.format("账户已锁定，剩余时间 %d 分钟", remainingTime));
            throw new ServiceException(String.format("账户已锁定，请 %d 分钟后重试", remainingTime));
        }

        // 验证密码
        if (!matches(user, password)) {
            // 记录密码错误次数
            int retryCount = recordPasswordError(username);

            // 检查是否达到锁定条件
            if (retryCount >= MAX_RETRY_COUNT) {
                lockAccount(username);
                recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL,
                        String.format("密码错误次数过多，账户已锁定 %d 分钟", LOCK_TIME_MILLIS / 60 / 1000));
                throw new ServiceException(String.format("密码错误次数过多，账户已锁定 %d 分钟", LOCK_TIME_MILLIS / 60 / 1000));
            }

            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL,
                    String.format("密码错误，剩余重试次数：%d", MAX_RETRY_COUNT - retryCount));
            throw new ServiceException(String.format("密码错误，剩余重试次数：%d", MAX_RETRY_COUNT - retryCount));
        }

        // 密码验证成功，清除错误次数
        clearPasswordError(username);
        logger.debug("✅ 密码验证成功 - 用户: {}", username);
    }

    /**
     * 🔍 密码匹配验证
     */
    private boolean matches(SysUser user, String rawPassword) {
        return SecurityUtils.matchesPassword(rawPassword, user.getPassword());
    }

    /**
     * 📊 记录密码错误次数
     */
    private int recordPasswordError(String username) {
        AtomicInteger retryCount = passwordRetryCache.computeIfAbsent(username, k -> new AtomicInteger(0));
        int currentCount = retryCount.incrementAndGet();

        logger.warn("🚨 密码验证失败 - 用户: {}, 错误次数: {}/{}", username, currentCount, MAX_RETRY_COUNT);
        return currentCount;
    }

    /**
     * 🔒 锁定账户
     */
    private void lockAccount(String username) {
        accountLockCache.put(username, System.currentTimeMillis());
        logger.warn("🔒 账户已锁定 - 用户: {}, 锁定时间: {} 分钟", username, LOCK_TIME_MILLIS / 60 / 1000);
    }

    /**
     * 🔍 检查账户是否被锁定
     */
    private boolean isAccountLocked(String username) {
        Long lockTime = accountLockCache.get(username);
        if (lockTime == null) {
            return false;
        }

        // 检查锁定时间是否已过期
        if (System.currentTimeMillis() - lockTime > LOCK_TIME_MILLIS) {
            // 锁定时间已过期，移除锁定状态
            accountLockCache.remove(username);
            clearPasswordError(username);
            logger.info("🔓 账户锁定已到期，自动解除锁定 - 用户: {}", username);
            return false;
        }

        return true;
    }

    /**
     * 🗑️ 清除密码错误次数
     */
    private void clearPasswordError(String username) {
        passwordRetryCache.remove(username);
        logger.debug("🗑️ 清除密码错误记录 - 用户: {}", username);
    }

    /**
     * 🔓 手动解锁账户（管理员操作）
     */
    public void unlockAccount(String username) {
        accountLockCache.remove(username);
        clearPasswordError(username);
        logger.info("🔓 手动解锁账户 - 用户: {}", username);
    }

    /**
     * 📊 获取密码错误次数
     */
    public int getPasswordErrorCount(String username) {
        AtomicInteger retryCount = passwordRetryCache.get(username);
        return retryCount != null ? retryCount.get() : 0;
    }

    /**
     * 🔍 检查密码复杂度
     */
    public void validatePasswordComplexity(String password, String username) {
        if (StringUtils.isBlank(password)) {
            throw new ServiceException("密码不能为空");
        }

        // 密码长度检查
        if (password.length() < 6 || password.length() > 50) {
            throw new ServiceException("密码长度必须在6-50个字符之间");
        }

        // 检查是否包含用户名
        if (StringUtils.isNotBlank(username) &&
                password.toLowerCase().contains(username.toLowerCase())) {
            throw new ServiceException("密码不能包含用户名");
        }

        // 检查常见弱密码
        if (isWeakPassword(password)) {
            throw new ServiceException("密码过于简单，请设置更复杂的密码");
        }
    }

    /**
     * 🔍 检查是否为弱密码
     */
    private boolean isWeakPassword(String password) {
        String[] weakPasswords = {
                "123456", "password", "123456789", "12345678", "12345",
                "1234567", "admin", "qwerty", "abc123", "111111"
        };

        String lowerPassword = password.toLowerCase();
        for (String weak : weakPasswords) {
            if (lowerPassword.equals(weak)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 📊 获取密码强度评分
     */
    public int getPasswordStrengthScore(String password) {
        int score = 0;

        if (password.length() >= 8) score += 20;
        if (password.length() >= 12) score += 10;

        if (password.chars().anyMatch(Character::isLowerCase)) score += 15;
        if (password.chars().anyMatch(Character::isUpperCase)) score += 15;
        if (password.chars().anyMatch(Character::isDigit)) score += 15;
        if (password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0)) score += 25;

        return Math.min(score, 100);
    }
}

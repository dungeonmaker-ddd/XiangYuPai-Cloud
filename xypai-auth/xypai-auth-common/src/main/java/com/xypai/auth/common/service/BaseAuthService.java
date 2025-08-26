package com.xypai.auth.common.service;

import com.xypai.common.core.constant.Constants;
import com.xypai.common.core.constant.SecurityConstants;
import com.xypai.common.core.domain.R;
import com.xypai.common.core.enums.UserStatus;
import com.xypai.common.core.exception.ServiceException;
import com.xypai.common.core.utils.StringUtils;
import com.xypai.system.api.RemoteUserService;
import com.xypai.system.api.domain.SysUser;
import com.xypai.system.api.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 🏗️ 基础认证服务抽象类
 * <p>
 * 提供认证服务的公共功能，遵循模板方法模式
 * <p>
 * 功能：
 * - 基础参数验证
 * - 用户信息获取
 * - 用户状态检查
 * - 登录信息记录
 * - 抽象方法供子类实现特定策略
 *
 * @author xypai
 * @version 4.1.0
 */
public abstract class BaseAuthService {

    private static final Logger logger = LoggerFactory.getLogger(BaseAuthService.class);

    protected final RemoteUserService remoteUserService;
    protected final SysPasswordService passwordService;
    protected final SysRecordLogService recordLogService;

    protected BaseAuthService(RemoteUserService remoteUserService,
                              SysPasswordService passwordService,
                              SysRecordLogService recordLogService) {
        this.remoteUserService = Objects.requireNonNull(remoteUserService, "远程用户服务不能为空");
        this.passwordService = Objects.requireNonNull(passwordService, "密码服务不能为空");
        this.recordLogService = Objects.requireNonNull(recordLogService, "日志记录服务不能为空");
    }

    /**
     * 🔍 基础参数验证
     */
    protected void validateBaseParams(String username, String password) {
        if (StringUtils.isBlank(username)) {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户名不能为空");
            throw new ServiceException("用户名不能为空");
        }
        if (StringUtils.isBlank(password)) {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "密码不能为空");
            throw new ServiceException("密码不能为空");
        }
    }

    /**
     * 🔍 用户名长度验证
     */
    protected void validateUsername(String username) {
        if (username.length() < 2 || username.length() > 30) {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户名长度必须在2-30个字符之间");
            throw new ServiceException("用户名长度必须在2-30个字符之间");
        }
    }

    /**
     * 🔍 密码策略验证 - 抽象方法，由子类实现具体策略
     */
    protected abstract void validatePasswordPolicy(String username, String password);

    /**
     * 🔒 安全检查 - 抽象方法，由子类实现具体策略（如IP检查）
     */
    protected abstract void performSecurityChecks(String username);

    /**
     * 👤 获取用户信息
     */
    protected LoginUser getUserInfo(String username) {
        R<LoginUser> userResult = remoteUserService.getUserInfo(username, SecurityConstants.INNER);

        if (R.FAIL == userResult.getCode()) {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户不存在或密码错误");
            throw new ServiceException("用户不存在或密码错误");
        }

        LoginUser userInfo = userResult.getData();
        if (userInfo == null || userInfo.getSysUser() == null) {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户信息获取失败");
            throw new ServiceException("用户信息获取失败");
        }

        return userInfo;
    }

    /**
     * ✅ 验证用户状态
     */
    protected void validateUserStatus(String username, SysUser user) {
        if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户账号已被删除");
            throw new ServiceException("用户账号已被删除");
        }
        if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户账号已停用");
            throw new ServiceException("用户账号已停用");
        }
    }

    /**
     * 📝 记录登录信息（更新最后登录时间）
     */
    protected void recordLoginInfo(Long userId) {
        if (userId != null) {
            try {
                // 更新用户最后登录时间
                // 这里可以调用远程服务更新用户的最后登录时间
                logger.debug("📝 记录用户登录信息 - 用户ID: {}", userId);
            } catch (Exception e) {
                logger.warn("📝 记录用户登录信息失败 - 用户ID: {}, 错误: {}", userId, e.getMessage());
                // 记录失败不影响登录流程
            }
        }
    }

    /**
     * 🛡️ 验证密码复杂度（通用方法）
     */
    protected boolean isPasswordComplex(String password) {
        if (password.length() < 8) {
            return false;
        }

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);

        // 至少包含3种类型
        int types = (hasUpper ? 1 : 0) + (hasLower ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);
        return types >= 3;
    }

    /**
     * 🔍 检查密码是否包含用户名
     */
    protected boolean isPasswordContainsUsername(String password, String username) {
        return password.toLowerCase().contains(username.toLowerCase());
    }

    /**
     * 📊 获取密码强度等级
     */
    protected String getPasswordStrength(String password) {
        if (password.length() < 6) {
            return "弱";
        }
        if (password.length() < 8 || !isPasswordComplex(password)) {
            return "中";
        }
        return "强";
    }
}

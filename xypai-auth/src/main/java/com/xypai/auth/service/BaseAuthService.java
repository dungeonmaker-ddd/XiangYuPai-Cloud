package com.xypai.auth.service;

import com.xypai.common.core.constant.Constants;
import com.xypai.common.core.constant.SecurityConstants;
import com.xypai.common.core.constant.UserConstants;
import com.xypai.common.core.domain.R;
import com.xypai.common.core.enums.UserStatus;
import com.xypai.common.core.exception.ServiceException;
import com.xypai.common.core.utils.DateUtils;
import com.xypai.common.core.utils.StringUtils;
import com.xypai.common.core.utils.ip.IpUtils;
import com.xypai.system.api.RemoteUserService;
import com.xypai.system.api.domain.SysUser;
import com.xypai.system.api.model.LoginUser;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 认证服务基类 - 提取公共认证逻辑
 *
 * @author xypai
 */
@Component
public abstract class BaseAuthService {

    protected final RemoteUserService remoteUserService;
    protected final SysPasswordService passwordService;
    protected final SysRecordLogService recordLogService;

    /**
     * 构造器注入 - Spring Boot 3+ 推荐方式
     *
     * @Lazy 注解解决Feign客户端循环依赖问题
     */
    protected BaseAuthService(@Lazy RemoteUserService remoteUserService,
                              SysPasswordService passwordService,
                              SysRecordLogService recordLogService) {
        this.remoteUserService = remoteUserService;
        this.passwordService = passwordService;
        this.recordLogService = recordLogService;
    }

    /**
     * 基础参数验证
     */
    protected void validateBaseParams(String username, String password) {
        if (StringUtils.isAnyBlank(username, password)) {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户名或密码不能为空");
            throw new ServiceException("用户名或密码不能为空");
        }
    }

    /**
     * 验证用户名长度
     */
    protected void validateUsername(String username) {
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户名长度不符合要求");
            throw new ServiceException("用户名长度不符合要求");
        }
    }

    /**
     * 获取用户信息
     */
    protected LoginUser getUserInfo(String username) {
        R<LoginUser> userResult = remoteUserService.getUserInfo(username, SecurityConstants.INNER);
        if (R.FAIL == userResult.getCode()) {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户不存在");
            throw new ServiceException("用户不存在或密码错误");
        }
        return userResult.getData();
    }

    /**
     * 检查用户状态
     */
    protected void validateUserStatus(String username, SysUser user) {
        if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "账号已被删除");
            throw new ServiceException("账号已被删除");
        }
        if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "账号已停用");
            throw new ServiceException("账号已停用");
        }
    }

    /**
     * 记录登录信息
     */
    protected void recordLoginInfo(Long userId) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(IpUtils.getIpAddr());
        sysUser.setLoginDate(DateUtils.getNowDate());
        remoteUserService.recordUserLogin(sysUser, SecurityConstants.INNER);
    }

    /**
     * 抽象方法：验证密码策略（子类实现不同的策略）
     */
    protected abstract void validatePasswordPolicy(String username, String password);

    /**
     * 抽象方法：额外的安全检查（如IP黑名单等）
     */
    protected abstract void performSecurityChecks(String username);
}

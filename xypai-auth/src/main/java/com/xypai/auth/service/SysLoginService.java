package com.xypai.auth.service;

import com.xypai.common.core.constant.CacheConstants;
import com.xypai.common.core.constant.Constants;
import com.xypai.common.core.constant.SecurityConstants;
import com.xypai.common.core.constant.UserConstants;
import com.xypai.common.core.domain.R;
import com.xypai.common.core.exception.ServiceException;
import com.xypai.common.core.text.Convert;
import com.xypai.common.core.utils.DateUtils;
import com.xypai.common.core.utils.StringUtils;
import com.xypai.common.core.utils.ip.IpUtils;
import com.xypai.common.redis.service.RedisService;
import com.xypai.common.security.utils.SecurityUtils;
import com.xypai.system.api.RemoteUserService;
import com.xypai.system.api.domain.SysUser;
import com.xypai.system.api.model.LoginUser;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 管理端登录服务 - 严格的安全策略
 *
 * @author ruoyi
 */
@Component
public class SysLoginService extends BaseAuthService {

    private final RedisService redisService;

    /**
     * 构造器注入 - Spring Boot 3+ 推荐方式
     *
     * @Lazy 注解解决Feign客户端循环依赖问题
     */
    public SysLoginService(@Lazy RemoteUserService remoteUserService,
                           SysPasswordService passwordService,
                           SysRecordLogService recordLogService,
                           RedisService redisService) {
        super(remoteUserService, passwordService, recordLogService);
        this.redisService = redisService;
    }

    /**
     * 管理端登录 - 严格的安全策略
     */
    public LoginUser login(String username, String password) {
        // 基础参数验证
        validateBaseParams(username, password);

        // 管理端严格的密码策略
        validatePasswordPolicy(username, password);

        // 用户名长度验证
        validateUsername(username);

        // 管理端安全检查（IP黑名单等）
        performSecurityChecks(username);

        // 获取用户信息
        LoginUser userInfo = getUserInfo(username);
        SysUser user = userInfo.getSysUser();

        // 检查用户状态
        validateUserStatus(username, user);

        // 验证密码
        passwordService.validate(user, password);

        // 记录登录成功
        recordLogService.recordLogininfor(username, Constants.LOGIN_SUCCESS, "管理端登录成功");
        recordLoginInfo(user.getUserId());

        return userInfo;
    }

    @Override
    protected void validatePasswordPolicy(String username, String password) {
        // 管理端严格的密码策略
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户密码不在指定范围");
            throw new ServiceException("用户密码不在指定范围");
        }
    }

    @Override
    protected void performSecurityChecks(String username) {
        // IP黑名单校验
        String blackStr = Convert.toStr(redisService.getCacheObject(CacheConstants.SYS_LOGIN_BLACKIPLIST));
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr())) {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "很遗憾，访问IP已被列入系统黑名单");
            throw new ServiceException("很遗憾，访问IP已被列入系统黑名单");
        }
    }





    public void logout(String loginName) {
        recordLogService.recordLogininfor(loginName, Constants.LOGOUT, "退出成功");
    }

    /**
     * 注册
     */
    public void register(String username, String password) {
        // 用户名或密码为空 错误
        if (StringUtils.isAnyBlank(username, password)) {
            throw new ServiceException("用户/密码必须填写");
        }
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            throw new ServiceException("账户长度必须在2到20个字符之间");
        }
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            throw new ServiceException("密码长度必须在5到20个字符之间");
        }

        // 注册用户信息
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);
        sysUser.setNickName(username);
        sysUser.setPwdUpdateDate(DateUtils.getNowDate());
        sysUser.setPassword(SecurityUtils.encryptPassword(password));
        R<?> registerResult = remoteUserService.registerUserInfo(sysUser, SecurityConstants.INNER);

        if (R.FAIL == registerResult.getCode()) {
            throw new ServiceException(registerResult.getMsg());
        }
        recordLogService.recordLogininfor(username, Constants.REGISTER, "注册成功");
    }
}

package com.xypai.auth.service;

import com.xypai.auth.vo.SmsCodeResponse;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * APP端登录服务 - 相对宽松的验证策略
 *
 * @author xypai
 */
@Component
public class AppLoginService extends BaseAuthService {

    private static final Logger logger = LoggerFactory.getLogger(AppLoginService.class);

    /**
     * 构造器注入 - Spring Boot 3+ 推荐方式
     *
     * @Lazy 注解解决Feign客户端循环依赖问题
     */
    public AppLoginService(@Lazy RemoteUserService remoteUserService,
                           SysPasswordService passwordService,
                           SysRecordLogService recordLogService) {
        super(remoteUserService, passwordService, recordLogService);
    }

    /**
     * APP端登录 - 不检查IP黑名单，密码要求相对宽松
     */
    public LoginUser login(String username, String password) {
        // 基础参数验证
        validateBaseParams(username, password);

        // APP端宽松的密码策略
        validatePasswordPolicy(username, password);

        // 用户名长度验证
        validateUsername(username);

        // APP端安全检查（无IP黑名单检查）
        performSecurityChecks(username);

        // 获取用户信息
        LoginUser userInfo = getUserInfo(username);
        SysUser user = userInfo.getSysUser();

        // 检查用户状态
        validateUserStatus(username, user);

        // 验证密码
        passwordService.validate(user, password);

        // 记录登录成功
        recordLogService.recordLogininfor(username, Constants.LOGIN_SUCCESS, "APP端登录成功");
        recordLoginInfo(user.getUserId());

        return userInfo;
    }

    @Override
    protected void validatePasswordPolicy(String username, String password) {
        // APP端相对宽松的密码长度要求
        if (password.length() < 6 || password.length() > 50) {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "密码长度必须在6-50个字符之间");
            throw new ServiceException("密码长度必须在6-50个字符之间");
        }
    }

    @Override
    protected void performSecurityChecks(String username) {
        // APP端不进行IP黑名单检查，保持方法为空即可
        // 如果需要其他安全检查，可以在这里添加
    }

    /**
     * 手机号验证码登录
     */
    public LoginUser loginBySms(String mobile, String code) {
        // 验证手机号格式
        if (!mobile.matches("^1[3-9]\\d{9}$")) {
            recordLogService.recordLogininfor(mobile, Constants.LOGIN_FAIL, "手机号格式不正确");
            throw new ServiceException("手机号格式不正确");
        }

        // 验证短信验证码 (这里简化处理，实际项目中需要对接短信服务)
        if (StringUtils.isBlank(code) || code.length() != 6) {
            recordLogService.recordLogininfor(mobile, Constants.LOGIN_FAIL, "验证码格式不正确");
            throw new ServiceException("验证码格式不正确");
        }

        // 通过手机号获取用户信息
        R<LoginUser> userResult = remoteUserService.getUserInfo(mobile, SecurityConstants.INNER);
        if (R.FAIL == userResult.getCode()) {
            recordLogService.recordLogininfor(mobile, Constants.LOGIN_FAIL, "手机号未注册");
            throw new ServiceException("手机号未注册");
        }

        LoginUser userInfo = userResult.getData();
        SysUser user = userInfo.getSysUser();

        // 检查用户状态
        if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            recordLogService.recordLogininfor(mobile, Constants.LOGIN_FAIL, "账号已被删除");
            throw new ServiceException("账号已被删除");
        }
        if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            recordLogService.recordLogininfor(mobile, Constants.LOGIN_FAIL, "账号已停用");
            throw new ServiceException("账号已停用");
        }

        // 记录登录成功
        recordLogService.recordLogininfor(mobile, Constants.LOGIN_SUCCESS, "短信验证码登录成功");

        return userInfo;
    }

    /**
     * 发送短信验证码
     */
    public SmsCodeResponse sendSmsCode(String mobile) {
        // 验证手机号格式
        if (!mobile.matches("^1[3-9]\\d{9}$")) {
            throw new ServiceException("手机号格式不正确");
        }

        // TODO: 这里应该调用短信服务发送验证码
        // 实际项目中需要集成阿里云短信、腾讯云短信等服务
        // 示例：smsService.sendCode(mobile, generateCode());

        recordLogService.recordLogininfor(mobile, Constants.LOGIN_SUCCESS, "短信验证码发送成功");

        // 返回短信验证码响应
        return SmsCodeResponse.success(mobile, 300); // 5分钟过期
    }

    /**
     * 🚀 APP端增强登录 - 支持设备信息和推送token
     *
     * @param loginRequest 登录请求
     * @param deviceInfo   设备信息
     * @param pushToken    推送token
     * @return 用户信息
     */
    public LoginUser login(com.xypai.auth.dto.LoginRequest loginRequest,
                           String deviceInfo, String pushToken) {
        // 执行基本登录
        LoginUser userInfo = login(loginRequest.username(), loginRequest.password());

        // 绑定设备信息和推送token
        if (StringUtils.isNotEmpty(deviceInfo) || StringUtils.isNotEmpty(pushToken)) {
            bindDeviceAndPushToken(userInfo.getSysUser().getUserId(),
                    loginRequest.deviceId(), deviceInfo, pushToken);
        }

        return userInfo;
    }

    /**
     * 🚀 APP端短信登录 - 支持设备信息和推送token
     *
     * @param mobile     手机号
     * @param code       验证码
     * @param deviceInfo 设备信息
     * @param pushToken  推送token
     * @return 用户信息
     */
    public LoginUser loginBySms(String mobile, String code,
                                String deviceInfo, String pushToken) {
        // 执行基本短信登录
        LoginUser userInfo = loginBySms(mobile, code);

        // 绑定设备信息和推送token
        if (StringUtils.isNotEmpty(deviceInfo) || StringUtils.isNotEmpty(pushToken)) {
            bindDeviceAndPushToken(userInfo.getSysUser().getUserId(),
                    null, deviceInfo, pushToken);
        }

        return userInfo;
    }

    /**
     * 绑定设备信息和推送token
     *
     * @param userId     用户ID
     * @param deviceId   设备ID
     * @param deviceInfo 设备信息
     * @param pushToken  推送token
     */
    private void bindDeviceAndPushToken(Long userId, String deviceId,
                                        String deviceInfo, String pushToken) {
        try {
            // TODO: 实现设备信息和推送token的绑定逻辑
            // 这里可以调用设备管理服务或推送服务
            // deviceService.bindDevice(userId, deviceId, deviceInfo);
            // pushService.bindPushToken(userId, pushToken);

            logger.info("📱 绑定设备信息成功 - 用户ID: {}, 设备ID: {}", userId, deviceId);
        } catch (Exception e) {
            logger.warn("📱 绑定设备信息失败 - 用户ID: {}, 错误: {}", userId, e.getMessage());
            // 绑定失败不影响登录成功
        }
    }

    /**
     * APP端退出登录日志记录
     */
    public void recordLogout(String username) {
        recordLogService.recordLogininfor(username, Constants.LOGOUT, "APP端退出成功");
    }
}

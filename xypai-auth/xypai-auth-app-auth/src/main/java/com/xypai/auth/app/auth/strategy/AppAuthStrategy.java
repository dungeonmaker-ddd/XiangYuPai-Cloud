package com.xypai.auth.app.auth.strategy;

import com.xypai.auth.app.auth.service.AppLoginService;
import com.xypai.auth.common.dto.LoginRequest;
import com.xypai.auth.common.dto.SmsLoginRequest;
import com.xypai.auth.common.strategy.AuthenticationStrategy;
import com.xypai.auth.common.vo.SmsCodeResponse;
import com.xypai.system.api.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 📱 APP端认证策略
 * <p>
 * 特点：
 * - 宽松的验证策略
 * - 设备指纹识别
 * - 移动端优化
 * - 快速响应
 *
 * @author xypai
 * @version 3.0.0
 * @since 2024-01-15
 */
@Component("appAuthStrategy")
public class AppAuthStrategy implements AuthenticationStrategy {

    private static final Logger logger = LoggerFactory.getLogger(AppAuthStrategy.class);

    private final AppLoginService appLoginService;

    public AppAuthStrategy(AppLoginService appLoginService) {
        this.appLoginService = appLoginService;
    }

    @Override
    public LoginUser authenticate(LoginRequest request) {
        logger.info("📱 执行APP端认证策略 - 用户: {}, 设备: {}",
                request.username(), request.deviceId());

        // APP端宽松认证
        LoginUser user = appLoginService.login(request.username(), request.password());

        logger.info("✅ APP端认证成功 - 用户: {}", request.username());
        return user;
    }

    @Override
    public LoginUser authenticateBySms(SmsLoginRequest request) {
        logger.info("📱 执行APP端短信认证策略 - 手机号: {}, 设备: {}",
                request.mobile(), request.deviceId());

        // APP端短信登录
        LoginUser user = appLoginService.loginBySms(request.mobile(), request.code());

        logger.info("✅ APP端短信认证成功 - 手机号: {}", request.mobile());
        return user;
    }

    @Override
    public SmsCodeResponse sendSmsCode(String mobile) {
        logger.info("📱 APP端发送短信验证码 - 手机号: {}", mobile);

        SmsCodeResponse response = appLoginService.sendSmsCode(mobile);

        logger.info("📤 APP端短信验证码发送成功 - 手机号: {}", mobile);
        return response;
    }

    @Override
    public String getStrategyName() {
        return "AppAuthStrategy(APP端宽松认证)";
    }
}

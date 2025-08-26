package com.xypai.auth.common.strategy;

import com.xypai.auth.common.dto.LoginRequest;
import com.xypai.auth.common.dto.SmsLoginRequest;
import com.xypai.auth.common.vo.SmsCodeResponse;
import com.xypai.system.api.model.LoginUser;

/**
 * 🎯 认证策略接口
 * <p>
 * 策略模式实现，彻底消除switch-case的代码异味
 * 每种客户端类型都有独立的认证策略实现
 *
 * @author xypai
 * @version 4.0.0 (扩展版本)
 * @since 2024-01-15
 */
public interface AuthenticationStrategy {

    /**
     * 执行认证策略
     *
     * @param request 登录请求
     * @return 认证用户信息
     * @throws SecurityException        认证失败
     * @throws IllegalArgumentException 参数无效
     */
    LoginUser authenticate(LoginRequest request);

    /**
     * 短信验证码认证（可选实现）
     *
     * @param request 短信登录请求
     * @return 认证用户信息
     * @throws UnsupportedOperationException 如果策略不支持短信登录
     */
    default LoginUser authenticateBySms(SmsLoginRequest request) {
        throw new UnsupportedOperationException(
                getStrategyName() + " 不支持短信验证码登录");
    }

    /**
     * 发送短信验证码（可选实现）
     *
     * @param mobile 手机号
     * @return 发送结果
     * @throws UnsupportedOperationException 如果策略不支持发送短信
     */
    default SmsCodeResponse sendSmsCode(String mobile) {
        throw new UnsupportedOperationException(
                getStrategyName() + " 不支持发送短信验证码");
    }

    /**
     * 策略描述（用于日志和调试）
     */
    default String getStrategyName() {
        return this.getClass().getSimpleName();
    }
}

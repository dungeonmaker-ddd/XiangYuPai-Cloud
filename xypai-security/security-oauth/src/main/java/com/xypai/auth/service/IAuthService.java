package com.xypai.auth.service;

import com.xypai.auth.domain.dto.LoginDTO;
import com.xypai.auth.domain.dto.SmsLoginDTO;
import com.xypai.auth.domain.dto.SmsCodeDTO;
import com.xypai.auth.domain.vo.LoginResultVO;

import java.util.Map;

/**
 * 认证服务接口
 *
 * @author xypai
 * @date 2025-01-01
 */
public interface IAuthService {

    /**
     * 密码登录
     */
    LoginResultVO loginWithPassword(LoginDTO loginDTO);

    /**
     * 短信登录
     */
    LoginResultVO loginWithSms(SmsLoginDTO smsLoginDTO);

    /**
     * 刷新令牌
     */
    LoginResultVO refreshToken(String refreshToken);

    /**
     * 用户登出
     */
    boolean logout(String accessToken);

    /**
     * 验证令牌
     */
    Map<String, Object> verifyToken(String accessToken);

    /**
     * 发送短信验证码
     */
    boolean sendSmsCode(SmsCodeDTO smsCodeDTO);

    /**
     * 验证短信验证码
     */
    boolean verifySmsCode(String mobile, String code);
}

package com.xypai.auth.app.client;

import com.xypai.auth.common.dto.LoginRequest;
import com.xypai.auth.common.dto.SmsCodeRequest;
import com.xypai.auth.common.dto.SmsLoginRequest;
import com.xypai.auth.common.vo.LoginResponse;
import com.xypai.auth.common.vo.SmsCodeResponse;
import com.xypai.common.core.domain.R;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 🔐 认证服务Feign客户端
 * <p>
 * APP业务服务调用认证服务的接口定义
 * 遵循微服务间通信的最佳实践
 *
 * @author xypai
 * @version 4.0.0
 * @since 2024-01-15
 */
@FeignClient(
        name = "xypai-auth-app-auth",
        contextId = "authServiceClient",
        path = "/auth",
        fallback = AuthServiceClientFallback.class
)
public interface AuthServiceClient {

    /**
     * 🔑 统一登录接口
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    ResponseEntity<R<LoginResponse>> login(@Valid @RequestBody LoginRequest request);

    /**
     * 📱 短信验证码登录
     *
     * @param request 短信登录请求
     * @return 登录响应
     */
    @PostMapping("/login/sms")
    ResponseEntity<R<LoginResponse>> smsLogin(@Valid @RequestBody SmsLoginRequest request);

    /**
     * 📱 发送短信验证码
     *
     * @param request 短信验证码请求
     * @return 发送结果
     */
    @PostMapping("/sms/send")
    ResponseEntity<R<SmsCodeResponse>> sendSmsCode(@Valid @RequestBody SmsCodeRequest request);

    /**
     * 🚪 退出登录
     *
     * @param authorization Authorization头信息
     * @return 退出结果
     */
    @DeleteMapping("/logout")
    ResponseEntity<R<Void>> logout(@RequestHeader("Authorization") String authorization);

    /**
     * 🔄 刷新Token
     *
     * @param authorization Authorization头信息
     * @return 新的Token信息
     */
    @PostMapping("/refresh")
    ResponseEntity<R<LoginResponse>> refresh(@RequestHeader("Authorization") String authorization);
}

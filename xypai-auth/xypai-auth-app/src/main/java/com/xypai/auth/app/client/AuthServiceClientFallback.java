package com.xypai.auth.app.client;

import com.xypai.auth.common.dto.LoginRequest;
import com.xypai.auth.common.dto.SmsCodeRequest;
import com.xypai.auth.common.dto.SmsLoginRequest;
import com.xypai.auth.common.vo.LoginResponse;
import com.xypai.auth.common.vo.SmsCodeResponse;
import com.xypai.common.core.domain.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * 🛡️ 认证服务Feign客户端降级处理
 * <p>
 * 当认证服务不可用时的降级策略
 * 遵循微服务容错的最佳实践
 *
 * @author xypai
 * @version 4.0.0
 * @since 2024-01-15
 */
@Component
public class AuthServiceClientFallback implements AuthServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceClientFallback.class);

    private static final String FALLBACK_MESSAGE = "认证服务暂时不可用，请稍后重试";

    @Override
    public ResponseEntity<R<LoginResponse>> login(LoginRequest request) {
        logger.warn("🚨 认证服务降级 - 登录接口不可用，用户: {}", request.username());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(R.fail(FALLBACK_MESSAGE));
    }

    @Override
    public ResponseEntity<R<LoginResponse>> smsLogin(SmsLoginRequest request) {
        logger.warn("🚨 认证服务降级 - 短信登录接口不可用，手机号: {}", request.mobile());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(R.fail(FALLBACK_MESSAGE));
    }

    @Override
    public ResponseEntity<R<SmsCodeResponse>> sendSmsCode(SmsCodeRequest request) {
        logger.warn("🚨 认证服务降级 - 短信发送接口不可用，手机号: {}", request.mobile());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(R.fail("短信服务暂时不可用，请稍后重试"));
    }

    @Override
    public ResponseEntity<R<Void>> logout(String authorization) {
        logger.warn("🚨 认证服务降级 - 退出登录接口不可用");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(R.fail(FALLBACK_MESSAGE));
    }

    @Override
    public ResponseEntity<R<LoginResponse>> refresh(String authorization) {
        logger.warn("🚨 认证服务降级 - Token刷新接口不可用");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(R.fail("Token刷新服务暂时不可用，请重新登录"));
    }
}

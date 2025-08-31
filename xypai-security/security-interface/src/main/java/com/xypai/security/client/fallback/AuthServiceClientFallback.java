package com.xypai.security.client.fallback;

import com.xypai.common.core.domain.R;
import com.xypai.security.client.AuthServiceClient;
import com.xypai.security.dto.request.AuthRequest;
import com.xypai.security.dto.response.AuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 🛡️ 认证服务客户端降级处理 (重构版)
 * <p>
 * XV01:08 重构后的认证服务调用失败时的降级处理
 * 确保系统在认证服务不可用时仍能稳定运行
 *
 * @author xypai
 * @since 2.0.0
 */
@Slf4j
@Component
public class AuthServiceClientFallback implements FallbackFactory<AuthServiceClient> {

    @Override
    public AuthServiceClient create(Throwable cause) {
        return new AuthServiceClient() {

            @Override
            public R<AuthResponse> login(AuthRequest authRequest) {
                log.error("认证服务登录调用失败: {}", cause.getMessage(), cause);
                return R.fail("认证服务暂时不可用，请稍后重试");
            }

            @Override
            public R<AuthResponse> refreshToken(String refreshToken) {
                log.error("认证服务刷新令牌调用失败: {}", cause.getMessage(), cause);
                return R.fail("令牌刷新服务暂时不可用，请重新登录");
            }

            @Override
            public R<Void> logout(String accessToken) {
                log.error("认证服务登出调用失败: {}", cause.getMessage(), cause);
                return R.fail("登出服务暂时不可用");
            }

            @Override
            public R<Map<String, Object>> verifyToken(String accessToken) {
                log.error("认证服务令牌验证调用失败: {}", cause.getMessage(), cause);
                return R.fail("令牌验证服务暂时不可用");
            }

            @Override
            public R<AuthResponse.UserInfo> getUserInfo(String accessToken) {
                log.error("认证服务用户信息调用失败: {}", cause.getMessage(), cause);
                return R.fail("用户信息服务暂时不可用");
            }

            @Override
            public R<Map<String, Object>> health() {
                log.error("认证服务健康检查调用失败: {}", cause.getMessage(), cause);
                return R.fail(Map.of(
                        "status", "DOWN",
                        "service", "security-oauth",
                        "error", "Service unavailable",
                        "timestamp", System.currentTimeMillis()
                ));
            }
        };
    }
}

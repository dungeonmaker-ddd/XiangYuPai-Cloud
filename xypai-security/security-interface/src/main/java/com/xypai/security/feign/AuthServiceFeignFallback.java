package com.xypai.security.feign;

import com.xypai.common.core.domain.R;
import com.xypai.security.model.AuthRequest;
import com.xypai.security.model.AuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 🛡️ 认证服务 Feign 降级处理
 * <p>
 * XV01:06 认证服务调用失败时的降级处理
 * 确保系统在认证服务不可用时仍能稳定运行
 *
 * @author xypai
 * @since 1.0.0
 */
@Slf4j
@Component
public class AuthServiceFeignFallback implements FallbackFactory<AuthServiceFeign> {
    
    @Override
    public AuthServiceFeign create(Throwable cause) {
        return new AuthServiceFeign() {
            
            @Override
            public R<AuthResponse> login(AuthRequest authRequest) {
                log.error("认证服务登录调用失败: {}", cause.getMessage(), cause);
                return R.fail("认证服务暂时不可用，请稍后重试");
            }
            
            @Override
            public R<AuthResponse> refreshToken(String refreshToken, String clientType) {
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
        };
    }
}

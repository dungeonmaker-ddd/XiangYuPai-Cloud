package com.xypai.security.client;

import com.xypai.common.core.domain.R;
import com.xypai.security.client.fallback.AuthServiceClientFallback;
import com.xypai.security.dto.request.AuthRequest;
import com.xypai.security.dto.response.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 🔗 认证服务客户端 (重构版)
 * <p>
 * XV01:07 重构后的认证服务远程调用接口
 * 提供给其他微服务调用的认证功能
 *
 * @author xypai
 * @since 2.0.0
 */
@FeignClient(
        name = "security-oauth",
        path = "/auth",
        fallbackFactory = AuthServiceClientFallback.class
)
public interface AuthServiceClient {

    /**
     * 🔐 用户认证登录
     *
     * @param authRequest 认证请求
     * @return 认证响应
     */
    @PostMapping("/login")
    R<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest);

    /**
     * 🔄 刷新访问令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的认证响应
     */
    @PostMapping("/refresh")
    R<AuthResponse> refreshToken(@RequestParam("refresh_token") String refreshToken);

    /**
     * 🚪 用户登出
     *
     * @param accessToken 访问令牌
     * @return 登出结果
     */
    @PostMapping("/logout")
    R<Void> logout(@RequestParam("access_token") String accessToken);

    /**
     * ✅ 验证访问令牌
     *
     * @param accessToken 访问令牌
     * @return 令牌信息
     */
    @GetMapping("/verify")
    R<Map<String, Object>> verifyToken(@RequestParam("access_token") String accessToken);

    /**
     * 👤 获取用户信息
     *
     * @param accessToken 访问令牌
     * @return 用户信息
     */
    @GetMapping("/user-info")
    R<AuthResponse.UserInfo> getUserInfo(@RequestParam("access_token") String accessToken);

    /**
     * 💚 健康检查
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    R<Map<String, Object>> health();
}

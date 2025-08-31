package com.xypai.security.oauth.auth.feign;

import com.xypai.common.core.domain.R;
import com.xypai.security.oauth.auth.dto.request.AuthRequest;
import com.xypai.security.oauth.auth.dto.response.AuthResponse;
import com.xypai.security.oauth.auth.feign.fallback.AuthServiceFeignFallback;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 🔗 认证服务 Feign 客户端 (现代化实现)
 * <p>
 * XV03:04 AUTH层 - 现代化Feign客户端
 * 支持异步调用、断路器、重试等现代特性
 *
 * @author xypai
 * @since 3.0.0
 */
@FeignClient(
        name = "security-oauth-internal",
        path = "/auth",
        fallbackFactory = AuthServiceFeignFallback.class,
        configuration = FeignConfig.class
)
public interface AuthServiceFeign {

    /**
     * 🔐 用户认证登录 (同步)
     */
    @PostMapping("/login")
    R<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest);

    /**
     * 🔐 用户认证登录 (异步) - 现代化特性
     */
    @PostMapping("/login")
    CompletableFuture<R<AuthResponse>> loginAsync(@Valid @RequestBody AuthRequest authRequest);

    /**
     * 🔄 刷新访问令牌
     */
    @PostMapping("/refresh")
    R<AuthResponse> refreshToken(@RequestParam("refresh_token") String refreshToken);

    /**
     * 🔄 刷新访问令牌 (异步)
     */
    @PostMapping("/refresh")
    CompletableFuture<R<AuthResponse>> refreshTokenAsync(@RequestParam("refresh_token") String refreshToken);

    /**
     * 🚪 用户登出
     */
    @PostMapping("/logout")
    R<Void> logout(@RequestParam("access_token") String accessToken);

    /**
     * ✅ 验证访问令牌
     */
    @GetMapping("/verify")
    R<Map<String, Object>> verifyToken(@RequestParam("access_token") String accessToken);

    /**
     * ✅ 验证访问令牌 (异步)
     */
    @GetMapping("/verify")
    CompletableFuture<R<Map<String, Object>>> verifyTokenAsync(@RequestParam("access_token") String accessToken);

    /**
     * 👤 获取用户信息
     */
    @GetMapping("/user-info")
    R<AuthResponse.UserInfo> getUserInfo(@RequestParam("access_token") String accessToken);

    /**
     * 💚 健康检查
     */
    @GetMapping("/health")
    R<Map<String, Object>> health();

    /**
     * 📊 批量验证令牌 - 现代化批量操作
     */
    @PostMapping("/batch-verify")
    R<Map<String, Boolean>> batchVerifyTokens(@RequestBody java.util.List<String> accessTokens);

    /**
     * 🔒 批量注销用户令牌
     */
    @PostMapping("/batch-logout")
    R<Map<String, Boolean>> batchLogout(@RequestBody java.util.List<String> accessTokens);
}

/**
 * 🔧 现代化Feign配置
 */
@org.springframework.context.annotation.Configuration
class FeignConfig {

    /**
     * 🔄 重试配置 - 使用现代化的配置方式
     */
    @org.springframework.context.annotation.Bean
    public feign.Retryer retryer() {
        // 最大重试3次，初始间隔100ms，最大间隔1s
        return new feign.Retryer.Default(100, java.time.Duration.ofSeconds(1).toMillis(), 3);
    }

    /**
     * ⏱️ 超时配置
     */
    @org.springframework.context.annotation.Bean
    public feign.Request.Options requestOptions() {
        return new feign.Request.Options(
                java.time.Duration.ofSeconds(5),    // 连接超时
                java.time.Duration.ofSeconds(10),   // 读取超时
                true                                // 跟随重定向
        );
    }

    /**
     * 📊 请求拦截器 - 添加现代化的链路追踪
     */
    @org.springframework.context.annotation.Bean("oauthRequestInterceptor")
    public feign.RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // 添加链路追踪ID
            var traceId = java.util.UUID.randomUUID().toString();
            requestTemplate.header("X-Trace-Id", traceId);

            // 添加时间戳
            requestTemplate.header("X-Request-Time", java.time.Instant.now().toString());

            // 添加客户端标识
            requestTemplate.header("X-Client", "xypai-security-feign");
        };
    }

    /**
     * 📈 日志配置 - 现代化日志级别
     */
    @org.springframework.context.annotation.Bean
    public feign.Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.BASIC;
    }

    /**
     * 🛡️ 错误解码器 - 现代化错误处理
     */
    @org.springframework.context.annotation.Bean
    public feign.codec.ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            var status = response.status();
            var reason = response.reason();

            return switch (status) {
                case 401 -> new SecurityException("认证失败: %s".formatted(reason));
                case 403 -> new SecurityException("权限不足: %s".formatted(reason));
                case 404 -> new IllegalArgumentException("资源不存在: %s".formatted(reason));
                case 429 -> new RuntimeException("请求过于频繁: %s".formatted(reason));
                case 500 -> new RuntimeException("服务内部错误: %s".formatted(reason));
                default -> new RuntimeException("未知错误[%d]: %s".formatted(status, reason));
            };
        };
    }
}

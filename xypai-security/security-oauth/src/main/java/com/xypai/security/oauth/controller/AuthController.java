package com.xypai.security.oauth.controller;

import com.xypai.common.core.domain.R;
import com.xypai.security.model.AuthRequest;
import com.xypai.security.model.AuthResponse;
import com.xypai.security.oauth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * 🔐 认证控制器
 * <p>
 * XV02:06 轻量级认证控制层
 * 专注于请求响应处理，业务逻辑委托给Service层
 *
 * @author xypai
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "🔐 认证中心", description = "OAuth2认证服务APIs")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 🔐 用户登录认证
     */
    @Operation(summary = "用户登录", description = "支持密码、短信、微信等多种认证方式")
    @PostMapping("/login")
    public R<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        log.info("收到登录请求: username={}, clientType={}", 
                authRequest.username(), authRequest.clientType());
        
        return authService.authenticate(authRequest)
                .map(R::ok)
                .orElse(R.fail("认证失败，请检查用户名和密码"));
    }
    
    /**
     * 🔄 刷新访问令牌
     */
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh")
    public R<AuthResponse> refreshToken(
            @RequestParam("refresh_token") String refreshToken,
            @RequestParam("client_type") String clientType) {
        
        log.info("收到刷新令牌请求: clientType={}", clientType);
        
        return authService.refreshToken(refreshToken, clientType)
                .map(R::ok)
                .orElse(R.fail("刷新令牌失败，请重新登录"));
    }
    
    /**
     * 🚪 用户登出
     */
    @Operation(summary = "用户登出", description = "使访问令牌失效")
    @PostMapping("/logout")
    public R<Void> logout(@RequestParam("access_token") String accessToken) {
        log.info("收到登出请求");
        
        boolean success = authService.logout(accessToken);
        return success ? R.ok() : R.fail("登出失败");
    }
    
    /**
     * ✅ 验证访问令牌
     */
    @Operation(summary = "验证令牌", description = "验证访问令牌的有效性")
    @GetMapping("/verify")
    public R<Map<String, Object>> verifyToken(@RequestParam("access_token") String accessToken) {
        return authService.verifyToken(accessToken)
                .map(R::ok)
                .orElse(R.fail("无效的访问令牌"));
    }
    
    /**
     * 👤 获取用户信息
     */
    @Operation(summary = "获取用户信息", description = "根据访问令牌获取用户详细信息")
    @GetMapping("/user-info")
    public R<AuthResponse.UserInfo> getUserInfo(@RequestParam("access_token") String accessToken) {
        return authService.getUserInfo(accessToken)
                .map(R::ok)
                .orElse(R.fail("获取用户信息失败"));
    }
    
    /**
     * 💚 健康检查
     */
    @Operation(summary = "健康检查", description = "检查认证服务状态")
    @GetMapping("/health")
    public R<Map<String, Object>> health() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "service", "xypai-security-oauth",
            "timestamp", System.currentTimeMillis()
        );
        return R.ok(health);
    }
}

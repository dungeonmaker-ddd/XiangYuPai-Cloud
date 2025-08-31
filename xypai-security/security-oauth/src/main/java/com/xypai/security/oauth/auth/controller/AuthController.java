package com.xypai.security.oauth.auth.controller;

import com.xypai.common.core.domain.R;
import com.xypai.security.oauth.auth.dto.request.AuthRequest;
import com.xypai.security.oauth.auth.dto.response.AuthResponse;
import com.xypai.security.oauth.service.business.AuthBusiness;
import com.xypai.security.oauth.service.business.TokenBusiness;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 🔐 认证控制器 (简洁三层架构)
 * <p>
 * XV03:01 AUTH层 - 认证业务控制器
 * 专注于HTTP请求响应处理，业务逻辑委托给Service层
 *
 * @author xypai
 * @since 3.0.0
 */
@Slf4j
@Tag(name = "🔐 认证中心", description = "OAuth2认证服务APIs")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthBusiness authBusiness;
    private final TokenBusiness tokenBusiness;

    /**
     * 🔐 用户登录认证
     */
    @Operation(
            summary = "用户登录",
            description = "支持密码、短信、微信等多种认证方式"
    )
    @PostMapping("/login")
    public R<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        log.info("收到登录请求: username={}, authType={}",
                authRequest.username(), authRequest.authType());

        return authBusiness.authenticate(authRequest)
                .map(R::ok)
                .orElse(R.fail("认证失败，请检查认证信息"));
    }

    /**
     * 🔄 刷新访问令牌
     */
    @Operation(
            summary = "刷新令牌",
            description = "使用刷新令牌获取新的访问令牌"
    )
    @PostMapping("/refresh")
    public R<AuthResponse> refreshToken(
            @Parameter(description = "刷新令牌", required = true)
            @RequestParam("refresh_token") String refreshToken) {

        log.info("收到刷新令牌请求");

        return tokenBusiness.refreshTokens(refreshToken)
                .map(R::ok)
                .orElse(R.fail("刷新令牌失败，请重新登录"));
    }

    /**
     * 🚪 用户登出
     */
    @Operation(
            summary = "用户登出",
            description = "使访问令牌失效"
    )
    @PostMapping("/logout")
    public R<Void> logout(
            @Parameter(description = "访问令牌", required = true)
            @RequestParam("access_token") String accessToken) {
        log.info("收到登出请求");

        boolean success = tokenBusiness.invalidateToken(accessToken);
        return success ? R.ok() : R.fail("登出失败");
    }

    /**
     * ✅ 验证访问令牌
     */
    @Operation(
            summary = "验证令牌",
            description = "验证访问令牌的有效性"
    )
    @GetMapping("/verify")
    public R<Map<String, Object>> verifyToken(
            @Parameter(description = "访问令牌", required = true)
            @RequestParam("access_token") String accessToken) {
        return tokenBusiness.verifyToken(accessToken)
                .map(R::ok)
                .orElse(R.fail("无效的访问令牌"));
    }

    /**
     * 👤 获取用户信息
     */
    @Operation(
            summary = "获取用户信息",
            description = "根据访问令牌获取用户详细信息"
    )
    @GetMapping("/user-info")
    public R<AuthResponse.UserInfo> getUserInfo(
            @Parameter(description = "访问令牌", required = true)
            @RequestParam("access_token") String accessToken) {
        return tokenBusiness.getUserInfoFromToken(accessToken)
                .map(R::ok)
                .orElse(R.fail("获取用户信息失败"));
    }

    /**
     * 📱 发送短信验证码
     */
    @Operation(
            summary = "发送短信验证码",
            description = "向指定手机号发送登录验证码"
    )
    @PostMapping("/sms/send")
    public R<Map<String, Object>> sendSmsCode(
            @Parameter(description = "手机号", required = true, example = "13800138000")
            @RequestParam("mobile") String mobile) {

        log.info("收到发送短信验证码请求: mobile={}", mobile);

        return authBusiness.sendSmsCode(mobile, "web")
                .map(result -> {
                    Map<String, Object> response = Map.of(
                            "message", "验证码发送成功",
                            "mobile", mobile,
                            "expires_in", result.expiresIn(),
                            "timestamp", System.currentTimeMillis()
                    );
                    return R.ok(response);
                })
                .orElse(R.fail("验证码发送失败，请稍后重试"));
    }

    /**
     * 💚 健康检查
     */
    @Operation(
            summary = "健康检查",
            description = "检查认证服务状态"
    )
    @GetMapping("/health")
    public R<Map<String, Object>> health() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "service", "xypai-security-oauth",
                "version", "3.0.0",
                "architecture", "三层架构(auth-common-service)",
                "timestamp", System.currentTimeMillis()
        );
        return R.ok(health);
    }
}

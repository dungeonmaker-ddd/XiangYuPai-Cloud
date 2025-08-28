package com.xypai.auth.app.auth.controller;

import com.xypai.common.core.domain.R;
import com.xypai.security.feign.AuthServiceFeign;
import com.xypai.security.model.AuthRequest;
import com.xypai.security.model.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 📱 APP端认证控制器 (重构版)
 * <p>
 * XV03:01 轻量化APP端认证控制器
 * 专注于请求响应处理，业务逻辑委托给新的安全认证服务
 *
 * @author xypai
 * @version 5.0.0
 */
@Slf4j
@Tag(name = "📱 APP端认证服务", description = "移动端专用认证接口")
@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceFeign authServiceFeign;

    /**
     * 📱 APP端用户名密码登录
     */
    @Operation(summary = "APP端登录", description = "支持用户名密码登录")
    @ApiResponse(responseCode = "200", description = "登录成功")
    @PostMapping("/login/password")
    public R<AuthResponse> passwordLogin(@Valid @RequestBody PasswordLoginRequest request) {
        log.info("APP端密码登录请求: username={}", request.username());

        AuthRequest authRequest = AuthRequest.ofPassword(
                request.username(),
                request.password(),
                "app"
        );

        return authServiceFeign.login(authRequest);
    }

    /**
     * 📱 短信验证码登录
     */
    @Operation(summary = "短信验证码登录", description = "通过手机号和短信验证码登录")
    @PostMapping("/login/sms")
    public R<AuthResponse> smsLogin(@Valid @RequestBody SmsLoginRequest request) {
        log.info("APP端短信登录请求: mobile={}", request.mobile());

        AuthRequest authRequest = AuthRequest.ofSms(
                request.mobile(),
                request.smsCode(),
                "app"
        );

        return authServiceFeign.login(authRequest);
    }

    /**
     * 🚪 APP端退出登录
     */
    @Operation(summary = "用户登出", description = "注销用户会话")
    @PostMapping("/logout")
    public R<Void> logout(@RequestParam("access_token") String accessToken) {
        log.info("APP端登出请求");
        return authServiceFeign.logout(accessToken);
    }

    /**
     * 🔄 刷新访问令牌
     */
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh")
    public R<AuthResponse> refreshToken(@RequestParam("refresh_token") String refreshToken) {
        log.info("APP端刷新令牌请求");
        return authServiceFeign.refreshToken(refreshToken, "app");
    }

    /**
     * 👤 获取用户信息
     */
    @Operation(summary = "获取用户信息", description = "根据访问令牌获取用户信息")
    @GetMapping("/user-info")
    public R<AuthResponse.UserInfo> getUserInfo(@RequestParam("access_token") String accessToken) {
        return authServiceFeign.getUserInfo(accessToken);
    }

    /**
     * ✅ 验证令牌
     */
    @Operation(summary = "验证令牌", description = "验证访问令牌的有效性")
    @GetMapping("/verify")
    public R<java.util.Map<String, Object>> verifyToken(@RequestParam("access_token") String accessToken) {
        return authServiceFeign.verifyToken(accessToken);
    }

    /**
     * 📱 密码登录请求 Record
     */
    public record PasswordLoginRequest(
            @NotBlank(message = "用户名不能为空")
            String username,

            @NotBlank(message = "密码不能为空")
            String password
    ) {
    }

    /**
     * 📱 短信登录请求 Record
     */
    public record SmsLoginRequest(
            @NotBlank(message = "手机号不能为空")
            String mobile,

            @NotBlank(message = "验证码不能为空")
            String smsCode
    ) {
    }
}

package com.xypai.auth.controller;

import com.xypai.auth.dto.LoginRequest;
import com.xypai.auth.dto.SmsLoginRequest;
import com.xypai.auth.service.AuthService;
import com.xypai.auth.vo.LoginResponse;
import com.xypai.auth.vo.SmsCodeResponse;
import com.xypai.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 🎯 统一认证控制器
 * <p>
 * 专注于HTTP请求路由，业务逻辑委托给AuthService
 *
 * @author xypai
 * @version 4.1.0 (简化重构版本)
 */
@Tag(name = "🎯 统一认证服务", description = "基于 clientType 参数的智能认证路由")
@RestController
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 🎯 统一登录接口
     */
    @Operation(summary = "🎯 统一登录", description = "智能识别客户端类型，自动选择最优认证策略")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "🎉 登录成功"),
            @ApiResponse(responseCode = "400", description = "❌ 参数无效"),
            @ApiResponse(responseCode = "401", description = "🚫 认证失败")
    })
    @PostMapping("/login")
    public ResponseEntity<R<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return authService.processLogin(request);
    }

    /**
     * 📱 短信验证码登录
     */
    @Operation(summary = "📱 短信验证码登录", description = "通过手机号和短信验证码登录")
    @PostMapping("/login/sms")
    public ResponseEntity<R<LoginResponse>> smsLogin(@Valid @RequestBody SmsLoginRequest request) {
        return authService.processSmsLogin(request);
    }

    /**
     * 📱 发送短信验证码
     */
    @Operation(summary = "📱 发送短信验证码", description = "向指定手机号发送登录验证码")
    @PostMapping("/sms/send")
    public ResponseEntity<R<SmsCodeResponse>> sendSmsCode(
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") @RequestParam String mobile,
            @RequestParam(defaultValue = "app") String clientType) {
        return authService.sendSmsCode(mobile, clientType);
    }

    /**
     * 🚪 统一退出登录
     */
    @Operation(summary = "🚪 统一退出登录", description = "注销用户会话，清除token缓存")
    @DeleteMapping("/logout")
    public ResponseEntity<R<Void>> logout(HttpServletRequest request) {
        return authService.processLogout(request);
    }

    /**
     * 🔄 统一刷新令牌
     */
    @Operation(summary = "🔄 刷新访问令牌", description = "延长当前token的有效期")
    @PostMapping("/refresh")
    public ResponseEntity<R<LoginResponse>> refresh(HttpServletRequest request) {
        return authService.refreshToken(request);
    }

    /**
     * 📋 获取当前用户信息
     */
    @Operation(summary = "📋 获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/info")
    public ResponseEntity<R<Object>> getCurrentUserInfo(HttpServletRequest request) {
        return authService.getCurrentUserInfo(request);
    }

    /**
     * ✅ 检查token有效性
     */
    @Operation(summary = "✅ 检查token有效性", description = "验证当前token是否有效")
    @GetMapping("/validate")
    public ResponseEntity<R<Object>> validateToken(HttpServletRequest request) {
        return authService.validateToken(request);
    }
}
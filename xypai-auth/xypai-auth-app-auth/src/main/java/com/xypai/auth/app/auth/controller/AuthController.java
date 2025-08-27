package com.xypai.auth.app.auth.controller;

import com.xypai.auth.app.auth.service.AuthService;
import com.xypai.auth.common.dto.LoginRequest;
import com.xypai.auth.common.dto.SmsCodeRequest;
import com.xypai.auth.common.dto.SmsLoginRequest;
import com.xypai.auth.common.vo.LoginResponse;
import com.xypai.auth.common.vo.SmsCodeResponse;
import com.xypai.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 📱 APP端认证控制器
 * <p>
 * 专为移动端APP提供认证服务
 *
 * @author xypai
 * @version 4.1.0
 */
@Tag(name = "📱 APP端认证服务", description = "移动端专用认证接口")
@RestController
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 📱 APP端用户名密码登录
     */
    @Operation(summary = "📱 APP端用户名密码登录", description = "移动端用户名密码登录")
    @ApiResponse(responseCode = "200", description = "登录成功")
    @ApiResponse(responseCode = "400", description = "参数无效")
    @ApiResponse(responseCode = "401", description = "认证失败")
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
    public ResponseEntity<R<SmsCodeResponse>> sendSmsCode(@Valid @RequestBody SmsCodeRequest request) {
        return authService.sendSmsCode(request);
    }

    /**
     * 🚪 APP端退出登录
     */
    @Operation(summary = "🚪 APP端退出登录", description = "注销用户会话，清除token缓存")
    @DeleteMapping("/logout")
    public ResponseEntity<R<Void>> logout(HttpServletRequest request) {
        return authService.processLogout(request);
    }

    /**
     * 🔄 刷新访问令牌
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

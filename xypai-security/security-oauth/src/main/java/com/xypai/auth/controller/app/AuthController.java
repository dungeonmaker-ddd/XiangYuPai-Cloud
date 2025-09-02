package com.xypai.auth.controller.app;

import com.xypai.auth.domain.dto.LoginDTO;
import com.xypai.auth.domain.dto.SmsLoginDTO;
import com.xypai.auth.domain.dto.SmsCodeDTO;
import com.xypai.auth.domain.vo.LoginResultVO;
import com.xypai.auth.service.IAuthService;
import com.xypai.common.core.domain.R;
import com.xypai.common.core.web.controller.BaseController;
import com.xypai.common.log.annotation.Log;
import com.xypai.common.log.enums.BusinessType;
import com.xypai.common.log.enums.OperatorType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 *
 * @author xypai
 * @date 2025-01-01
 */
@Slf4j
@Tag(name = "认证管理", description = "用户登录认证API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController extends BaseController {

    private final IAuthService authService;

    /**
     * 密码登录
     */
    @Operation(summary = "密码登录", description = "使用用户名和密码登录")
    @PostMapping("/login")
    @Log(title = "密码登录", businessType = BusinessType.OTHER, operatorType = OperatorType.MOBILE)
    public R<LoginResultVO> login(@Validated @RequestBody LoginDTO loginDTO) {
        LoginResultVO result = authService.loginWithPassword(loginDTO);
        return R.ok(result);
    }

    /**
     * 短信登录
     * 
     * 
    sequenceDiagram
    participant C as 客户端
    participant A as 认证服务
    participant U as 用户服务
    participant R as Redis

    C->>A: POST /login/sms (mobile, smsCode)
    A->>R: 验证短信验证码
    R-->>A: 验证成功
    A->>U: GET /auth/mobile/{mobile}
    U-->>A: 用户不存在
    A->>U: POST /auth/auto-register
    U->>U: 创建新用户
    U-->>A: 返回用户信息
    A->>A: 生成JWT令牌
    A-->>C: 返回登录成功结果
     */
    @Operation(summary = "短信登录", description = "使用手机号和短信验证码登录")
    @PostMapping("/login/sms")
    @Log(title = "短信登录", businessType = BusinessType.OTHER, operatorType = OperatorType.MOBILE)
    public R<LoginResultVO> loginWithSms(@Validated @RequestBody SmsLoginDTO smsLoginDTO) {
        LoginResultVO result = authService.loginWithSms(smsLoginDTO);
        return R.ok(result);
    }

    /**
     * 刷新令牌
     */
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh")
    @Log(title = "刷新令牌", businessType = BusinessType.OTHER, operatorType = OperatorType.MOBILE)
    public R<LoginResultVO> refreshToken(
            @Parameter(description = "刷新令牌", required = true)
            @RequestParam("refreshToken") String refreshToken) {
        LoginResultVO result = authService.refreshToken(refreshToken);
        return R.ok(result);
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "用户退出登录")
    @PostMapping("/logout")
    @Log(title = "用户登出", businessType = BusinessType.OTHER, operatorType = OperatorType.MOBILE)
    public R<Void> logout(@RequestHeader("Authorization") String authorization) {
        // 提取令牌 (去掉 "Bearer " 前缀)
        String accessToken = authorization.startsWith("Bearer ") ? 
                authorization.substring(7) : authorization;
        
        boolean success = authService.logout(accessToken);
        return success ? R.ok() : R.fail("登出失败");
    }

    /**
     * 验证令牌
     */
    @Operation(summary = "验证令牌", description = "验证访问令牌的有效性")
    @GetMapping("/verify")
    public R<Map<String, Object>> verifyToken(
            @Parameter(description = "访问令牌", required = true)
            @RequestParam("accessToken") String accessToken) {
        Map<String, Object> result = authService.verifyToken(accessToken);
        return result != null ? R.ok(result) : R.fail("无效的访问令牌");
    }

    /**
     * 发送短信验证码
     */
    @Operation(summary = "发送短信验证码", description = "向指定手机号发送验证码")
    @PostMapping("/sms/send")
    @Log(title = "发送短信验证码", businessType = BusinessType.OTHER, operatorType = OperatorType.MOBILE)
    public R<String> sendSmsCode(@Validated @RequestBody SmsCodeDTO smsCodeDTO) {
        boolean success = authService.sendSmsCode(smsCodeDTO);
        return success ? R.ok("验证码发送成功") : R.fail("验证码发送失败");
    }

    /**
     * 验证短信验证码
     */
    @Operation(summary = "验证短信验证码", description = "验证手机号验证码是否正确")
    @PostMapping("/sms/verify")
    public R<Boolean> verifySmsCode(
            @Parameter(description = "手机号", required = true)
            @RequestParam("mobile") String mobile,
            @Parameter(description = "验证码", required = true)
            @RequestParam("code") String code) {
        boolean valid = authService.verifySmsCode(mobile, code);
        return R.ok(valid);
    }

    /**
     * 健康检查
     */
    @Operation(summary = "健康检查", description = "检查认证服务状态")
    @GetMapping("/health")
    public R<Map<String, Object>> health() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "service", "xypai-auth",
                "version", "1.0.0",
                "timestamp", System.currentTimeMillis()
        );
        return R.ok(health);
    }
}

package com.xypai.security.oauth.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * 🔐 认证请求 DTO (简洁三层架构)
 * <p>
 * XV03:02 AUTH层 - 认证请求数据传输对象
 * 用于API接口的请求参数封装
 *
 * @author xypai
 * @since 3.0.0
 */
@Schema(description = "认证请求")
public record AuthRequest(

        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度必须在3-50字符之间")
        @Schema(description = "用户名", example = "admin")
        String username,

        @Schema(description = "密码(密码认证时必填)", example = "123456")
        String password,

        @Schema(description = "手机号(短信认证时必填)", example = "13800138000")
        String mobile,

        @JsonProperty("sms_code")
        @Schema(description = "短信验证码(短信认证时必填)", example = "123456")
        String smsCode,

        @JsonProperty("open_id")
        @Schema(description = "微信OpenId(微信认证时必填)", example = "ox1234567890abcdef")
        String openId,

        @JsonProperty("wechat_code")
        @Schema(description = "微信授权码(微信认证时必填)", example = "wx_auth_code_demo")
        String wechatCode,

        @JsonProperty("auth_type")
        @Schema(hidden = true, description = "认证类型（后端自动判断）")
        String authType
) {

    /**
     * 紧凑构造器 - 数据验证和标准化
     */
    public AuthRequest {
        // 基础验证
        Objects.requireNonNull(username, "用户名不能为空");

        // 数据标准化
        username = username.trim().toLowerCase();

        if (authType != null) {
            authType = authType.trim().toLowerCase();
        }

        // 自动判断认证类型
        if (authType == null || authType.isEmpty()) {
            authType = determineAuthType(password, mobile, smsCode, openId, wechatCode);
        }

        // 业务规则验证
        validateAuthTypeFields(authType, password, mobile, smsCode, openId, wechatCode);
    }

    /**
     * 🤖 自动判断认证类型
     */
    private static String determineAuthType(String password, String mobile, String smsCode,
                                            String openId, String wechatCode) {
        // 1. 优先级：微信 > 短信 > 密码
        if (isNotEmpty(openId) || isNotEmpty(wechatCode)) {
            return "wechat";
        }

        if (isNotEmpty(smsCode)) {
            return "sms";
        }

        if (isNotEmpty(password)) {
            return "password";
        }

        throw new IllegalArgumentException("无法判断认证类型，请提供密码、短信验证码或微信授权码");
    }

    /**
     * 检查字符串是否非空
     */
    private static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 验证认证类型对应的必填字段
     */
    private static void validateAuthTypeFields(String authType, String password,
                                               String mobile, String smsCode,
                                               String openId, String wechatCode) {
        switch (authType) {
            case "password" -> {
                if (password == null || password.trim().isEmpty()) {
                    throw new IllegalArgumentException("密码认证时密码不能为空");
                }
            }
            case "sms" -> {
                if (mobile == null || mobile.trim().isEmpty()) {
                    throw new IllegalArgumentException("短信认证时手机号不能为空");
                }
                if (smsCode == null || smsCode.trim().isEmpty()) {
                    throw new IllegalArgumentException("短信认证时验证码不能为空");
                }
            }
            case "wechat" -> {
                if (openId == null || openId.trim().isEmpty()) {
                    throw new IllegalArgumentException("微信认证时OpenId不能为空");
                }
                if (wechatCode == null || wechatCode.trim().isEmpty()) {
                    throw new IllegalArgumentException("微信认证时授权码不能为空");
                }
            }
        }
    }

    /**
     * 🏭 简化创建方法 - 自动判断认证类型
     */
    public static AuthRequest create(String username, String password, String mobile, String smsCode) {
        return new AuthRequest(username, password, mobile, smsCode, null, null, null);
    }

    /**
     * 🏭 密码认证工厂方法
     */
    public static AuthRequest passwordAuth(String username, String password) {
        return new AuthRequest(username, password, null, null, null, null, null);
    }

    /**
     * 🏭 短信认证工厂方法
     */
    public static AuthRequest smsAuth(String mobile, String smsCode) {
        return new AuthRequest(mobile, null, mobile, smsCode, null, null, null);
    }

    /**
     * 🏭 微信认证工厂方法
     */
    public static AuthRequest wechatAuth(String openId, String wechatCode) {
        return new AuthRequest(openId, null, null, null, openId, wechatCode, null);
    }

    // 便利方法
    public boolean isPasswordAuth() {
        return "password".equals(authType);
    }

    public boolean isSmsAuth() {
        return "sms".equals(authType);
    }

    public boolean isWechatAuth() {
        return "wechat".equals(authType);
    }

    /**
     * 安全的字符串表示（隐藏敏感信息）
     */
    @Override
    public String toString() {
        return String.format("AuthRequest{username='%s', authType='%s'}",
                username, authType);
    }
}

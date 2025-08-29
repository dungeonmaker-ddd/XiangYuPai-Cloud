package com.xypai.security.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * 🔐 认证请求 Record
 * <p>
 * XV01:03 统一认证请求数据结构
 * 支持多种认证方式：用户名密码、手机号、微信等
 *
 * @author xypai
 * @since 1.0.0
 */
public record AuthRequest(
        
        @NotBlank(message = "用户标识不能为空")
        @Size(min = 2, max = 50, message = "用户标识长度必须在2-50之间")
        String username,
        
        @Size(max = 128, message = "密码长度不能超过128")
        String password,
        
        @JsonProperty("client_type")
        String clientType,
        
        @NotBlank(message = "认证类型不能为空")
        @Pattern(regexp = "^(password|sms|wechat)$", message = "认证类型只能是password、sms或wechat")
        @JsonProperty("auth_type")
        String authType,
        
        @JsonProperty("sms_code")
        String smsCode,
        
        @JsonProperty("wechat_code")
        String wechatCode
) {
    
    /**
     * 紧凑构造函数 - 数据验证和标准化
     */
    public AuthRequest {
        Objects.requireNonNull(username, "用户标识不能为null");
        Objects.requireNonNull(authType, "认证类型不能为null");
        
        // 标准化处理
        username = username.trim().toLowerCase();
        if (clientType != null) {
            clientType = clientType.trim().toLowerCase();
        }
        authType = authType.trim().toLowerCase();
        
        // 业务规则验证
        switch (authType) {
            case "password" -> {
                if (password == null || password.trim().isEmpty()) {
                    throw new IllegalArgumentException("密码认证时密码不能为空");
                }
            }
            case "sms" -> {
                if (smsCode == null || smsCode.trim().isEmpty()) {
                    throw new IllegalArgumentException("短信认证时验证码不能为空");
                }
            }
            case "wechat" -> {
                if (wechatCode == null || wechatCode.trim().isEmpty()) {
                    throw new IllegalArgumentException("微信认证时授权码不能为空");
                }
            }
        }
    }
    
    /**
     * 创建密码认证请求
     */
    public static AuthRequest ofPassword(String username, String password) {
        return new AuthRequest(username, password, null, "password", null, null);
    }
    
    /**
     * 创建短信认证请求
     */
    public static AuthRequest ofSms(String mobile, String smsCode) {
        return new AuthRequest(mobile, null, null, "sms", smsCode, null);
    }
    
    /**
     * 创建微信认证请求
     */
    public static AuthRequest ofWechat(String openId, String wechatCode) {
        return new AuthRequest(openId, null, null, "wechat", null, wechatCode);
    }
    
    /**
     * 是否为密码认证
     */
    public boolean isPasswordAuth() {
        return "password".equals(authType);
    }
    
    /**
     * 是否为短信认证
     */
    public boolean isSmsAuth() {
        return "sms".equals(authType);
    }
    
    /**
     * 是否为微信认证
     */
    public boolean isWechatAuth() {
        return "wechat".equals(authType);
    }
}

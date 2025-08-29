package com.xypai.security.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/**
 * 🔐 认证响应 Record
 * <p>
 * XV01:04 统一认证响应数据结构
 * 包含访问令牌、刷新令牌、用户信息等
 *
 * @author xypai
 * @since 1.0.0
 */
public record AuthResponse(
        
        @JsonProperty("access_token")
        String accessToken,
        
        @JsonProperty("refresh_token")
        String refreshToken,
        
        @JsonProperty("token_type")
        String tokenType,
        
        @JsonProperty("expires_in")
        Long expiresIn,
        
        @JsonProperty("user_info")
        UserInfo userInfo
) {
    
    /**
     * 紧凑构造函数 - 数据验证
     */
    public AuthResponse {
        Objects.requireNonNull(accessToken, "访问令牌不能为空");
        Objects.requireNonNull(refreshToken, "刷新令牌不能为空");
        Objects.requireNonNull(userInfo, "用户信息不能为空");
        
        // 默认值处理
        if (tokenType == null || tokenType.trim().isEmpty()) {
            tokenType = "Bearer";
        }
        
        if (expiresIn == null || expiresIn <= 0) {
            throw new IllegalArgumentException("令牌过期时间必须大于0");
        }
    }
    
    /**
     * 创建认证响应
     */
    public static AuthResponse of(String accessToken, String refreshToken, 
                                 Long expiresIn, UserInfo userInfo) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresIn, userInfo);
    }
    
    /**
     * 🧑 用户信息 Record
     */
    public record UserInfo(
            Long id,
            String username,
            String nickname,
            String email,
            String mobile,
            Set<String> roles,
            Set<String> permissions,
            @JsonProperty("login_time")
            Instant loginTime
    ) {
        
        /**
         * 紧凑构造函数 - 用户信息验证
         */
        public UserInfo {
            Objects.requireNonNull(id, "用户ID不能为空");
            Objects.requireNonNull(username, "用户名不能为空");
            
            // 默认值处理
            if (nickname == null || nickname.trim().isEmpty()) {
                nickname = username;
            }
            
            if (roles == null) {
                roles = Set.of();
            }
            
            if (permissions == null) {
                permissions = Set.of();
            }
            
            if (loginTime == null) {
                loginTime = Instant.now();
            }
        }
        
        /**
         * 创建用户信息
         */
        public static UserInfo of(Long id, String username) {
            return new UserInfo(id, username, null, null, null,
                    null, null, null);
        }
        
        /**
         * 是否有指定角色
         */
        public boolean hasRole(String role) {
            return roles.contains(role);
        }
        
        /**
         * 是否有指定权限
         */
        public boolean hasPermission(String permission) {
            return permissions.contains(permission);
        }
    }
}

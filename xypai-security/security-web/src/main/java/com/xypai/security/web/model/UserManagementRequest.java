package com.xypai.security.web.model;

import jakarta.validation.constraints.*;

import java.util.Objects;
import java.util.Set;

/**
 * 👥 用户管理请求 Record
 * <p>
 * XV03:03 管理端用户管理请求
 * 支持用户创建、更新等操作
 *
 * @author xypai
 * @since 1.0.0
 */
public record UserManagementRequest(
        
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
        String username,
        
        @Email(message = "邮箱格式不正确")
        String email,
        
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String mobile,
        
        String nickname,
        
        @NotNull(message = "用户状态不能为空")
        Boolean enabled,
        
        Set<String> roles
) {
    
    /**
     * 紧凑构造函数 - 数据验证和标准化
     */
    public UserManagementRequest {
        Objects.requireNonNull(username, "用户名不能为空");
        Objects.requireNonNull(enabled, "用户状态不能为空");
        
        // 标准化处理
        username = username.trim().toLowerCase();
        
        if (email != null) {
            email = email.trim().toLowerCase();
        }
        
        if (mobile != null) {
            mobile = mobile.trim();
        }
        
        if (nickname == null || nickname.trim().isEmpty()) {
            nickname = username;
        } else {
            nickname = nickname.trim();
        }
        
        if (roles == null) {
            roles = Set.of("USER"); // 默认角色
        }
    }
    
    /**
     * 创建用户请求
     */
    public static UserManagementRequest forCreate(String username, String email, String mobile) {
        return new UserManagementRequest(username, email, mobile, null, true, null);
    }
    
    /**
     * 更新用户请求
     */
    public static UserManagementRequest forUpdate(String username, String nickname, Boolean enabled, Set<String> roles) {
        return new UserManagementRequest(username, null, null, nickname, enabled, roles);
    }
}

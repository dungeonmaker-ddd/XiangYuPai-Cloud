package com.xypai.security.oauth.feign.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 👤 用户响应DTO
 * <p>
 * 用于接收用户服务返回的用户信息
 *
 * @author xypai
 * @since 3.0.0
 */
@Schema(description = "用户信息响应")
public record UserResponse(

        @Schema(description = "用户ID", example = "1")
        Long id,

        @Schema(description = "手机号（脱敏）", example = "138****8001")
        String mobile,

        @Schema(description = "用户名", example = "testuser001")
        String username,

        @Schema(description = "用户昵称", example = "测试用户")
        String nickname,

        @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
        String avatar,

        @Schema(description = "性别代码", example = "1", allowableValues = {"0", "1", "2", "3"})
        Integer genderCode,

        @Schema(description = "性别描述", example = "男")
        String genderDesc,

        @Schema(description = "用户状态", example = "1", allowableValues = {"1", "2", "3", "4", "5"})
        Integer status,

        @Schema(description = "状态描述", example = "激活")
        String statusDesc,

        @JsonProperty("create_time")
        @Schema(description = "创建时间", example = "2025-01-02T10:30:00")
        LocalDateTime createTime
) {

    /**
     * 转换为认证用户信息
     */
    public AuthUserInfo toAuthUserInfo() {
        // 根据用户状态判断是否启用（1=激活）
        boolean enabled = status != null && status == 1;

        // 根据昵称判断用户类型（简化逻辑）
        String userType = determineUserType();

        // 创建默认角色和权限（实际应从其他服务获取）
        Set<String> defaultRoles = createDefaultRoles();
        Set<String> defaultPermissions = createDefaultPermissions();

        return new AuthUserInfo(
                id, username, nickname, null, mobile,
                enabled, userType, defaultRoles, defaultPermissions, createTime
        );
    }

    /**
     * 判断用户类型
     */
    private String determineUserType() {
        if (username != null && username.equals("admin")) {
            return "admin";
        }
        return "user";
    }

    /**
     * 创建默认角色
     */
    private Set<String> createDefaultRoles() {
        if ("admin".equals(determineUserType())) {
            return Set.of("ADMIN", "USER");
        }
        return Set.of("USER");
    }

    /**
     * 创建默认权限
     */
    private Set<String> createDefaultPermissions() {
        if ("admin".equals(determineUserType())) {
            return Set.of("user:read", "user:write", "admin:all", "system:config");
        }
        return Set.of("user:read");
    }

    /**
     * 认证用户信息Record
     */
    public record AuthUserInfo(
            Long id,
            String username,
            String displayName,
            String email,
            String mobile,
            Boolean enabled,
            String userType,
            Set<String> roles,
            Set<String> permissions,
            LocalDateTime lastLogin
    ) {

        public boolean isAdmin() {
            return roles != null && roles.contains("ADMIN");
        }

        public boolean hasPermission(String permission) {
            return permissions != null && permissions.contains(permission);
        }
    }
}

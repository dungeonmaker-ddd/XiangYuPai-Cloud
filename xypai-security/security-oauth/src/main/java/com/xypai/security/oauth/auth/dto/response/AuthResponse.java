package com.xypai.security.oauth.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/**
 * 🔐 认证响应 DTO (现代化实现)
 * <p>
 * XV03:03 AUTH层 - 认证响应数据传输对象
 * 使用现代Java Records + 模式匹配 + Sealed类
 *
 * @author xypai
 * @since 3.0.0
 */
@Schema(description = "认证响应")
public record AuthResponse(
        @JsonProperty("access_token")
        @Schema(description = "访问令牌(JWT格式)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @JsonProperty("refresh_token")
        @Schema(description = "刷新令牌(JWT格式)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken,

        @JsonProperty("token_type")
        @Schema(description = "令牌类型", example = "Bearer")
        String tokenType,

        @JsonProperty("expires_in")
        @Schema(description = "过期时间(秒)", example = "86400")
        Long expiresIn,

        @JsonProperty("user_info")
        @Schema(description = "用户信息")
        UserInfo userInfo
) {

    /**
     * 紧凑构造器 - 现代化验证
     */
    public AuthResponse {
        Objects.requireNonNull(accessToken, "访问令牌不能为空");
        Objects.requireNonNull(refreshToken, "刷新令牌不能为空");
        Objects.requireNonNull(expiresIn, "过期时间不能为空");
        Objects.requireNonNull(userInfo, "用户信息不能为空");

        // 使用现代化的条件表达式
        tokenType = tokenType == null || tokenType.isBlank() ? "Bearer" : tokenType;

        // 使用现代化的验证
        if (expiresIn <= 0) {
            throw new IllegalArgumentException("过期时间必须大于0，当前值: %d".formatted(expiresIn));
        }
    }

    /**
     * 🏭 现代化工厂方法 - 使用builder模式的简化版
     */
    public static AuthResponse create(String accessToken, String refreshToken, Long expiresIn, UserInfo userInfo) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresIn, userInfo);
    }

    /**
     * 现代化的用户数据验证
     */
    private static void validateUserData(String username, String email, String mobile) {
        // 使用现代化的验证模式
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("用户名长度必须在3-50字符之间，当前: %s".formatted(username));
        }

        // 使用Optional + 现代化验证
        if (email != null && !email.isBlank() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("邮箱格式不正确: %s".formatted(email));
        }

        if (mobile != null && !mobile.isBlank() && !mobile.matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式不正确: %s".formatted(mobile));
        }
    }

    // 现代化的便利方法
    public boolean isExpiringSoon(long thresholdSeconds) {
        return expiresIn <= thresholdSeconds;
    }

    public String getAuthorizationHeader() {
        return "%s %s".formatted(tokenType, accessToken);
    }

    /**
     * 现代化的字符串表示
     */
    @Override
    public String toString() {
        return "AuthResponse[tokenType='%s', expiresIn=%d, user='%s', userType='%s']"
                .formatted(tokenType, expiresIn, userInfo.username(), userInfo.getUserType());
    }

    /**
     * 👤 用户信息 - 使用Sealed类限制继承
     */
    @Schema(description = "用户信息")
    public sealed interface UserInfo
            permits AuthResponse.StandardUser, AuthResponse.AdminUser, AuthResponse.GuestUser {

        Long id();

        String username();

        String displayName();

        String email();

        String mobile();

        Set<String> roles();

        Set<String> permissions();

        Instant lastLogin();

        // 现代化的默认方法
        default boolean hasRole(String role) {
            return roles().contains(role);
        }

        default boolean hasPermission(String permission) {
            return permissions().contains(permission);
        }

        default boolean isAdmin() {
            return hasRole("ADMIN");
        }

        // 使用模式匹配的现代化方法
        default String getUserType() {
            return switch (this) {
                case AdminUser admin -> "管理员";
                case StandardUser user -> "普通用户";
                case GuestUser guest -> "访客";
            };
        }
    }

    /**
     * 🔹 标准用户实现 - Record with sealed interface
     */
    public record StandardUser(
            @Schema(description = "用户ID", example = "1")
            Long id,

            @Schema(description = "用户名", example = "user")
            String username,

            @JsonProperty("display_name")
            @Schema(description = "显示名称", example = "普通用户")
            String displayName,

            @Schema(description = "邮箱", example = "user@xypai.com")
            String email,

            @Schema(description = "手机号", example = "13800138001")
            String mobile,

            @Schema(description = "角色列表", example = "[\"USER\"]")
            Set<String> roles,

            @Schema(description = "权限列表", example = "[\"user:read\", \"profile:edit\"]")
            Set<String> permissions,

            @JsonProperty("last_login")
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
            @Schema(description = "最后登录时间")
            Instant lastLogin
    ) implements UserInfo {

        public StandardUser {
            Objects.requireNonNull(id, "用户ID不能为空");
            Objects.requireNonNull(username, "用户名不能为空");

            // 现代化的默认值处理
            displayName = displayName == null || displayName.isBlank() ? username : displayName;
            roles = roles == null ? Set.of("USER") : Set.copyOf(roles);
            permissions = permissions == null ? Set.of("user:read", "profile:edit") : Set.copyOf(permissions);
            lastLogin = lastLogin == null ? Instant.now() : lastLogin;

            // 现代化的验证
            validateUserData(username, email, mobile);
        }

        /**
         * 🏭 工厂方法
         */
        public static StandardUser create(Long id, String username, String email, String mobile) {
            return new StandardUser(id, username, null, email, mobile, null, null, null);
        }
    }

    /**
     * 🔸 管理员用户实现
     */
    public record AdminUser(
            Long id,
            String username,
            @JsonProperty("display_name")
            String displayName,
            String email,
            String mobile,
            Set<String> roles,
            Set<String> permissions,
            @JsonProperty("last_login")
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
            Instant lastLogin
    ) implements UserInfo {

        public AdminUser {
            Objects.requireNonNull(id, "用户ID不能为空");
            Objects.requireNonNull(username, "用户名不能为空");

            displayName = displayName == null || displayName.isBlank() ? "管理员" : displayName;
            roles = roles == null ? Set.of("ADMIN", "USER") : Set.copyOf(roles);
            permissions = permissions == null ?
                    Set.of("user:read", "user:write", "system:config", "admin:all") :
                    Set.copyOf(permissions);
            lastLogin = lastLogin == null ? Instant.now() : lastLogin;

            validateUserData(username, email, mobile);
        }

        public static AdminUser create(Long id, String username, String email, String mobile) {
            return new AdminUser(id, username, null, email, mobile, null, null, null);
        }
    }

    /**
     * 🔹 访客用户实现
     */
    public record GuestUser(
            Long id,
            String username,
            @JsonProperty("display_name")
            String displayName,
            String email,
            String mobile,
            Set<String> roles,
            Set<String> permissions,
            @JsonProperty("last_login")
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
            Instant lastLogin
    ) implements UserInfo {

        public GuestUser {
            Objects.requireNonNull(id, "用户ID不能为空");
            Objects.requireNonNull(username, "用户名不能为空");

            displayName = displayName == null || displayName.isBlank() ? "访客" : displayName;
            roles = roles == null ? Set.of("GUEST") : Set.copyOf(roles);
            permissions = permissions == null ? Set.of("guest:read") : Set.copyOf(permissions);
            lastLogin = lastLogin == null ? Instant.now() : lastLogin;

            validateUserData(username, email, mobile);
        }

        public static GuestUser create(Long id, String username) {
            return new GuestUser(id, username, null, null, null, null, null, null);
        }
    }
}

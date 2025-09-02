package com.xypai.auth.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 登录结果VO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录结果")
public class LoginResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 访问令牌
     */
    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    /**
     * 刷新令牌
     */
    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    /**
     * 令牌类型
     */
    @Schema(description = "令牌类型", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * 过期时间(秒)
     */
    @Schema(description = "过期时间(秒)", example = "86400")
    private Long expiresIn;

    /**
     * 用户信息
     */
    @Schema(description = "用户信息")
    private UserInfo userInfo;

    /**
     * 用户信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户信息")
    public static class UserInfo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 用户ID
         */
        @Schema(description = "用户ID", example = "1001")
        private Long id;

        /**
         * 用户名
         */
        @Schema(description = "用户名", example = "alice_dev")
        private String username;

        /**
         * 昵称
         */
        @Schema(description = "昵称", example = "Alice·全栈开发")
        private String nickname;

        /**
         * 头像
         */
        @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
        private String avatar;

        /**
         * 手机号(脱敏)
         */
        @Schema(description = "手机号(脱敏)", example = "138****8001")
        private String mobile;

        /**
         * 用户状态
         */
        @Schema(description = "用户状态", example = "1")
        private Integer status;

        /**
         * 角色列表
         */
        @Schema(description = "角色列表", example = "[\"USER\"]")
        private Set<String> roles;

        /**
         * 权限列表
         */
        @Schema(description = "权限列表", example = "[\"user:read\"]")
        private Set<String> permissions;

        /**
         * 最后登录时间
         */
        @Schema(description = "最后登录时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginTime;
    }
}

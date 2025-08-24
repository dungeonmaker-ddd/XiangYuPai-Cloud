package com.xypai.auth.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Objects;

/**
 * 登录响应
 *
 * @param accessToken 访问令牌
 * @param tokenType   令牌类型
 * @param expiresIn   过期时间（秒）
 * @param username    用户名
 * @param nickname    用户昵称
 * @param issuedAt    签发时间
 * @author xypai
 */
@Schema(description = "登录响应")
public record LoginResponse(
        @JsonProperty("access_token")
        @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", required = true)
        String accessToken,

        @JsonProperty("token_type")
        @Schema(description = "令牌类型", example = "Bearer", required = true)
        String tokenType,

        @JsonProperty("expires_in")
        @Schema(description = "过期时间（秒）", example = "7200")
        Long expiresIn,

        @Schema(description = "用户名", example = "admin", required = true)
        String username,

        @Schema(description = "用户昵称", example = "管理员", required = true)
        String nickname,

        @JsonProperty("issued_at")
        @Schema(description = "签发时间", required = true)
        Instant issuedAt
) {
    /**
     * 紧凑构造器 - 验证不变量
     */
    public LoginResponse {
        Objects.requireNonNull(accessToken, "访问令牌不能为空");
        Objects.requireNonNull(username, "用户名不能为空");
        Objects.requireNonNull(nickname, "用户昵称不能为空");
        Objects.requireNonNull(issuedAt, "签发时间不能为空");

        if (accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("访问令牌不能为空字符串");
        }
        if (username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空字符串");
        }
        if (nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("用户昵称不能为空字符串");
        }

        // 设置默认值
        if (tokenType == null || tokenType.trim().isEmpty()) {
            tokenType = "Bearer";
        }

        if (expiresIn != null && expiresIn <= 0) {
            throw new IllegalArgumentException("过期时间必须大于0");
        }
    }

    /**
     * 创建基本登录响应
     */
    public static LoginResponse of(String accessToken, String username, String nickname) {
        return new LoginResponse(accessToken, "Bearer", null, username, nickname, Instant.now());
    }

    /**
     * 创建完整登录响应
     */
    public static LoginResponse of(String accessToken, String tokenType, Long expiresIn,
                                   String username, String nickname) {
        return new LoginResponse(accessToken, tokenType, expiresIn, username, nickname, Instant.now());
    }
}

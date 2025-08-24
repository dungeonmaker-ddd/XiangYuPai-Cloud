package com.xypai.auth.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Objects;

/**
 * 用户注册响应
 *
 * @param userId       用户ID
 * @param username     用户名
 * @param message      注册结果消息
 * @param registeredAt 注册时间
 * @author xypai
 */
@Schema(description = "用户注册响应")
public record RegisterResponse(
        @JsonProperty("user_id")
        @Schema(description = "用户ID", example = "1001", required = true)
        Long userId,

        @Schema(description = "用户名", example = "newuser", required = true)
        String username,

        @Schema(description = "注册结果消息", example = "注册成功", required = true)
        String message,

        @JsonProperty("registered_at")
        @Schema(description = "注册时间", required = true)
        Instant registeredAt
) {
    /**
     * 紧凑构造器 - 验证不变量
     */
    public RegisterResponse {
        Objects.requireNonNull(username, "用户名不能为空");
        Objects.requireNonNull(message, "消息不能为空");
        Objects.requireNonNull(registeredAt, "注册时间不能为空");

        if (username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空字符串");
        }
        if (message.trim().isEmpty()) {
            throw new IllegalArgumentException("消息不能为空字符串");
        }
    }

    /**
     * 创建注册成功响应
     */
    public static RegisterResponse success(Long userId, String username) {
        return new RegisterResponse(userId, username, "注册成功", Instant.now());
    }

    /**
     * 创建自定义消息响应
     */
    public static RegisterResponse of(Long userId, String username, String message) {
        return new RegisterResponse(userId, username, message, Instant.now());
    }
}

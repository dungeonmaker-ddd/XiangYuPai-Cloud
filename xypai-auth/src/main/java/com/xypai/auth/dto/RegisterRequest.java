package com.xypai.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * 用户注册请求
 *
 * @param username        用户名
 * @param password        密码
 * @param confirmPassword 确认密码
 * @author xypai
 */
@Schema(description = "用户注册请求")
public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "用户名只能包含字母、数字、下划线和连字符")
        @Schema(description = "用户名", example = "newuser", required = true)
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 50, message = "密码长度必须在6-50个字符之间")
        @Schema(description = "密码", example = "password123", required = true)
        String password,

        @NotBlank(message = "确认密码不能为空")
        @Schema(description = "确认密码", example = "password123", required = true)
        String confirmPassword
) {
    /**
     * 紧凑构造器 - 验证不变量
     */
    public RegisterRequest {
        Objects.requireNonNull(username, "用户名不能为空");
        Objects.requireNonNull(password, "密码不能为空");
        Objects.requireNonNull(confirmPassword, "确认密码不能为空");

        // 清理输入数据
        username = username.trim();
        password = password.trim();
        confirmPassword = confirmPassword.trim();

        // 验证两次密码是否一致
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }
    }

    /**
     * 创建注册请求
     */
    public static RegisterRequest of(String username, String password, String confirmPassword) {
        return new RegisterRequest(username, password, confirmPassword);
    }
}

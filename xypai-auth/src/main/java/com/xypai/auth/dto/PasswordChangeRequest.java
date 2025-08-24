package com.xypai.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * 密码修改请求
 *
 * @param userId          用户ID（管理员操作时需要）
 * @param oldPassword     旧密码
 * @param newPassword     新密码
 * @param confirmPassword 确认密码
 * @author xypai
 */
@Schema(description = "密码修改请求")
public record PasswordChangeRequest(
        @Positive(message = "用户ID必须为正数")
        @Schema(description = "用户ID（管理员操作时需要）", example = "1")
        Long userId,

        @Size(min = 6, max = 50, message = "旧密码长度必须在6-50个字符之间")
        @Schema(description = "旧密码", example = "123456")
        String oldPassword,

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 50, message = "新密码长度必须在6-50个字符之间")
        @Schema(description = "新密码", example = "newpass123", required = true)
        String newPassword,

        @NotBlank(message = "确认密码不能为空")
        @Schema(description = "确认密码", example = "newpass123", required = true)
        String confirmPassword
) {
    /**
     * 紧凑构造器 - 验证不变量
     */
    public PasswordChangeRequest {
        Objects.requireNonNull(newPassword, "新密码不能为空");
        Objects.requireNonNull(confirmPassword, "确认密码不能为空");

        // 清理输入数据
        newPassword = newPassword.trim();
        confirmPassword = confirmPassword.trim();
        if (oldPassword != null) {
            oldPassword = oldPassword.trim();
        }

        // 验证两次密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }

        // 验证新旧密码不能相同
        if (oldPassword != null && newPassword.equals(oldPassword)) {
            throw new IllegalArgumentException("新密码不能与旧密码相同");
        }
    }

    /**
     * 创建用户自己修改密码的请求
     */
    public static PasswordChangeRequest userChange(String oldPassword, String newPassword, String confirmPassword) {
        return new PasswordChangeRequest(null, oldPassword, newPassword, confirmPassword);
    }

    /**
     * 创建管理员强制修改密码的请求
     */
    public static PasswordChangeRequest adminChange(Long userId, String newPassword, String confirmPassword) {
        return new PasswordChangeRequest(userId, null, newPassword, confirmPassword);
    }
}

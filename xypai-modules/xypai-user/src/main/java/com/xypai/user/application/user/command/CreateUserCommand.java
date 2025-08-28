package com.xypai.user.application.user.command;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * 🔨 创建用户命令
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record CreateUserCommand(
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String mobile,

        @Size(max = 30, message = "用户名长度不能超过30字符")
        String username,

        @NotBlank(message = "昵称不能为空")
        @Size(min = 1, max = 30, message = "昵称长度必须在1-30字符之间")
        String nickname,

        @Size(max = 200, message = "头像URL长度不能超过200字符")
        String avatar,

        @Min(value = 0, message = "性别值只能是0(未知)、1(男)或2(女)")
        @Max(value = 2, message = "性别值只能是0(未知)、1(男)或2(女)")
        Integer gender,

        LocalDate birthDate,

        @NotBlank(message = "客户端类型不能为空")
        @Pattern(regexp = "^(web|app|mini)$", message = "客户端类型只能是web、app或mini")
        String clientType
) {

    public CreateUserCommand {
        // 验证生日不能是未来日期
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("生日不能是未来日期");
        }
    }
}

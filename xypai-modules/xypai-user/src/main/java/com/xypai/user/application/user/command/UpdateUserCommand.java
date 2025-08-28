package com.xypai.user.application.user.command;

import com.xypai.user.domain.user.valueobject.UserId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 📝 更新用户命令
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record UpdateUserCommand(
        @NotNull(message = "用户ID不能为空")
        UserId userId,

        @Size(max = 30, message = "用户名长度不能超过30字符")
        String username,

        @Size(max = 30, message = "昵称长度不能超过30字符")
        String nickname,

        @Size(max = 200, message = "头像URL长度不能超过200字符")
        String avatar,

        @Min(value = 0, message = "性别值只能是0(未知)、1(男)或2(女)")
        @Max(value = 2, message = "性别值只能是0(未知)、1(男)或2(女)")
        Integer gender,

        LocalDate birthDate
) {

    public UpdateUserCommand {
        // 验证生日不能是未来日期
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("生日不能是未来日期");
        }
    }
}

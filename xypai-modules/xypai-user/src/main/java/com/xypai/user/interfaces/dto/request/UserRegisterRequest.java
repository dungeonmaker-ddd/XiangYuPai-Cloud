package com.xypai.user.interfaces.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 用户注册请求
 *
 * @author XyPai
 */
public record UserRegisterRequest(
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String mobile,

        @Size(max = 30, message = "用户名不能超过30个字符")
        String username,

        @NotBlank(message = "昵称不能为空")
        @Size(min = 1, max = 30, message = "昵称长度必须在1-30个字符之间")
        String nickname,

        @Size(max = 200, message = "头像URL不能超过200个字符")
        String avatar,

        @Min(value = 0, message = "性别值必须为0、1或2")
        @Max(value = 2, message = "性别值必须为0、1或2")
        Integer gender,

        LocalDate birthDate,

        @NotBlank(message = "客户端类型不能为空")
        @Pattern(regexp = "^(web|app|mini)$", message = "客户端类型只能是web、app或mini")
        String clientType
) {
    public UserRegisterRequest {
        Objects.requireNonNull(mobile, "手机号不能为null");
        Objects.requireNonNull(nickname, "昵称不能为null");
        Objects.requireNonNull(clientType, "客户端类型不能为null");

        // 设置默认值
        if (gender == null) {
            gender = 0; // 默认未知
        }

        // 验证生日不能是未来日期
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("生日不能是未来日期");
        }
    }

    public static UserRegisterRequest of(String mobile, String nickname, String clientType) {
        return new UserRegisterRequest(mobile, null, nickname, null, 0, null, clientType);
    }
}

package com.xypai.user.domain.record;

import jakarta.validation.constraints.*;

import java.util.Objects;
import java.util.Optional;

/**
 * 用户更新请求记录
 *
 * @author XyPai
 */
public record UserUpdateRequest(
        @NotNull(message = "用户ID不能为空")
        @Positive(message = "用户ID必须为正数")
        Long userId,

        @Size(min = 1, max = 30, message = "昵称长度必须在1-30个字符之间")
        Optional<String> nickname,

        @Email(message = "邮箱格式不正确")
        @Size(max = 50, message = "邮箱长度不能超过50个字符")
        Optional<String> email,

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        Optional<String> phone,

        @Min(value = 0, message = "性别值无效")
        @Max(value = 2, message = "性别值无效")
        Optional<Integer> gender,

        @Size(max = 30, message = "部门名称长度不能超过30个字符")
        Optional<String> deptName,

        @Size(max = 100, message = "备注长度不能超过100个字符")
        Optional<String> remark,

        @Min(value = 0, message = "状态值无效")
        @Max(value = 1, message = "状态值无效")
        Optional<Integer> status
) {
    public UserUpdateRequest {
        Objects.requireNonNull(userId, "用户ID不能为null");

        // 验证Optional字段的值
        nickname.ifPresent(value -> {
            if (value.trim().isEmpty()) {
                throw new IllegalArgumentException("昵称不能为空字符串");
            }
        });

        email.ifPresent(value -> {
            if (value.trim().isEmpty()) {
                throw new IllegalArgumentException("邮箱不能为空字符串");
            }
        });

        phone.ifPresent(value -> {
            if (value.trim().isEmpty()) {
                throw new IllegalArgumentException("手机号不能为空字符串");
            }
        });
    }

    /**
     * 创建仅更新昵称的请求
     */
    public static UserUpdateRequest nickname(Long userId, String nickname) {
        return new UserUpdateRequest(
                userId,
                Optional.of(nickname),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

    /**
     * 创建仅更新邮箱的请求
     */
    public static UserUpdateRequest email(Long userId, String email) {
        return new UserUpdateRequest(
                userId,
                Optional.empty(),
                Optional.of(email),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

    /**
     * 创建仅更新状态的请求
     */
    public static UserUpdateRequest status(Long userId, Integer status) {
        return new UserUpdateRequest(
                userId,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(status)
        );
    }

    /**
     * 设置用户ID
     */
    public UserUpdateRequest withId(Long newUserId) {
        return new UserUpdateRequest(
                newUserId,
                nickname,
                email,
                phone,
                gender,
                deptName,
                remark,
                status
        );
    }
}

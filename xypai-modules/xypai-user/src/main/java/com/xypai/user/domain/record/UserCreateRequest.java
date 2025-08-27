package com.xypai.user.domain.record;

import jakarta.validation.constraints.*;

import java.util.Objects;

/**
 * 用户创建请求记录
 *
 * @author XyPai
 */
public record UserCreateRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
        String username,

        @NotBlank(message = "昵称不能为空")
        @Size(min = 1, max = 30, message = "昵称长度必须在1-30个字符之间")
        String nickname,

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        @Size(max = 50, message = "邮箱长度不能超过50个字符")
        String email,

        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
        String password,

        @NotNull(message = "性别不能为空")
        @Min(value = 0, message = "性别值无效")
        @Max(value = 2, message = "性别值无效")
        Integer gender,

        @Size(max = 30, message = "部门名称长度不能超过30个字符")
        String deptName,

        @Size(max = 100, message = "备注长度不能超过100个字符")
        String remark
) {
    public UserCreateRequest {
        Objects.requireNonNull(username, "用户名不能为null");
        Objects.requireNonNull(nickname, "昵称不能为null");
        Objects.requireNonNull(email, "邮箱不能为null");
        Objects.requireNonNull(phone, "手机号不能为null");
        Objects.requireNonNull(password, "密码不能为null");
        Objects.requireNonNull(gender, "性别不能为null");

        // 去除前后空格并验证
        username = username.trim();
        nickname = nickname.trim();
        email = email.trim().toLowerCase();
        phone = phone.trim();
        password = password.trim();

        if (username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空字符串");
        }
        if (nickname.isEmpty()) {
            throw new IllegalArgumentException("昵称不能为空字符串");
        }
        if (email.isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空字符串");
        }
        if (phone.isEmpty()) {
            throw new IllegalArgumentException("手机号不能为空字符串");
        }
        if (password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空字符串");
        }
    }

    /**
     * 创建基础用户请求
     */
    public static UserCreateRequest of(String username, String nickname, String email,
                                       String phone, String password, Integer gender) {
        return new UserCreateRequest(username, nickname, email, phone, password, gender, null, null);
    }

    /**
     * 创建完整用户请求
     */
    public static UserCreateRequest withDept(String username, String nickname, String email,
                                             String phone, String password, Integer gender,
                                             String deptName, String remark) {
        return new UserCreateRequest(username, nickname, email, phone, password, gender, deptName, remark);
    }
}

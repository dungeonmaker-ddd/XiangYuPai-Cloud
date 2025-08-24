package com.xypai.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.Objects;
import java.util.Set;

/**
 * 用户创建请求
 *
 * @param username 用户名
 * @param password 密码
 * @param nickname 昵称
 * @param email    邮箱
 * @param mobile   手机号
 * @param roleIds  角色ID集合
 * @param deptId   部门ID
 * @author xypai
 */
@Schema(description = "用户创建请求")
public record UserCreateRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "用户名只能包含字母、数字、下划线和连字符")
        @Schema(description = "用户名", example = "zhangsan", required = true)
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 50, message = "密码长度必须在6-50个字符之间")
        @Schema(description = "密码", example = "123456", required = true)
        String password,

        @Size(max = 50, message = "昵称长度不能超过50个字符")
        @Schema(description = "用户昵称", example = "张三")
        String nickname,

        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱长度不能超过100个字符")
        @Schema(description = "邮箱", example = "zhangsan@example.com")
        String email,

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        @Schema(description = "手机号", example = "13800138000")
        String mobile,

        @Schema(description = "角色ID集合", example = "[1, 2]")
        Set<@Positive(message = "角色ID必须为正数") Long> roleIds,

        @Positive(message = "部门ID必须为正数")
        @Schema(description = "部门ID", example = "1")
        Long deptId
) {
    /**
     * 紧凑构造器 - 验证不变量
     */
    public UserCreateRequest {
        Objects.requireNonNull(username, "用户名不能为空");
        Objects.requireNonNull(password, "密码不能为空");

        // 清理输入数据
        username = username.trim();
        password = password.trim();

        if (nickname != null) {
            nickname = nickname.trim();
            if (nickname.isEmpty()) {
                nickname = username; // 默认使用用户名作为昵称
            }
        } else {
            nickname = username;
        }

        if (email != null) {
            email = email.trim().toLowerCase();
            if (email.isEmpty()) {
                email = null;
            }
        }

        if (mobile != null) {
            mobile = mobile.trim();
            if (mobile.isEmpty()) {
                mobile = null;
            }
        }

        // 防御性复制
        if (roleIds != null) {
            roleIds = Set.copyOf(roleIds);
        }
    }

    /**
     * 创建简单用户（仅用户名密码）
     */
    public static UserCreateRequest simple(String username, String password) {
        return new UserCreateRequest(username, password, null, null, null, null, null);
    }

    /**
     * 创建带联系方式的用户
     */
    public static UserCreateRequest withContact(String username, String password, String email, String mobile) {
        return new UserCreateRequest(username, password, null, email, mobile, null, null);
    }
}

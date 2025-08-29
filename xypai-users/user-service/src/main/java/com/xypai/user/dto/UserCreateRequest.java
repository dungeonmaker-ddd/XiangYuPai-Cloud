package com.xypai.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 📖 用户注册请求 - MVP版本
 * <p>
 * 🔧 字段说明:
 *
 * @param mobile   手机号，必填，11位数字，用于登录和验证
 * @param username 用户名，必填，3-20字符，支持字母数字下划线
 * @param nickname 昵称，必填，1-50字符，用于显示
 *                 <p>
 *                 📋 请求示例:
 *                 {
 *                 "mobile": "13800138001",
 *                 "username": "testuser001",
 *                 "nickname": "测试用户昵称"
 *                 }
 * @author XyPai Team
 * @since 2025-01-02
 */
@Schema(description = "用户注册请求")
public record UserCreateRequest(

        @Schema(description = "手机号，11位数字，用于登录和验证", example = "13800138001", pattern = "^1[3-9]\\d{9}$")
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
        String mobile,

        @Schema(description = "用户名，3-20字符，支持字母数字下划线", example = "testuser001", minLength = 3, maxLength = 20)
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名长度必须在3-20字符之间")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字、下划线")
        String username,

        @Schema(description = "用户昵称，1-50字符，用于显示", example = "测试用户昵称", minLength = 1, maxLength = 50)
        @NotBlank(message = "昵称不能为空")
        @Size(min = 1, max = 50, message = "昵称长度必须在1-50字符之间")
        String nickname

) {

    /**
     * 📖 创建默认注册请求
     *
     * @param mobile   手机号
     * @param username 用户名
     * @return 默认注册请求，昵称等于用户名
     */
    public static UserCreateRequest of(String mobile, String username) {
        return new UserCreateRequest(mobile, username, username);
    }

    /**
     * 📖 创建完整注册请求
     *
     * @param mobile   手机号
     * @param username 用户名
     * @param nickname 昵称
     * @return 完整注册请求
     */
    public static UserCreateRequest of(String mobile, String username, String nickname) {
        return new UserCreateRequest(mobile, username, nickname);
    }
}

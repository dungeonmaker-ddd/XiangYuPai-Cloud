package com.xypai.auth.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * 登录请求
 *
 * @param username   用户名/手机号/邮箱
 * @param password   密码
 * @param clientType 客户端类型
 * @param deviceId   设备ID
 * @author xypai
 */
@Schema(description = "登录请求")
public record LoginRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 2, max = 50, message = "用户名长度必须在2-50个字符之间")
        @Schema(description = "用户名/手机号/邮箱", example = "admin", required = true)
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 50, message = "密码长度必须在6-50个字符之间")
        @Schema(description = "密码", example = "123456", required = true)
        String password,

        @NotNull(message = "客户端类型不能为空")
        @Pattern(regexp = "^(web|app|mini)$", message = "客户端类型只能是web、app或mini")
        @Schema(description = "客户端类型", example = "web", allowableValues = {"web", "app", "mini"}, required = true)
        String clientType,

        @Schema(description = "设备ID", example = "device-123456")
        String deviceId
) {
    /**
     * 紧凑构造器 - 验证不变量
     */
    public LoginRequest {
        Objects.requireNonNull(username, "用户名不能为空");
        Objects.requireNonNull(password, "密码不能为空");
        Objects.requireNonNull(clientType, "客户端类型不能为空");

        // 清理输入数据
        username = username.trim();
        password = password.trim();
        clientType = clientType.trim().toLowerCase();
        if (deviceId != null) {
            deviceId = deviceId.trim();
        }
    }


    /**
     * 创建Web端登录请求
     */
    public static LoginRequest web(String username, String password) {
        return new LoginRequest(username, password, "web", null);
    }

    /**
     * 创建App端登录请求
     */
    public static LoginRequest app(String username, String password, String deviceId) {
        return new LoginRequest(username, password, "app", deviceId);
    }

    /**
     * 创建小程序登录请求
     */
    public static LoginRequest mini(String username, String password) {
        return new LoginRequest(username, password, "mini", null);
    }
}

package com.xypai.auth.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * 短信验证码登录请求
 *
 * @param mobile     手机号
 * @param code       验证码
 * @param clientType 客户端类型
 * @param deviceId   设备ID
 * @author xypai
 */
@Schema(description = "短信验证码登录请求")
public record SmsLoginRequest(
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        @Schema(description = "手机号", example = "13800138000", required = true)
        String mobile,

        @NotBlank(message = "验证码不能为空")
        @Size(min = 6, max = 6, message = "验证码必须为6位数字")
        @Pattern(regexp = "\\d{6}", message = "验证码必须为6位数字")
        @Schema(description = "短信验证码", example = "123456", required = true)
        String code,

        @NotNull(message = "客户端类型不能为空")
        @Pattern(regexp = "^(web|app|mini)$", message = "客户端类型只能是web、app或mini")
        @Schema(description = "客户端类型", example = "app", allowableValues = {"web", "app", "mini"}, required = true)
        String clientType,

        @Schema(description = "设备ID", example = "device-123456")
        String deviceId
) {
    /**
     * 紧凑构造器 - 验证不变量
     */
    public SmsLoginRequest {
        Objects.requireNonNull(mobile, "手机号不能为空");
        Objects.requireNonNull(code, "验证码不能为空");
        Objects.requireNonNull(clientType, "客户端类型不能为空");

        // 清理输入数据
        mobile = mobile.trim();
        code = code.trim();
        clientType = clientType.trim().toLowerCase();
        if (deviceId != null) {
            deviceId = deviceId.trim();
        }
    }

    /**
     * 创建App端短信登录请求
     */
    public static SmsLoginRequest app(String mobile, String code, String deviceId) {
        return new SmsLoginRequest(mobile, code, "app", deviceId);
    }

    /**
     * 创建Web端短信登录请求
     */
    public static SmsLoginRequest web(String mobile, String code) {
        return new SmsLoginRequest(mobile, code, "web", null);
    }
}

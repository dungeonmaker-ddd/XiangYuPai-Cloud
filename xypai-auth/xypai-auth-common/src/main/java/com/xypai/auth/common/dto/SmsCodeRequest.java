package com.xypai.auth.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Objects;

/**
 * 短信验证码发送请求
 *
 * @param mobile     手机号
 * @param clientType 客户端类型
 * @author xypai
 */
@Schema(description = "短信验证码发送请求")
public record SmsCodeRequest(
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        @Schema(description = "手机号", example = "13800138000", required = true)
        String mobile,

        @Pattern(regexp = "^(web|app|mini)$", message = "客户端类型只能是web、app或mini")
        @Schema(description = "客户端类型", example = "app", allowableValues = {"web", "app", "mini"})
        String clientType
) {
    /**
     * 紧凑构造器 - 验证不变量
     */
    public SmsCodeRequest {
        Objects.requireNonNull(mobile, "手机号不能为空");

        // 清理输入数据
        mobile = mobile.trim();

        // 设置默认客户端类型
        if (clientType == null || clientType.trim().isEmpty()) {
            clientType = "app";
        } else {
            clientType = clientType.trim().toLowerCase();
        }
    }

    /**
     * 创建App端短信验证码请求
     */
    public static SmsCodeRequest app(String mobile) {
        return new SmsCodeRequest(mobile, "app");
    }

    /**
     * 创建Web端短信验证码请求
     */
    public static SmsCodeRequest web(String mobile) {
        return new SmsCodeRequest(mobile, "web");
    }
}

package com.xypai.auth.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短信登录请求DTO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "短信登录请求")
public class SmsLoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138001")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    /**
     * 短信验证码
     */
    @Schema(description = "短信验证码", example = "123456")
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    private String smsCode;

    /**
     * 客户端类型
     */
    @Schema(description = "客户端类型", example = "web", allowableValues = {"web", "app", "mini"})
    @Builder.Default
    private String clientType = "web";

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "device_12345")
    private String deviceId;

    /**
     * 是否记住登录
     */
    @Schema(description = "是否记住登录", example = "false")
    @Builder.Default
    private Boolean rememberMe = false;
}

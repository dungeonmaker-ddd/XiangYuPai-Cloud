package com.xypai.auth.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;

/**
 * 密码登录请求DTO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "密码登录请求")
public class LoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "alice_dev")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 128, message = "密码长度必须在6-128个字符之间")
    private String password;

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

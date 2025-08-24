package com.xypai.auth.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Objects;

/**
 * 短信验证码发送响应
 *
 * @param mobile     手机号（脱敏显示）
 * @param message    响应消息
 * @param sentAt     发送时间
 * @param expiresIn  验证码有效期（秒）
 * @param nextSendIn 下次发送间隔（秒）
 * @author xypai
 */
@Schema(description = "短信验证码发送响应")
public record SmsCodeResponse(
        @Schema(description = "手机号（脱敏）", example = "138****8000", required = true)
        String mobile,

        @Schema(description = "响应消息", example = "验证码已发送", required = true)
        String message,

        @JsonProperty("sent_at")
        @Schema(description = "发送时间", required = true)
        Instant sentAt,

        @JsonProperty("expires_in")
        @Schema(description = "验证码有效期（秒）", example = "300", required = true)
        Integer expiresIn,

        @JsonProperty("next_send_in")
        @Schema(description = "下次发送间隔（秒）", example = "60")
        Integer nextSendIn
) {
    /**
     * 紧凑构造器 - 验证不变量
     */
    public SmsCodeResponse {
        Objects.requireNonNull(mobile, "手机号不能为空");
        Objects.requireNonNull(message, "响应消息不能为空");
        Objects.requireNonNull(sentAt, "发送时间不能为空");
        Objects.requireNonNull(expiresIn, "有效期不能为空");

        if (mobile.trim().isEmpty()) {
            throw new IllegalArgumentException("手机号不能为空字符串");
        }
        if (message.trim().isEmpty()) {
            throw new IllegalArgumentException("响应消息不能为空字符串");
        }
        if (expiresIn <= 0) {
            throw new IllegalArgumentException("有效期必须大于0");
        }
        if (nextSendIn != null && nextSendIn <= 0) {
            throw new IllegalArgumentException("下次发送间隔必须大于0");
        }
    }

    /**
     * 手机号脱敏处理
     */
    private static String maskMobile(String mobile) {
        if (mobile == null || mobile.length() != 11) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(7);
    }

    /**
     * 创建发送成功响应
     */
    public static SmsCodeResponse success(String mobile, Integer expiresIn) {
        return new SmsCodeResponse(
                maskMobile(mobile),
                "验证码已发送",
                Instant.now(),
                expiresIn,
                60 // 默认60秒后可重新发送
        );
    }

    /**
     * 创建发送成功响应（自定义间隔）
     */
    public static SmsCodeResponse success(String mobile, Integer expiresIn, Integer nextSendIn) {
        return new SmsCodeResponse(
                maskMobile(mobile),
                "验证码已发送",
                Instant.now(),
                expiresIn,
                nextSendIn
        );
    }

    /**
     * 创建自定义消息响应
     */
    public static SmsCodeResponse of(String mobile, String message, Integer expiresIn) {
        return new SmsCodeResponse(
                maskMobile(mobile),
                message,
                Instant.now(),
                expiresIn,
                null
        );
    }
}

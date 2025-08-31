package com.xypai.sms.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * DTO: 短信模板
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
public record SmsTemplateDto(
        Long id,

        @NotBlank(message = "模板代码不能为空")
        @Size(max = 50, message = "模板代码长度不能超过50字符")
        String templateCode,

        @NotBlank(message = "模板名称不能为空")
        @Size(max = 100, message = "模板名称长度不能超过100字符")
        String templateName,

        @NotBlank(message = "模板内容不能为空")
        @Size(max = 500, message = "模板内容长度不能超过500字符")
        String content,

        @NotNull(message = "模板类型不能为空")
        String templateType,

        @NotEmpty(message = "支持的渠道不能为空")
        Set<String> supportedChannels,

        Map<String, String> parameterConfig,

        String status,
        String auditComment,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /**
     * DTO: 紧凑构造器验证
     */
    public SmsTemplateDto {
        if (templateCode != null) {
            templateCode = templateCode.trim().toUpperCase();
            if (!templateCode.matches("^[A-Z][A-Z0-9_]{2,49}$")) {
                throw new IllegalArgumentException("模板代码格式不正确，必须以字母开头，只能包含大写字母、数字和下划线");
            }
        }

        if (templateName != null) {
            templateName = templateName.trim();
        }

        if (content != null) {
            content = content.trim();
        }

        if (parameterConfig == null) {
            parameterConfig = Map.of();
        }
    }

    /**
     * DTO: 创建验证码模板
     */
    public static SmsTemplateDto createVerificationTemplate(
            String templateCode,
            String templateName,
            String content,
            Set<String> supportedChannels) {
        return new SmsTemplateDto(
                null,
                templateCode,
                templateName,
                content,
                "VERIFICATION",
                supportedChannels,
                Map.of("code", "验证码", "minutes", "有效时间"),
                null,
                null,
                "验证码模板",
                null,
                null
        );
    }

    /**
     * DTO: 创建通知模板
     */
    public static SmsTemplateDto createNotificationTemplate(
            String templateCode,
            String templateName,
            String content,
            Set<String> supportedChannels,
            Map<String, String> parameterConfig) {
        return new SmsTemplateDto(
                null,
                templateCode,
                templateName,
                content,
                "NOTIFICATION",
                supportedChannels,
                parameterConfig,
                null,
                null,
                "通知模板",
                null,
                null
        );
    }

    /**
     * DTO: 检查模板是否可用
     */
    public boolean isAvailable() {
        return "ACTIVE".equals(status);
    }

    /**
     * DTO: 检查是否为验证码模板
     */
    public boolean isVerificationTemplate() {
        return "VERIFICATION".equals(templateType);
    }
}

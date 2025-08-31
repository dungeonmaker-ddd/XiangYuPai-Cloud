package com.xypai.sms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 📱 短信模板DTO
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Builder
@Schema(description = "短信模板数据传输对象")
public record SmsTemplateDTO(

        @Schema(description = "模板ID", example = "1")
        Long id,

        @Schema(description = "模板编号", example = "REGISTER_VERIFY")
        @NotBlank(message = "模板编号不能为空")
        @Size(max = 50, message = "模板编号长度不能超过50字符")
        String templateCode,

        @Schema(description = "模板名称", example = "注册验证码")
        @NotBlank(message = "模板名称不能为空")
        @Size(max = 100, message = "模板名称长度不能超过100字符")
        String templateName,

        @Schema(description = "模板内容", example = "您的验证码是{code}，请在{minutes}分钟内输入。")
        @NotBlank(message = "模板内容不能为空")
        @Size(max = 500, message = "模板内容长度不能超过500字符")
        String content,

        @Schema(description = "模板类型", example = "VERIFICATION", allowableValues = {"NOTIFICATION", "VERIFICATION", "MARKETING"})
        @NotBlank(message = "模板类型不能为空")
        String templateType,

        @Schema(description = "支持的渠道", example = "[\"ALIYUN\", \"TENCENT\"]")
        @NotEmpty(message = "支持的渠道不能为空")
        java.util.Set<String> supportedChannels,

        @Schema(description = "模板参数配置", example = "{\"code\": \"验证码\", \"minutes\": \"有效时间\"}")
        Map<String, String> parameterConfig,

        @Schema(description = "状态", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "PENDING_APPROVAL"})
        String status,

        @Schema(description = "审核意见", example = "模板内容符合规范")
        String auditComment,

        @Schema(description = "创建时间", example = "2025-01-02T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "更新时间", example = "2025-01-02T10:30:00")
        LocalDateTime updatedAt
) {

    /**
     * 🏭 创建新模板DTO的工厂方法
     */
    public static SmsTemplateDTO createNew(
            String templateCode,
            String templateName,
            String content,
            String templateType,
            java.util.Set<String> supportedChannels,
            Map<String, String> parameterConfig) {
        return SmsTemplateDTO.builder()
                .templateCode(templateCode)
                .templateName(templateName)
                .content(content)
                .templateType(templateType)
                .supportedChannels(supportedChannels)
                .parameterConfig(parameterConfig)
                .status("PENDING_APPROVAL")
                .build();
    }

    /**
     * 🔍 检查是否支持指定渠道
     */
    public boolean supportsChannel(String channel) {
        return supportedChannels != null && supportedChannels.contains(channel);
    }

    /**
     * ✅ 检查模板是否可用
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}

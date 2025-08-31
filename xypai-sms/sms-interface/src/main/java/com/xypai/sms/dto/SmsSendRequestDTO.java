package com.xypai.sms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Map;
import java.util.Set;

/**
 * 📤 短信发送请求DTO
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Builder
@Schema(description = "短信发送请求数据传输对象")
public record SmsSendRequestDTO(

        @Schema(description = "模板编号", example = "REGISTER_VERIFY", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "模板编号不能为空")
        String templateCode,

        @Schema(description = "接收手机号", example = "[\"13800138000\", \"13900139000\"]", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "接收手机号不能为空")
        @Size(max = 100, message = "单次发送手机号数量不能超过100个")
        Set<@Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String> phoneNumbers,

        @Schema(description = "模板参数", example = "{\"code\": \"123456\", \"minutes\": \"5\"}")
        Map<String, String> templateParams,

        @Schema(description = "签名编号", example = "DEFAULT_SIGN")
        String signCode,

        @Schema(description = "优先渠道", example = "ALIYUN")
        String preferredChannel,

        @Schema(description = "负载均衡策略", example = "ROUND_ROBIN", allowableValues = {"ROUND_ROBIN", "RANDOM", "WEIGHT_RANDOM", "HASH"})
        String loadBalanceStrategy,

        @Schema(description = "是否异步发送", example = "true")
        Boolean async,

        @Schema(description = "业务标识", example = "USER_REGISTER")
        String businessTag,

        @Schema(description = "请求ID", example = "req_123456789")
        String requestId
) {

    /**
     * 🏭 创建同步发送请求的工厂方法
     */
    public static SmsSendRequestDTO createSync(
            String templateCode,
            Set<String> phoneNumbers,
            Map<String, String> templateParams) {
        return SmsSendRequestDTO.builder()
                .templateCode(templateCode)
                .phoneNumbers(phoneNumbers)
                .templateParams(templateParams)
                .async(false)
                .loadBalanceStrategy("ROUND_ROBIN")
                .build();
    }

    /**
     * 🏭 创建异步发送请求的工厂方法
     */
    public static SmsSendRequestDTO createAsync(
            String templateCode,
            Set<String> phoneNumbers,
            Map<String, String> templateParams,
            String businessTag) {
        return SmsSendRequestDTO.builder()
                .templateCode(templateCode)
                .phoneNumbers(phoneNumbers)
                .templateParams(templateParams)
                .businessTag(businessTag)
                .async(true)
                .loadBalanceStrategy("ROUND_ROBIN")
                .build();
    }

    /**
     * 🔍 检查是否为异步请求
     */
    public boolean isAsync() {
        return Boolean.TRUE.equals(async);
    }

    /**
     * 📱 获取手机号数量
     */
    public int getPhoneCount() {
        return phoneNumbers != null ? phoneNumbers.size() : 0;
    }
}

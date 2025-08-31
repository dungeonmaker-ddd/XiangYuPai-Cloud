package com.xypai.sms.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * DTO: 短信发送请求
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
public record SmsSendRequest(

        @NotBlank(message = "模板代码不能为空")
        String templateCode,

        @NotEmpty(message = "手机号列表不能为空")
        @Size(max = 100, message = "单次发送手机号数量不能超过100个")
        Set<@Pattern(regexp = "^1[3-9]\\d{9}$|^\\+?[1-9]\\d{6,14}$", message = "手机号格式不正确") String> phoneNumbers,

        Map<String, String> templateParams,

        String preferredChannel,

        @Pattern(regexp = "^(ROUND_ROBIN|RANDOM|WEIGHT_RANDOM|HASH)$", message = "负载均衡策略不正确")
        String loadBalanceStrategy,

        String businessTag,

        Boolean async,

        String requestId
) {

    /**
     * DTO: 紧凑构造器验证
     */
    public SmsSendRequest {
        Objects.requireNonNull(templateCode, "模板代码不能为空");
        Objects.requireNonNull(phoneNumbers, "手机号列表不能为空");

        if (phoneNumbers.isEmpty()) {
            throw new IllegalArgumentException("手机号列表不能为空");
        }

        if (phoneNumbers.size() > 100) {
            throw new IllegalArgumentException("单次发送手机号数量不能超过100个");
        }

        // 设置默认值
        if (loadBalanceStrategy == null || loadBalanceStrategy.trim().isEmpty()) {
            loadBalanceStrategy = "ROUND_ROBIN";
        }

        if (async == null) {
            async = false;
        }

        if (templateParams == null) {
            templateParams = Map.of();
        }
    }

    /**
     * DTO: 创建同步发送请求
     */
    public static SmsSendRequest createSync(
            String templateCode,
            Set<String> phoneNumbers,
            Map<String, String> templateParams) {
        return new SmsSendRequest(
                templateCode,
                phoneNumbers,
                templateParams,
                null,
                "ROUND_ROBIN",
                null,
                false,
                null
        );
    }

    /**
     * DTO: 创建异步发送请求
     */
    public static SmsSendRequest createAsync(
            String templateCode,
            Set<String> phoneNumbers,
            Map<String, String> templateParams,
            String businessTag) {
        return new SmsSendRequest(
                templateCode,
                phoneNumbers,
                templateParams,
                null,
                "ROUND_ROBIN",
                businessTag,
                true,
                null
        );
    }

    /**
     * DTO: 检查是否为异步发送
     */
    public boolean isAsync() {
        return Boolean.TRUE.equals(async);
    }

    /**
     * DTO: 获取手机号数量
     */
    public int getPhoneCount() {
        return phoneNumbers != null ? phoneNumbers.size() : 0;
    }
}

package com.xypai.sms.service.business;

import com.xypai.sms.common.constant.SmsConstants;
import com.xypai.sms.controller.dto.SmsSendRequest;
import com.xypai.sms.controller.dto.SmsTemplateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Business: 数据验证业务服务
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private static final Pattern CHINA_MOBILE_PATTERN = Pattern.compile(SmsConstants.Regex.CHINA_MOBILE);
    private static final Pattern INTERNATIONAL_PHONE_PATTERN = Pattern.compile(SmsConstants.Regex.INTERNATIONAL_PHONE);
    private static final Pattern TEMPLATE_CODE_PATTERN = Pattern.compile(SmsConstants.Regex.TEMPLATE_CODE);

    /**
     * Business: 验证发送请求
     */
    public void validateSendRequest(SmsSendRequest request) {
        Objects.requireNonNull(request, "发送请求不能为空");

        // 验证模板代码
        validateTemplateCode(request.templateCode());

        // 验证手机号列表
        if (request.phoneNumbers() == null || request.phoneNumbers().isEmpty()) {
            throw new IllegalArgumentException("手机号列表不能为空");
        }

        if (request.phoneNumbers().size() > SmsConstants.Limit.MAX_PHONE_COUNT) {
            throw new IllegalArgumentException("单次发送手机号数量不能超过" + SmsConstants.Limit.MAX_PHONE_COUNT + "个");
        }

        // 验证负载均衡策略
        if (request.loadBalanceStrategy() != null) {
            validateLoadBalanceStrategy(request.loadBalanceStrategy());
        }
    }

    /**
     * Business: 验证手机号列表
     */
    public Set<String> validatePhoneNumbers(Set<String> phoneNumbers) {
        Set<String> validPhones = new HashSet<>();

        for (String phone : phoneNumbers) {
            if (phone == null || phone.trim().isEmpty()) {
                log.warn("Business: 忽略空手机号");
                continue;
            }

            String cleanPhone = phone.trim();

            if (isValidPhone(cleanPhone)) {
                validPhones.add(cleanPhone);
            } else {
                log.warn("Business: 无效手机号格式, phone={}", maskPhone(cleanPhone));
                throw new IllegalArgumentException("手机号格式不正确: " + maskPhone(cleanPhone));
            }
        }

        if (validPhones.isEmpty()) {
            throw new IllegalArgumentException("没有有效的手机号");
        }

        return validPhones;
    }

    /**
     * Business: 验证模板数据
     */
    public void validateTemplate(SmsTemplateDto template) {
        Objects.requireNonNull(template, "模板数据不能为空");

        // 验证模板代码
        validateTemplateCode(template.templateCode());

        // 验证模板名称
        if (template.templateName() == null || template.templateName().trim().isEmpty()) {
            throw new IllegalArgumentException("模板名称不能为空");
        }

        if (template.templateName().length() > SmsConstants.Limit.MAX_TEMPLATE_NAME_LENGTH) {
            throw new IllegalArgumentException("模板名称长度不能超过" + SmsConstants.Limit.MAX_TEMPLATE_NAME_LENGTH + "字符");
        }

        // 验证模板内容
        if (template.content() == null || template.content().trim().isEmpty()) {
            throw new IllegalArgumentException("模板内容不能为空");
        }

        if (template.content().length() > SmsConstants.Limit.MAX_TEMPLATE_LENGTH) {
            throw new IllegalArgumentException("模板内容长度不能超过" + SmsConstants.Limit.MAX_TEMPLATE_LENGTH + "字符");
        }

        // 验证模板类型
        validateTemplateType(template.templateType());

        // 验证支持的渠道
        if (template.supportedChannels() == null || template.supportedChannels().isEmpty()) {
            throw new IllegalArgumentException("至少需要支持一个渠道");
        }

        // 检查敏感词
        if (containsSensitiveWords(template.content())) {
            throw new IllegalArgumentException("模板内容包含敏感词，请修改后重新提交");
        }
    }

    /**
     * Business: 验证模板代码
     */
    private void validateTemplateCode(String templateCode) {
        if (templateCode == null || templateCode.trim().isEmpty()) {
            throw new IllegalArgumentException("模板代码不能为空");
        }

        String code = templateCode.trim().toUpperCase();

        if (!TEMPLATE_CODE_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException("模板代码格式不正确，必须以字母开头，只能包含大写字母、数字和下划线");
        }

        if (code.length() > SmsConstants.Limit.MAX_TEMPLATE_CODE_LENGTH) {
            throw new IllegalArgumentException("模板代码长度不能超过" + SmsConstants.Limit.MAX_TEMPLATE_CODE_LENGTH + "字符");
        }
    }

    /**
     * Business: 验证模板类型
     */
    private void validateTemplateType(String templateType) {
        if (templateType == null || templateType.trim().isEmpty()) {
            throw new IllegalArgumentException("模板类型不能为空");
        }

        String[] validTypes = {
                SmsConstants.TemplateType.NOTIFICATION,
                SmsConstants.TemplateType.VERIFICATION,
                SmsConstants.TemplateType.MARKETING,
                SmsConstants.TemplateType.SYSTEM
        };

        boolean isValid = false;
        for (String validType : validTypes) {
            if (validType.equals(templateType)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new IllegalArgumentException("无效的模板类型: " + templateType);
        }
    }

    /**
     * Business: 验证负载均衡策略
     */
    private void validateLoadBalanceStrategy(String strategy) {
        String[] validStrategies = {
                SmsConstants.LoadBalanceStrategy.ROUND_ROBIN,
                SmsConstants.LoadBalanceStrategy.RANDOM,
                SmsConstants.LoadBalanceStrategy.WEIGHT_RANDOM,
                SmsConstants.LoadBalanceStrategy.HASH
        };

        boolean isValid = false;
        for (String validStrategy : validStrategies) {
            if (validStrategy.equals(strategy)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new IllegalArgumentException("无效的负载均衡策略: " + strategy);
        }
    }

    /**
     * Business: 验证手机号格式
     */
    private boolean isValidPhone(String phone) {
        return CHINA_MOBILE_PATTERN.matcher(phone).matches() ||
                INTERNATIONAL_PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Business: 检查敏感词
     */
    private boolean containsSensitiveWords(String content) {
        String lowerContent = content.toLowerCase();

        // 简单的敏感词检查
        String[] sensitiveWords = {
                "赌博", "色情", "暴力", "反动", "法轮功",
                "贷款", "投资", "理财", "股票", "期货"
        };

        for (String word : sensitiveWords) {
            if (lowerContent.contains(word)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Business: 脱敏手机号
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }

        if (phone.length() == 11 && phone.startsWith("1")) {
            // 中国手机号
            return phone.substring(0, 3) + "****" + phone.substring(7);
        } else {
            // 国际手机号
            int len = phone.length();
            if (len <= 6) {
                return "***" + phone.substring(len - 3);
            } else {
                return phone.substring(0, 3) + "****" + phone.substring(len - 3);
            }
        }
    }
}

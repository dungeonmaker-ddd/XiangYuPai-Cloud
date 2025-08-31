package com.xypai.sms.service.util;

import com.xypai.sms.common.constant.SmsConstants;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Util: 数据验证工具类
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
public final class ValidationUtil {

    private static final Pattern CHINA_MOBILE_PATTERN = Pattern.compile(SmsConstants.Regex.CHINA_MOBILE);
    private static final Pattern INTERNATIONAL_PHONE_PATTERN = Pattern.compile(SmsConstants.Regex.INTERNATIONAL_PHONE);
    private static final Pattern TEMPLATE_CODE_PATTERN = Pattern.compile(SmsConstants.Regex.TEMPLATE_CODE);
    private static final Pattern TEMPLATE_VARIABLE_PATTERN = Pattern.compile(SmsConstants.Regex.TEMPLATE_VARIABLE);
    private ValidationUtil() {
    }

    /**
     * Util: 验证中国手机号
     */
    public static boolean isChinaMobile(String phone) {
        return phone != null && CHINA_MOBILE_PATTERN.matcher(phone).matches();
    }

    /**
     * Util: 验证国际手机号
     */
    public static boolean isInternationalPhone(String phone) {
        return phone != null && INTERNATIONAL_PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Util: 验证手机号（中国或国际）
     */
    public static boolean isValidPhone(String phone) {
        return isChinaMobile(phone) || isInternationalPhone(phone);
    }

    /**
     * Util: 验证模板代码
     */
    public static boolean isValidTemplateCode(String templateCode) {
        return templateCode != null && TEMPLATE_CODE_PATTERN.matcher(templateCode).matches();
    }

    /**
     * Util: 验证邮箱格式
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return Pattern.matches(emailRegex, email.trim());
    }

    /**
     * Util: 验证字符串长度
     */
    public static boolean isValidLength(String str, int maxLength) {
        return str != null && str.length() <= maxLength;
    }

    /**
     * Util: 验证字符串长度范围
     */
    public static boolean isValidLength(String str, int minLength, int maxLength) {
        return str != null && str.length() >= minLength && str.length() <= maxLength;
    }

    /**
     * Util: 验证不为空
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Util: 验证对象不为空
     */
    public static boolean isNotNull(Object obj) {
        return obj != null;
    }

    /**
     * Util: 验证数字范围
     */
    public static boolean isInRange(Integer number, int min, int max) {
        return number != null && number >= min && number <= max;
    }

    /**
     * Util: 验证正数
     */
    public static boolean isPositive(Integer number) {
        return number != null && number > 0;
    }

    /**
     * Util: 验证非负数
     */
    public static boolean isNonNegative(Integer number) {
        return number != null && number >= 0;
    }

    /**
     * Util: 验证模板变量格式
     */
    public static boolean containsValidVariables(String template) {
        if (template == null) {
            return false;
        }

        return TEMPLATE_VARIABLE_PATTERN.matcher(template).find();
    }

    /**
     * Util: 清理字符串
     */
    public static String cleanString(String str) {
        return str != null ? str.trim() : null;
    }

    /**
     * Util: 规范化手机号
     */
    public static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }

        // 移除所有非数字字符（除了+号）
        String cleaned = phone.replaceAll("[^0-9+]", "");

        // 如果是中国手机号，确保以1开头
        if (cleaned.length() == 11 && cleaned.startsWith("1")) {
            return cleaned;
        }

        // 如果是国际手机号，确保以+开头
        if (!cleaned.startsWith("+") && cleaned.length() > 11) {
            cleaned = "+" + cleaned;
        }

        return cleaned;
    }

    /**
     * Util: 脱敏手机号
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }

        if (isChinaMobile(phone)) {
            return phone.substring(0, 3) + "****" + phone.substring(7);
        } else {
            int len = phone.length();
            if (len <= 6) {
                return "***" + phone.substring(len - 3);
            } else {
                return phone.substring(0, 3) + "****" + phone.substring(len - 3);
            }
        }
    }

    /**
     * Util: 脱敏邮箱
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }

        String[] parts = email.split("@");
        if (parts.length != 2) {
            return "***";
        }

        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 2) {
            return "***@" + domain;
        } else {
            return username.substring(0, 2) + "***@" + domain;
        }
    }

    /**
     * Util: 断言不为空
     */
    public static void requireNonNull(Object obj, String message) {
        Objects.requireNonNull(obj, message);
    }

    /**
     * Util: 断言字符串不为空
     */
    public static void requireNonEmpty(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Util: 断言条件为真
     */
    public static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}

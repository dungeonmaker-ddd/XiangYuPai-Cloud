package com.xypai.sms.common.constant;

/**
 * Constant: 短信服务常量
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
public final class SmsConstants {

    private SmsConstants() {
    }

    /**
     * Constant: 默认配置
     */
    public static final class Default {
        public static final String LOAD_BALANCE_STRATEGY = "ROUND_ROBIN";
        public static final Integer BATCH_MAX_SIZE = 100;
        public static final Integer RATE_LIMIT = 60;
        public static final Integer TEMPLATE_TTL = 3600;
        public static final Integer SEND_RECORD_TTL = 86400;
    }

    /**
     * Constant: 状态常量
     */
    public static final class Status {
        public static final String PENDING = "PENDING";
        public static final String PROCESSING = "PROCESSING";
        public static final String SUCCESS = "SUCCESS";
        public static final String FAILED = "FAILED";
        public static final String PARTIAL_SUCCESS = "PARTIAL_SUCCESS";
        public static final String TIMEOUT = "TIMEOUT";
    }

    /**
     * Constant: 模板状态
     */
    public static final class TemplateStatus {
        public static final String PENDING_APPROVAL = "PENDING_APPROVAL";
        public static final String ACTIVE = "ACTIVE";
        public static final String INACTIVE = "INACTIVE";
        public static final String REJECTED = "REJECTED";
    }

    /**
     * Constant: 模板类型
     */
    public static final class TemplateType {
        public static final String NOTIFICATION = "NOTIFICATION";
        public static final String VERIFICATION = "VERIFICATION";
        public static final String MARKETING = "MARKETING";
        public static final String SYSTEM = "SYSTEM";
    }

    /**
     * Constant: 负载均衡策略
     */
    public static final class LoadBalanceStrategy {
        public static final String ROUND_ROBIN = "ROUND_ROBIN";
        public static final String RANDOM = "RANDOM";
        public static final String WEIGHT_RANDOM = "WEIGHT_RANDOM";
        public static final String HASH = "HASH";
    }

    /**
     * Constant: 缓存键前缀
     */
    public static final class CacheKey {
        private static final String PREFIX = "sms:";
        public static final String TEMPLATE = PREFIX + "template:";
        public static final String SEND_RECORD = PREFIX + "send:";
        public static final String BLACKLIST = PREFIX + "blacklist:";
        public static final String RATE_LIMIT = PREFIX + "rate:";
        public static final String CHANNEL_STATUS = PREFIX + "channel:";
    }

    /**
     * Constant: 消息队列
     */
    public static final class Queue {
        public static final String SMS_SEND = "sms.send.queue";
        public static final String SMS_CALLBACK = "sms.callback.queue";
        public static final String SMS_DEAD_LETTER = "sms.send.dlq";
    }

    /**
     * Constant: 正则表达式
     */
    public static final class Regex {
        public static final String CHINA_MOBILE = "^1[3-9]\\d{9}$";
        public static final String INTERNATIONAL_PHONE = "^\\+?[1-9]\\d{6,14}$";
        public static final String TEMPLATE_CODE = "^[A-Z][A-Z0-9_]{2,49}$";
        public static final String TEMPLATE_VARIABLE = "\\{([^}]+)\\}";
    }

    /**
     * Constant: 限制配置
     */
    public static final class Limit {
        public static final int MAX_PHONE_COUNT = 100;
        public static final int MAX_TEMPLATE_LENGTH = 500;
        public static final int MAX_TEMPLATE_NAME_LENGTH = 100;
        public static final int MAX_TEMPLATE_CODE_LENGTH = 50;
        public static final int MAX_RETRY_ATTEMPTS = 3;
        public static final int DEFAULT_TIMEOUT = 30000;
    }
}

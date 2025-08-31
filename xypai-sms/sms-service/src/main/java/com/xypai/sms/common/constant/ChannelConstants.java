package com.xypai.sms.common.constant;

/**
 * Constant: 短信渠道常量
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
public final class ChannelConstants {

    private ChannelConstants() {
    }

    /**
     * Constant: 渠道类型
     */
    public static final class Type {
        public static final String ALIYUN = "ALIYUN";
        public static final String TENCENT = "TENCENT";
        public static final String BAIDU = "BAIDU";
        public static final String HUAWEI = "HUAWEI";
        public static final String JD_CLOUD = "JD_CLOUD";
    }

    /**
     * Constant: 渠道代码
     */
    public static final class Code {
        public static final String ALIYUN = "aliyun";
        public static final String TENCENT = "tencent";
        public static final String BAIDU = "baidu";
        public static final String HUAWEI = "huawei";
        public static final String JD_CLOUD = "jdcloud";
    }

    /**
     * Constant: 渠道状态
     */
    public static final class Status {
        public static final String ACTIVE = "ACTIVE";
        public static final String INACTIVE = "INACTIVE";
        public static final String MAINTENANCE = "MAINTENANCE";
    }

    /**
     * Constant: 健康状态
     */
    public static final class HealthStatus {
        public static final String HEALTHY = "HEALTHY";
        public static final String UNHEALTHY = "UNHEALTHY";
        public static final String UNKNOWN = "UNKNOWN";
    }

    /**
     * Constant: 阿里云配置
     */
    public static final class Aliyun {
        public static final String ENDPOINT = "dysmsapi.aliyuncs.com";
        public static final String REGION = "cn-hangzhou";
        public static final String PRODUCT = "Dysmsapi";
        public static final String DOMAIN = "dysmsapi.aliyuncs.com";
        public static final String VERSION = "2017-05-25";
    }

    /**
     * Constant: 腾讯云配置
     */
    public static final class Tencent {
        public static final String ENDPOINT = "sms.tencentcloudapi.com";
        public static final String SERVICE = "sms";
        public static final String VERSION = "2021-01-11";
        public static final String ACTION_SEND = "SendSms";
        public static final String ACTION_STATUS = "PullSmsSendStatus";
    }

    /**
     * Constant: 百度云配置
     */
    public static final class Baidu {
        public static final String ENDPOINT = "sms.bj.baidubce.com";
        public static final String REGION = "bj";
        public static final String VERSION = "v1";
        public static final String CONTENT_TYPE = "application/json";
    }

    /**
     * Constant: 错误代码
     */
    public static final class ErrorCode {
        public static final String SUCCESS = "OK";
        public static final String INVALID_PARAMS = "INVALID_PARAMS";
        public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
        public static final String INSUFFICIENT_BALANCE = "INSUFFICIENT_BALANCE";
        public static final String TEMPLATE_NOT_FOUND = "TEMPLATE_NOT_FOUND";
        public static final String PHONE_BLACKLIST = "PHONE_BLACKLIST";
        public static final String NETWORK_ERROR = "NETWORK_ERROR";
        public static final String CHANNEL_ERROR = "CHANNEL_ERROR";
    }

    /**
     * Constant: 优先级
     */
    public static final class Priority {
        public static final int HIGH = 1;
        public static final int MEDIUM = 2;
        public static final int LOW = 3;
        public static final int BACKUP = 4;
    }
}

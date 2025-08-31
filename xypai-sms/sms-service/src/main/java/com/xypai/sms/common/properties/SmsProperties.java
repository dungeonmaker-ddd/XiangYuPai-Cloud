package com.xypai.sms.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Properties: SMS配置属性
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Data
@Component
@ConfigurationProperties(prefix = "xypai.sms")
public class SmsProperties {

    /**
     * Properties: 发送配置
     */
    private SendConfig send = new SendConfig();

    /**
     * Properties: 渠道配置
     */
    private Map<String, ChannelConfig> channels;

    /**
     * Properties: 缓存配置
     */
    private CacheConfig cache = new CacheConfig();

    /**
     * Properties: 消息队列配置
     */
    private MqConfig mq = new MqConfig();

    @Data
    public static class SendConfig {
        private String defaultLoadBalance = "ROUND_ROBIN";
        private Integer batchMaxSize = 100;
        private Integer rateLimit = 60;
        private Integer asyncQueueSize = 1000;
    }

    @Data
    public static class ChannelConfig {
        private Boolean enabled = true;
        private Integer priority = 1;
        private String accessKeyId;
        private String accessKeySecret;
        private String secretId;
        private String secretKey;
        private String endpoint;
        private String region;
        private String sdkAppId;
    }

    @Data
    public static class CacheConfig {
        private Long templateTtl = 3600L;
        private Long sendRecordTtl = 86400L;
        private Long blacklistTtl = 1800L;
    }

    @Data
    public static class MqConfig {
        private String sendQueue = "sms.send.queue";
        private String deadLetterQueue = "sms.send.dlq";
        private Integer consumerConcurrency = 5;
        private Integer maxRetryAttempts = 3;
    }
}

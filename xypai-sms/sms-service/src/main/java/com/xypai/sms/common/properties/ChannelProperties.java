package com.xypai.sms.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Properties: 短信渠道配置属性
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Data
@Component
@ConfigurationProperties(prefix = "xypai.sms.channels")
public class ChannelProperties {

    /**
     * Properties: 阿里云配置
     */
    private AliyunConfig aliyun = new AliyunConfig();

    /**
     * Properties: 腾讯云配置
     */
    private TencentConfig tencent = new TencentConfig();

    /**
     * Properties: 百度云配置
     */
    private BaiduConfig baidu = new BaiduConfig();

    @Data
    public static class AliyunConfig {
        private Boolean enabled = true;
        private Integer priority = 1;
        private String accessKeyId;
        private String accessKeySecret;
        private String endpoint = "dysmsapi.aliyuncs.com";
        private String signName;
        private Map<String, String> templateIds;
    }

    @Data
    public static class TencentConfig {
        private Boolean enabled = true;
        private Integer priority = 2;
        private String secretId;
        private String secretKey;
        private String region = "ap-guangzhou";
        private String sdkAppId;
        private String signName;
        private Map<String, String> templateIds;
    }

    @Data
    public static class BaiduConfig {
        private Boolean enabled = false;
        private Integer priority = 3;
        private String accessKeyId;
        private String secretAccessKey;
        private String endpoint = "sms.bj.baidubce.com";
        private String invokeId;
        private Map<String, String> templateIds;
    }
}

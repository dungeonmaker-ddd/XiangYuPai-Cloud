package com.xypai.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Token配置类
 * <p>
 * 管理不同客户端的token过期时间配置
 *
 * @author xypai
 */
@Component
@ConfigurationProperties(prefix = "auth.token")
public class TokenConfig {

    /**
     * Web端token过期时间（秒）- 2小时
     */
    private Long webExpireTime = 7200L;

    /**
     * App端token过期时间（秒）- 24小时
     */
    private Long appExpireTime = 86400L;

    /**
     * 小程序token过期时间（秒）- 24小时
     */
    private Long miniExpireTime = 86400L;

    /**
     * 根据客户端类型获取token过期时间
     */
    public Long getExpireTime(String clientType) {
        return switch (clientType) {
            case "web" -> webExpireTime;
            case "app" -> appExpireTime;
            case "mini" -> miniExpireTime;
            default -> webExpireTime;
        };
    }

    // Getters and Setters
    public Long getWebExpireTime() {
        return webExpireTime;
    }

    public void setWebExpireTime(Long webExpireTime) {
        this.webExpireTime = webExpireTime;
    }

    public Long getAppExpireTime() {
        return appExpireTime;
    }

    public void setAppExpireTime(Long appExpireTime) {
        this.appExpireTime = appExpireTime;
    }

    public Long getMiniExpireTime() {
        return miniExpireTime;
    }

    public void setMiniExpireTime(Long miniExpireTime) {
        this.miniExpireTime = miniExpireTime;
    }
}

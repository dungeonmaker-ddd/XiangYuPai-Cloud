package com.xypai.security.oauth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 🔑 Token配置属性类
 * <p>
 * XV02:03 继承并简化原有TokenConfig功能
 * 管理不同客户端的token过期时间配置
 *
 * @author xypai
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "auth.token")
public class TokenProperties {
    
    /**
     * 客户端类型常量
     */
    public static final String CLIENT_TYPE_WEB = "web";
    public static final String CLIENT_TYPE_APP = "app";
    public static final String CLIENT_TYPE_MINI = "mini";
    
    /**
     * 默认过期时间常量（秒）
     */
    private static final Long DEFAULT_WEB_EXPIRE_TIME = 7200L;    // 2小时
    private static final Long DEFAULT_APP_EXPIRE_TIME = 86400L;   // 24小时
    private static final Long DEFAULT_MINI_EXPIRE_TIME = 86400L;  // 24小时
    
    /**
     * Web端token过期时间（秒）
     */
    private Long webExpireTime = DEFAULT_WEB_EXPIRE_TIME;
    
    /**
     * App端token过期时间（秒）
     */
    private Long appExpireTime = DEFAULT_APP_EXPIRE_TIME;
    
    /**
     * 小程序token过期时间（秒）
     */
    private Long miniExpireTime = DEFAULT_MINI_EXPIRE_TIME;
    
    /**
     * 根据客户端类型获取token过期时间
     *
     * @param clientType 客户端类型 (web/app/mini)
     * @return token过期时间（秒）
     */
    public Long getExpireTime(String clientType) {
        Objects.requireNonNull(clientType, "客户端类型不能为空");
        
        return switch (clientType.toLowerCase().trim()) {
            case CLIENT_TYPE_WEB -> webExpireTime;
            case CLIENT_TYPE_APP -> appExpireTime;
            case CLIENT_TYPE_MINI -> miniExpireTime;
            default -> webExpireTime; // 默认使用Web端时间
        };
    }
    
    /**
     * 验证过期时间是否有效
     */
    private boolean isValidExpireTime(Long expireTime) {
        return expireTime != null && expireTime > 0 && expireTime <= 604800L; // 最大7天
    }
    
    // Getters and Setters
    
    public Long getWebExpireTime() {
        return webExpireTime;
    }
    
    public void setWebExpireTime(Long webExpireTime) {
        if (!isValidExpireTime(webExpireTime)) {
            throw new IllegalArgumentException("Web端过期时间必须在1秒到7天之间");
        }
        this.webExpireTime = webExpireTime;
    }
    
    public Long getAppExpireTime() {
        return appExpireTime;
    }
    
    public void setAppExpireTime(Long appExpireTime) {
        if (!isValidExpireTime(appExpireTime)) {
            throw new IllegalArgumentException("App端过期时间必须在1秒到7天之间");
        }
        this.appExpireTime = appExpireTime;
    }
    
    public Long getMiniExpireTime() {
        return miniExpireTime;
    }
    
    public void setMiniExpireTime(Long miniExpireTime) {
        if (!isValidExpireTime(miniExpireTime)) {
            throw new IllegalArgumentException("小程序端过期时间必须在1秒到7天之间");
        }
        this.miniExpireTime = miniExpireTime;
    }
    
    @Override
    public String toString() {
        return String.format("TokenProperties{web=%ds, app=%ds, mini=%ds}",
                webExpireTime, appExpireTime, miniExpireTime);
    }
}

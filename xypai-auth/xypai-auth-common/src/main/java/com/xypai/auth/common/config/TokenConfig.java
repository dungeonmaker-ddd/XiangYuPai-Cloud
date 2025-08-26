package com.xypai.auth.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 🔑 Token配置类
 * <p>
 * 管理不同客户端的token过期时间配置
 * 支持Web端、App端、小程序端的差异化过期时间设置
 *
 * @author xypai
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "auth.token")
public class TokenConfig {

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
     * Web端token过期时间（秒）- 默认2小时
     */
    private Long webExpireTime = DEFAULT_WEB_EXPIRE_TIME;

    /**
     * App端token过期时间（秒）- 默认24小时
     */
    private Long appExpireTime = DEFAULT_APP_EXPIRE_TIME;

    /**
     * 小程序token过期时间（秒）- 默认24小时
     */
    private Long miniExpireTime = DEFAULT_MINI_EXPIRE_TIME;

    /**
     * 根据客户端类型获取token过期时间
     *
     * @param clientType 客户端类型 (web/app/mini)
     * @return token过期时间（秒），如果类型未知则返回web端默认时间
     * @throws IllegalArgumentException 当clientType为null时抛出
     */
    public Long getExpireTime(String clientType) {
        Objects.requireNonNull(clientType, "客户端类型不能为空");

        return switch (clientType.toLowerCase().trim()) {
            case CLIENT_TYPE_WEB -> webExpireTime;
            case CLIENT_TYPE_APP -> appExpireTime;
            case CLIENT_TYPE_MINI -> miniExpireTime;
            default -> {
                // 记录未知客户端类型的警告日志
                System.err.println("警告: 未知的客户端类型 '" + clientType + "'，使用默认Web端过期时间");
                yield webExpireTime;
            }
        };
    }

    /**
     * 验证过期时间是否有效
     *
     * @param expireTime 过期时间（秒）
     * @return 是否有效
     */
    private boolean isValidExpireTime(Long expireTime) {
        return expireTime != null && expireTime > 0 && expireTime <= 604800L; // 最大7天
    }

    // Getters and Setters with validation

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

    /**
     * 获取配置摘要信息
     *
     * @return 配置摘要字符串
     */
    @Override
    public String toString() {
        return String.format("TokenConfig{web=%ds, app=%ds, mini=%ds}",
                webExpireTime, appExpireTime, miniExpireTime);
    }
}

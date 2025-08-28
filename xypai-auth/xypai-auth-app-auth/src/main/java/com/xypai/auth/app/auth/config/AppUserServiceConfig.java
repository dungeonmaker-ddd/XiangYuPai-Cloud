package com.xypai.auth.app.auth.config;

import com.xypai.auth.app.auth.service.RemoteAppUserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * APP用户服务配置
 *
 * @author XyPai
 */
@Configuration
public class AppUserServiceConfig {

    /**
     * 如果RemoteAppUserService Bean创建失败，提供一个备用的实现
     */
    @Bean
    @ConditionalOnMissingBean(RemoteAppUserService.class)
    public RemoteAppUserService fallbackAppUserService() {
        return new com.xypai.auth.app.auth.service.RemoteAppUserFallback();
    }
}

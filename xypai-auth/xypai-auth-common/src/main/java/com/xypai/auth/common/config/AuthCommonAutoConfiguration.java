package com.xypai.auth.common.config;

import com.xypai.auth.common.factory.AuthStrategyFactory;
import com.xypai.auth.common.strategy.AuthenticationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.Map;

/**
 * 🔧 认证公共模块自动配置
 * <p>
 * 为使用xypai-auth-common模块的应用提供自动配置支持
 * 自动扫描并注册认证相关的组件，无需手动配置ComponentScan
 *
 * @author xypai
 * @version 4.1.0
 * @since 2024-08-26
 */
@AutoConfiguration
@ConditionalOnClass({AuthStrategyFactory.class, AuthenticationStrategy.class})
@ComponentScan("com.xypai.auth.common")
@EnableConfigurationProperties(TokenConfig.class)
public class AuthCommonAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AuthCommonAutoConfiguration.class);

    /**
     * 自动配置认证策略工厂
     *
     * @param strategies Spring容器中所有的AuthenticationStrategy实现
     * @return 认证策略工厂实例
     */
    @Bean
    @ConditionalOnMissingBean
    public AuthStrategyFactory authStrategyFactory(Map<String, AuthenticationStrategy> strategies) {
        logger.info("🏭 自动配置认证策略工厂 - 发现策略数量: {}", strategies.size());
        return new AuthStrategyFactory(strategies);
    }
}

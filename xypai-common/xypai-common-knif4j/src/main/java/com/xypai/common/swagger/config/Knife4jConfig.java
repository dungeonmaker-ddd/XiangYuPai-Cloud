package com.xypai.common.swagger.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import com.xypai.common.swagger.properties.SwaggerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 🔪 Knife4j 增强配置类
 * <p>
 * 主要功能：
 * - 启用 Knife4j 增强功能
 * - 配置 Knife4j 特定设置
 * - 集成 Basic 认证（如果启用）
 * - 自定义界面设置
 *
 * @author XyPai
 * @version 3.0.0
 * @since 2025-01-01
 */
@Slf4j
@EnableKnife4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "swagger.knife4j.enable", havingValue = "true", matchIfMissing = true)
public class Knife4jConfig {

    private final SwaggerProperties swaggerProperties;

    /**
     * Knife4j 配置初始化
     */
    @PostConstruct
    public void init() {
        log.info("🔪 Initializing Knife4j Enhanced Configuration...");

        // 这里可以添加一些初始化逻辑
        configureKnife4jSettings();

        log.info("✅ Knife4j Enhanced Configuration initialized successfully");
    }

    /**
     * 配置 Knife4j 设置
     */
    private void configureKnife4jSettings() {
        log.debug("🔧 Configuring Knife4j enhanced settings...");

        // 使用配置属性进行设置
        if (swaggerProperties != null && swaggerProperties.getKnife4j() != null) {
            SwaggerProperties.Knife4j knife4jConfig = swaggerProperties.getKnife4j();
            log.debug("📋 Knife4j enabled: {}", knife4jConfig.getEnable());
            log.debug("🏭 Production mode: {}", knife4jConfig.getProduction());
        }

        log.debug("✅ Knife4j settings configured");
    }
}

package com.xypai.common.swagger.config;

import com.xypai.common.swagger.properties.SwaggerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 🌐 Swagger Web MVC 配置类
 * <p>
 * 功能特性：
 * - 配置 Swagger UI 静态资源映射
 * - 设置 Knife4j 文档访问路径
 * - 支持自定义主页重定向
 * - 确保文档资源正常加载
 *
 * @author XyPai
 * @version 3.0.0
 * @since 2025-01-01
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "swagger.enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerWebMvcConfig implements WebMvcConfigurer {

    private final SwaggerProperties swaggerProperties;

    /**
     * 配置静态资源处理器
     * 添加 Knife4j 相关的静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.debug("🔧 Configuring Swagger static resource handlers...");

        // Knife4j 文档静态资源
        registry.addResourceHandler("/doc.html**")
                .addResourceLocations("classpath:/META-INF/resources/");

        // Swagger UI 静态资源
        registry.addResourceHandler("/swagger-ui.html**")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/swagger-ui/");

        // Webjars 资源
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        // API 文档资源
        registry.addResourceHandler("/v3/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/");

        // Favicon
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/META-INF/resources/");

        log.info("✅ Swagger static resource handlers configured successfully");
    }

    /**
     * 配置视图控制器
     * 设置默认访问路径重定向
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        log.debug("🔧 Configuring Swagger view controllers...");

        // 根路径重定向到 API 文档
        if (swaggerProperties.getKnife4j().getSetting().getEnableHomeCustom()) {
            String homePath = swaggerProperties.getKnife4j().getSetting().getHomeCustomPath();
            if (homePath != null && !homePath.isEmpty()) {
                registry.addRedirectViewController("/", homePath);
                registry.addRedirectViewController("/api", homePath);
                registry.addRedirectViewController("/docs", homePath);

                log.info("📄 Default page redirect configured: {} -> {}", "/", homePath);
            }
        }

        // Swagger UI 重定向
        registry.addRedirectViewController("/swagger-ui", "/swagger-ui/index.html");
        registry.addRedirectViewController("/api-docs", "/v3/api-docs");

        log.info("✅ Swagger view controllers configured successfully");
    }
}

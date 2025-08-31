package com.xypai.common.swagger.config;

import com.xypai.common.swagger.properties.SwaggerProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 📚 Swagger/Knife4j 自动配置类
 * <p>
 * 遵循企业架构规范：
 * - 统一API文档风格
 * - 支持多模块分组
 * - 集成JWT认证
 * - 生产环境安全控制
 *
 * @author XyPai
 * @version 3.0.0
 * @since 2025-01-01
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SwaggerProperties.class)
@ConditionalOnProperty(name = "swagger.enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerAutoConfiguration {

    private final SwaggerProperties swaggerProperties;

    /**
     * 创建 OpenAPI 文档信息
     */
    @Bean
    public OpenAPI createOpenApi() {
        log.info("🚀 Initializing Swagger OpenAPI Documentation...");

        OpenAPI openAPI = new OpenAPI()
                .info(createApiInfo())
                .components(createComponents());

        // 添加全局安全认证
        if (isAuthorizationEnabled()) {
            openAPI.addSecurityItem(createSecurityRequirement());
        }

        log.info("✅ Swagger OpenAPI Documentation initialized successfully");
        return openAPI;
    }

    /**
     * 创建默认分组
     */
    @Bean
    public GroupedOpenApi defaultGroup() {
        String groupName = "01-默认接口";

        GroupedOpenApi.Builder builder = GroupedOpenApi.builder()
                .group(groupName)
                .displayName(groupName);

        // 配置扫描路径
        if (!CollectionUtils.isEmpty(swaggerProperties.getBasePackages())) {
            builder.packagesToScan(swaggerProperties.getBasePackages().toArray(new String[0]));
        } else {
            builder.packagesToScan("com.xypai");
        }

        // 配置路径匹配
        builder.pathsToMatch("/**");

        // 排除路径
        if (!CollectionUtils.isEmpty(swaggerProperties.getExcludePaths())) {
            swaggerProperties.getExcludePaths().forEach(builder::pathsToExclude);
        }

        log.info("📋 Created default API group: {}", groupName);
        return builder.build();
    }

    /**
     * 创建自定义分组
     */
    @Bean
    public List<GroupedOpenApi> customGroups() {
        List<GroupedOpenApi> groups = new ArrayList<>();

        if (!CollectionUtils.isEmpty(swaggerProperties.getGroups())) {
            for (SwaggerProperties.GroupInfo groupInfo : swaggerProperties.getGroups()) {
                if (StringUtils.hasText(groupInfo.getName()) &&
                        StringUtils.hasText(groupInfo.getBasePackage())) {

                    GroupedOpenApi.Builder builder = GroupedOpenApi.builder()
                            .group(groupInfo.getName())
                            .displayName(groupInfo.getName())
                            .packagesToScan(groupInfo.getBasePackage())
                            .pathsToMatch(groupInfo.getPathsToMatch());

                    // 排除路径
                    if (!CollectionUtils.isEmpty(groupInfo.getExcludePaths())) {
                        groupInfo.getExcludePaths().forEach(builder::pathsToExclude);
                    }

                    GroupedOpenApi group = builder.build();
                    groups.add(group);

                    log.info("📋 Created custom API group: {} -> {}",
                            groupInfo.getName(), groupInfo.getBasePackage());
                }
            }
        }

        return groups;
    }

    /**
     * 创建 API 信息
     */
    private Info createApiInfo() {
        Contact contact = new Contact()
                .name(swaggerProperties.getContact().getName())
                .email(swaggerProperties.getContact().getEmail())
                .url(swaggerProperties.getContact().getUrl());

        License license = new License()
                .name(swaggerProperties.getLicense())
                .url(swaggerProperties.getLicenseUrl());

        return new Info()
                .title(swaggerProperties.getTitle())
                .description(swaggerProperties.getDescription())
                .version(swaggerProperties.getVersion())
                .termsOfService(swaggerProperties.getTermsOfServiceUrl())
                .contact(contact)
                .license(license);
    }

    /**
     * 创建组件配置（认证等）
     */
    private Components createComponents() {
        Components components = new Components();

        if (isAuthorizationEnabled()) {
            SwaggerProperties.Authorization auth = swaggerProperties.getAuthorization();

            SecurityScheme securityScheme = new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description(auth.getDescription());

            components.addSecuritySchemes(auth.getName(), securityScheme);
        }

        return components;
    }

    /**
     * 创建安全要求
     */
    private SecurityRequirement createSecurityRequirement() {
        String authName = swaggerProperties.getAuthorization().getName();
        return new SecurityRequirement().addList(authName);
    }

    /**
     * 检查是否启用认证
     */
    private boolean isAuthorizationEnabled() {
        SwaggerProperties.Authorization auth = swaggerProperties.getAuthorization();
        return auth != null && StringUtils.hasText(auth.getName());
    }
}

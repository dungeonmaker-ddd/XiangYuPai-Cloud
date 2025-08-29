package com.xypai.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 📚 Knife4j API 文档配置类
 * <p>
 * US03:01 用户服务API文档配置
 * 使用 Knife4j 提供增强的 Swagger UI
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("👤 XY相遇派用户服务 API")
                        .description("""
                                ## 用户服务 - MVP版本
                                
                                ### 🚀 核心功能
                                - 👤 用户创建与查询
                                - 📋 用户列表管理  
                                - ✅ 用户状态控制
                                - 🔍 用户信息验证
                                
                                ### 🛠️ 技术栈
                                - **框架**: Spring Boot 3.x
                                - **数据库**: MyBatis-Plus + MySQL 8.0
                                - **服务发现**: Nacos
                                - **API文档**: Knife4j
                                
                                ### 📡 服务信息
                                - **端口**: 9106
                                - **健康检查**: `/users/health`
                                - **API文档**: `/doc.html`
                                """)
                        .version("1.0.0-MVP")
                        .contact(new Contact()
                                .name("XY相遇派开发团队")
                                .email("dev@xypai.com")
                                .url("https://xypai.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }

    /**
     * 🔗 用户管理 API 分组
     */
    @Bean
    public GroupedOpenApi userManagementApi() {
        return GroupedOpenApi.builder()
                .group("👤 用户管理")
                .pathsToMatch("/users/**")
                .build();
    }

    /**
     * 🔗 健康检查 API 分组
     */
    @Bean
    public GroupedOpenApi healthApi() {
        return GroupedOpenApi.builder()
                .group("🚀 系统监控")
                .pathsToMatch("/users/health", "/actuator/**")
                .build();
    }
}

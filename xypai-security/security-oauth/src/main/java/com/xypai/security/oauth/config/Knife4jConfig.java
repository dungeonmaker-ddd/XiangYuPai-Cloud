package com.xypai.security.oauth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 🔐 OAuth2认证服务 Knife4j 配置
 * <p>
 * XV02:10 认证服务API文档配置
 * 使用 Knife4j 提供增强的接口文档
 *
 * @author xypai
 * @since 1.0.0
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI authServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🔐 XY相遇派认证服务 API")
                        .description("""
                                ## OAuth2 认证服务 - MVP版本
                                
                                ### 🚀 核心功能
                                - 🔐 用户登录认证
                                - 🔄 令牌刷新机制
                                - ✅ 令牌验证服务
                                - 👤 用户信息获取
                                - 🚪 安全登出功能
                                
                                ### 🛠️ 技术栈
                                - **框架**: Spring Boot 3.x + Spring Security
                                - **认证**: OAuth2 + JWT
                                - **服务发现**: Nacos
                                - **API文档**: Knife4j
                                
                                ### 🔑 认证方式
                                - **密码认证**: username + password
                                - **短信认证**: mobile + sms_code (预留)
                                - **微信认证**: openid + wechat_code (预留)
                                
                                ### 📡 服务信息
                                - **端口**: 9401
                                - **健康检查**: `/auth/health`
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
     * 🔐 认证管理 API 分组
     */
    @Bean
    public GroupedOpenApi authManagementApi() {
        return GroupedOpenApi.builder()
                .group("🔐 认证管理")
                .pathsToMatch("/auth/**")
                .build();
    }

    /**
     * 🚀 系统监控 API 分组
     */
    @Bean
    public GroupedOpenApi authHealthApi() {
        return GroupedOpenApi.builder()
                .group("🚀 系统监控")
                .pathsToMatch("/auth/health", "/actuator/**")
                .build();
    }
}

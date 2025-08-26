package com.xypai.common.swagger.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import com.xypai.common.swagger.config.properties.SpringDocProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Knife4j 简化配置
 *
 * @author xypai
 */
@EnableKnife4j
@AutoConfiguration
@EnableConfigurationProperties(SpringDocProperties.class)
@ConditionalOnProperty(name = "knife4j.enable", havingValue = "true", matchIfMissing = true)
public class Knife4jAutoConfiguration {

    /**
     * 🚀 智能化 OpenAPI 配置
     * 基于应用名称自动生成适配的API文档
     * 只在没有自定义 OpenAPI Bean 时启用
     */
    @Bean
    @ConditionalOnMissingBean(OpenAPI.class)
    public OpenAPI knife4jOpenAPI(SpringDocProperties properties,
                                  @Value("${spring.application.name:unknown-service}") String serviceName,
                                  @Value("${server.port:8080}") String serverPort,
                                  @Value("${gateway.host:localhost:8080}") String gatewayHost) {
        SpringDocProperties.InfoProperties infoProps = properties.getInfo();

        // 🎯 根据服务名称智能生成标题和描述
        ServiceInfo serviceInfo = getServiceInfo(serviceName);

        return new OpenAPI()
                .info(new Info()
                        .title(infoProps.getTitle() != null ? infoProps.getTitle() : serviceInfo.title)
                        .description(serviceInfo.description)
                        .version(infoProps.getVersion() != null ? infoProps.getVersion() : "4.0.0")
                        .contact(new io.swagger.v3.oas.models.info.Contact()
                                .name("XyPai开发团队")
                                .email("dev@xypai.com")
                                .url("https://www.xypai.com"))
                        .license(new io.swagger.v3.oas.models.info.License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                )
                .components(new Components()
                        // JWT 认证配置
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("请输入JWT Token")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .servers(List.of(
                        // 🚀 网关优先 - Swagger UI会默认使用第一个服务器
                        new io.swagger.v3.oas.models.servers.Server()
                                .url("http://" + gatewayHost + serviceInfo.gatewayPath)
                                .description("🚀 网关访问地址 (推荐)"),
                        // 🛠️ 直连服务 - 仅用于开发调试
                        new io.swagger.v3.oas.models.servers.Server()
                                .url("http://localhost:" + serverPort)
                                .description("🛠️ 直连服务 (开发环境)")
                ));
    }

    /**
     * 🎯 根据服务名称获取服务信息
     */
    private ServiceInfo getServiceInfo(String serviceName) {
        return switch (serviceName.toLowerCase()) {
            case "xypai-auth-admin" -> new ServiceInfo(
                    "🏛️ XyPai 管理端服务API",
                    "## 🎯 后台管理系统专用服务\n\n" +
                            "### 🌟 核心功能\n" +
                            "- **管理员认证**：严格的管理员登录和权限验证\n" +
                            "- **用户管理**：系统用户的增删改查和权限分配\n" +
                            "- **权限管理**：角色权限配置和访问控制\n" +
                            "- **系统配置**：平台参数配置和功能开关\n" +
                            "- **审计日志**：操作记录追踪和安全审计\n" +
                            "- **数据统计**：系统运行状态和业务数据统计",
                    "/api/admin"
            );
            case "xypai-auth-app-auth" -> new ServiceInfo(
                    "🎯 XyPai APP认证服务API",
                    "## 📱 移动端专用认证服务\n\n" +
                            "### 🌟 核心功能\n" +
                            "- **智能认证路由**：基于clientType自动选择认证策略\n" +
                            "- **多种登录方式**：用户名密码、手机号验证码\n" +
                            "- **安全Token管理**：JWT令牌生成、刷新、验证\n" +
                            "- **会话管理**：用户登录状态管理和安全退出",
                    "/api/app/auth"
            );
            case "xypai-auth-app" -> new ServiceInfo(
                    "📱 XyPai APP业务服务API",
                    "## 🎯 移动端专用业务服务\n\n" +
                            "### 🌟 核心功能\n" +
                            "- **个人资料管理**：用户信息查看和编辑\n" +
                            "- **设备管理**：用户设备绑定和管理\n" +
                            "- **推送设置**：消息推送偏好配置\n" +
                            "- **隐私设置**：用户隐私和安全配置\n" +
                            "- **应用配置**：APP功能开关和参数设置",
                    "/api/app/business"
            );
            default -> new ServiceInfo(
                    "🚀 XyPai 微服务API",
                    "## ⚡ XiangYuPai 云平台微服务\n\n" +
                            "### 🎯 自动生成的API文档\n" +
                            "- **服务名称**：" + serviceName + "\n" +
                            "- **文档版本**：4.0.0\n" +
                            "- **生成时间**：" + java.time.LocalDateTime.now(),
                    "/api"
            );
        };
    }

    /**
     * 📋 服务信息记录
     */
    private record ServiceInfo(String title, String description, String gatewayPath) {
    }
}

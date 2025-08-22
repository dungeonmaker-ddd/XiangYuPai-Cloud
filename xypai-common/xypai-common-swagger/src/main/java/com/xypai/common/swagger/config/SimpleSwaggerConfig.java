package com.xypai.common.swagger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 🔧 简化版 Swagger 配置
 * <p>
 * 专为支持 Controller 内注解而设计的极简配置
 * 遵循 YAGNI 原则 - 只提供必需的功能
 *
 * @author xypai
 */
@AutoConfiguration
@ConditionalOnProperty(name = "swagger.enabled", havingValue = "true", matchIfMissing = true)
public class SimpleSwaggerConfig {

    /**
     * 🚀 创建基础的 OpenAPI 配置
     * 支持 Controller 中的所有标准注解：
     * - @Tag: 控制器标签
     * - @Operation: 接口描述
     * - @Parameter: 参数描述
     * - @ApiResponse: 响应描述
     */
    @Bean
    public OpenAPI createOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("📱 XiangYuPai API")
                        .description("基于 Controller 注解的 API 文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("XiangYuPai Team")
                                .email("support@xypai.com")
                        )
                );
    }
}

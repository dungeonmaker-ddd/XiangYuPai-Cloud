package com.xypai.sms.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Config: Swagger文档配置
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Configuration
public class SwaggerConfig {

    /**
     * Config: OpenAPI配置
     */
    @Bean
    public OpenAPI smsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("XyPai SMS微服务API")
                        .description("提供短信发送、模板管理等功能的微服务API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("XyPai Team")
                                .email("xypai@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}

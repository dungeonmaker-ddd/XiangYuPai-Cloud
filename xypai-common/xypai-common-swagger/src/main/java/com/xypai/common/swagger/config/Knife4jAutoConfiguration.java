package com.xypai.common.swagger.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import com.xypai.common.swagger.config.properties.SpringDocProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Knife4j 简化配置
 *
 * @author xypai
 */
@EnableKnife4j
@AutoConfiguration
@EnableConfigurationProperties(SpringDocProperties.class)
@ConditionalOnProperty(name = "knife4j.enable", havingValue = "true", matchIfMissing = true)
@Import(SpringDocAutoConfiguration.class)
public class Knife4jAutoConfiguration {

    /**
     * 简化的 OpenAPI 配置
     */
    @Bean
    public OpenAPI knife4jOpenAPI(SpringDocProperties properties) {
        SpringDocProperties.InfoProperties infoProps = properties.getInfo();

        return new OpenAPI()
                .info(new Info()
                        .title(infoProps.getTitle() != null ? infoProps.getTitle() : "XiangYuPai Cloud API")
                        .description("XiangYuPai 云平台接口文档")
                        .version(infoProps.getVersion() != null ? infoProps.getVersion() : "3.6.6")
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
                .addSecurityItem(new SecurityRequirement().addList("Bearer"));
    }
}

package com.xypai.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Application: XyPai SMS 微服务启动类
 * <p>
 * 基于业务控制层架构的短信服务
 * - Controller层：REST控制器、Feign客户端、DTO
 * - Common层：配置类、属性、异常处理、常量
 * - Service层：业务服务、数据访问、工具类
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.xypai.sms.controller.feign")
@EnableAsync
@EnableTransactionManagement
@ConfigurationPropertiesScan(basePackages = "com.xypai.sms.common.properties")
public class SmsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmsServiceApplication.class, args);
        System.out.println("🎉 XyPai SMS Service started successfully!");
        System.out.println("📱 Service Name: xypai-sms-service");
        System.out.println("🌐 Service Port: 9107");
        System.out.println("📄 Swagger UI: http://localhost:9107/doc.html");
        System.out.println("💚 Health Check: http://localhost:9107/actuator/health");
        System.out.println("🏗️ Architecture: Controller + Common + Service");
    }
}
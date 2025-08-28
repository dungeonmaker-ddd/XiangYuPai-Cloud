package com.xypai.security.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 🌐 XY相遇派安全Web管理端
 * <p>
 * XV03:01 Web管理端安全服务启动类
 * 提供管理端用户、权限、配置等管理功能
 *
 * @author xypai
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.xypai")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.xypai")
public class SecurityWebApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SecurityWebApplication.class, args);
        System.out.println("""
                
                🌐 XY相遇派安全Web管理端启动成功
                ================================
                🏛️ 管理端业务服务已就绪
                👥 用户管理、权限管理、系统配置
                📊 审计日志、数据统计
                ================================
                """);
    }
}

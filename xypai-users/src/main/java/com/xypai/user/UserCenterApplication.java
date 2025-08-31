package com.xypai.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 🏗️ Users 微服务启动类 - 企业架构实现
 * <p>
 * 遵循企业微服务架构规范：
 * - 启用Feign客户端
 * - 支持服务发现和配置中心
 * - 集成企业级common模块
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@SpringBootApplication
@EnableFeignClients(basePackages = {"com.xypai.system.api", "com.xypai.user"})
public class UserCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCenterApplication.class, args);
        System.out.println("""
                
                🎉 ========================================
                🚀 Users 微服务启动成功！
                🏗️ 企业微服务架构实现
                📋 基于Spring Boot 3.x + MyBatis Plus
                💾 集成Redis缓存 + Nacos配置中心
                🔐 内置安全框架 + 数据权限控制
                📊 支持操作日志 + 敏感数据脱敏
                ========================================= 🎉
                
                """);
    }
}

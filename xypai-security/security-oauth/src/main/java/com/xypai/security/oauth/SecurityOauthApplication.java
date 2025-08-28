package com.xypai.security.oauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 🛡️ XY相遇派安全认证中心
 * <p>
 * XV02:01 OAuth2认证服务启动类
 * 统一处理所有客户端的认证需求：Web、App、小程序
 *
 * @author xypai
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.xypai")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.xypai")
public class SecurityOauthApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SecurityOauthApplication.class, args);
        System.out.println("""
                
                🛡️ XY相遇派安全认证中心启动成功
                ================================
                🔐 OAuth2 认证服务已就绪
                🌐 支持客户端: Web、App、小程序
                🔒 认证方式: 密码、短信、微信
                ================================
                """);
    }
}

package com.xypai.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证微服务启动类
 *
 * @author xypai
 * @date 2025-01-01
 */
@SpringBootApplication(
    scanBasePackages = "com.xypai",
    exclude = {
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
    }
)
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.xypai")
public class XyPaiAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(XyPaiAuthApplication.class, args);
        System.out.println("""
                
                🛡️ XY相遇派认证微服务启动成功
                ================================
                🔐 C端登录认证服务已就绪
                🌐 支持登录方式: 密码、短信验证码
                🔒 JWT令牌管理: 访问令牌、刷新令牌
                ================================
                """);
    }
}

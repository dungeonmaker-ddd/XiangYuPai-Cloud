package com.xypai.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 👤 用户服务启动类 - MVP版本
 * <p>
 * 端口：9106
 * 职责：用户基础信息管理
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
        System.out.println("""
                
                🚀 用户服务启动成功！
                📱 端口：9106
                🔗 健康检查：http://localhost:9106/actuator/health
                📖 API文档：http://localhost:9106/swagger-ui.html
                
                """);
    }
}

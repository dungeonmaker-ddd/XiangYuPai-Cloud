package com.xypai.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 🤝 社交服务启动类 - DDD架构
 * <p>
 * 端口：9107
 * 职责：社交关系管理 (关注/粉丝/好友)
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@SpringBootApplication
@EnableDiscoveryClient
public class SocialServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialServiceApplication.class, args);
        System.out.println("""
                
                🤝 社交服务启动成功！
                📱 端口：9107
                🔗 健康检查：http://localhost:9107/actuator/health
                📖 聚合根：SocialAggregate
                
                """);
    }
}

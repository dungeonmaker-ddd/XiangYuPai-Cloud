package com.xypai.feed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 📱 动态服务启动类 - DDD架构
 * <p>
 * 端口：9109
 * 职责：动态内容管理 (发布/浏览/互动)
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@SpringBootApplication
@EnableDiscoveryClient
public class FeedServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeedServiceApplication.class, args);
        System.out.println("""
                
                📱 动态服务启动成功！
                📱 端口：9109
                🔗 健康检查：http://localhost:9109/actuator/health
                📖 聚合根：FeedAggregate + InteractionAggregate
                
                """);
    }
}

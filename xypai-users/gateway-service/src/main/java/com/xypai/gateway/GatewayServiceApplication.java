package com.xypai.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 🚪 网关服务启动类 - MVP版本
 * <p>
 * 端口：8080
 * 职责：统一路由管理，无/api前缀
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
        System.out.println("""
                
                🚪 网关服务启动成功！
                📱 端口：8080
                🔗 用户服务：http://localhost:8080/users/**
                🔗 社交服务：http://localhost:8080/social/**
                🔗 钱包服务：http://localhost:8080/wallets/**
                🔗 动态服务：http://localhost:8080/feeds/**
                
                """);
    }
}

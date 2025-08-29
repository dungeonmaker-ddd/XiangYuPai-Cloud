package com.xypai.wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 💰 钱包服务启动类 - DDD架构
 * <p>
 * 端口：9108
 * 职责：钱包财务管理 (余额/交易/转账)
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@SpringBootApplication
@EnableDiscoveryClient
public class WalletServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletServiceApplication.class, args);
        System.out.println("""
                
                💰 钱包服务启动成功！
                📱 端口：9108
                🔗 健康检查：http://localhost:9108/actuator/health
                📖 聚合根：WalletAggregate
                
                """);
    }
}

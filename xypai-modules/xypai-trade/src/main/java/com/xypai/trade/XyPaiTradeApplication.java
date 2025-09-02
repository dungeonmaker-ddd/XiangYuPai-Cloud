package com.xypai.trade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import com.xypai.common.security.annotation.EnableCustomConfig;
import com.xypai.common.security.annotation.EnableRyFeignClients;

/**
 * 交易模块
 *
 * @author xypai
 */
@EnableCustomConfig
@EnableRyFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class XyPaiTradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(XyPaiTradeApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  交易模块启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}

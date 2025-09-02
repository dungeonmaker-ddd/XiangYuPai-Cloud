package com.xypai.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import com.xypai.common.security.annotation.EnableCustomConfig;
import com.xypai.common.security.annotation.EnableRyFeignClients;

/**
 * 用户模块
 *
 * @author xypai
 */
@EnableCustomConfig
@EnableRyFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class XyPaiUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(XyPaiUserApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  用户模块启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}

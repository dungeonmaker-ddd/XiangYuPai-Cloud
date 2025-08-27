package com.xypai.user;

import com.xypai.common.security.annotation.EnableCustomConfig;
import com.xypai.common.security.annotation.EnableRyFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户中心启动程序
 *
 * @author XyPai
 */
@EnableCustomConfig
@EnableRyFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class XyPaiUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(XyPaiUserApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  用户中心启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}

package com.xypai.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.xypai.common.security.annotation.EnableCustomConfig;
import com.xypai.common.security.annotation.EnableRyFeignClients;

/**
 * 聊天模块
 *
 * @author xypai
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class XyPaiChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(XyPaiChatApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  聊天模块启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}

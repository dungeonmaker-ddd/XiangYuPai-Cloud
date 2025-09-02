package com.xypai.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.xypai.common.security.annotation.EnableCustomConfig;
import com.xypai.common.security.annotation.EnableRyFeignClients;

/**
 * 内容模块
 *
 * @author xypai
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class XyPaiContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(XyPaiContentApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  内容模块启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}

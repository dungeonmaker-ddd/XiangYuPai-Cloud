package com.xypai.auth.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * APP业务服务中心 - 专为移动端用户提供业务功能服务
 * <p>
 * 🔄 重构说明：
 * - 移除了Feign客户端依赖
 * - 改用RestTemplate调用认证服务
 * - 专注于APP业务功能，认证功能代理给认证服务
 *
 * @author xypai
 * @version 4.1.0
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class XyPaiAuthAppBusinessApplication {
    public static void main(String[] args) {
        SpringApplication.run(XyPaiAuthAppBusinessApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  APP业务服务中心启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                " .-------.       ____     __        \n" +
                " |  _ _   \\      \\   \\   /  /    \n" +
                " | ( ' )  |       \\  _. /  '       \n" +
                " |(_ o _) /        _( )_ .'         \n" +
                " | (_,_).' __  ___(_ o _)'          \n" +
                " |  |\\ \\  |  ||   |(_,_)'         \n" +
                " |  | \\ `'   /|   `-'  /           \n" +
                " |  |  \\    /  \\      /           \n" +
                " ''-'   `'-'    `-..-'              ");
    }
}

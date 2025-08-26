package com.xypai.auth.app.auth;

import com.xypai.common.security.annotation.EnableRyFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * APP认证服务中心 - 专为移动端提供认证功能
 * <p>
 * 🎯 通过xypai-auth-common模块的自动配置，自动注册认证相关组件
 *
 * @author xypai
 * @version 4.0.0
 */
@EnableRyFeignClients
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class XyPaiAuthAppServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(XyPaiAuthAppServiceApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  APP认证服务中心启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
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

package com.xypai.security.oauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 🔐 安全配置类
 * <p>
 * XV02:09 简化的安全配置
 * MVP版本：只配置密码编码器等基础组件
 *
 * @author xypai
 * @since 1.0.0
 */
@Configuration
public class SecurityConfig {
    
    /**
     * 🔒 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

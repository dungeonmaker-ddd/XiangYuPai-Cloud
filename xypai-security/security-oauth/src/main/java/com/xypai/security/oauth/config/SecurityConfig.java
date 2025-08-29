package com.xypai.security.oauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 🔐 安全配置类
 * <p>
 * XV02:09 OAuth2认证服务安全配置
 * 配置CSRF、认证路径、密码编码器等
 *
 * @author xypai
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 🛡️ 安全过滤器链配置
     * 禁用CSRF，允许认证相关接口访问
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF保护（REST API不需要）
                .csrf(csrf -> csrf.disable())

                // 配置请求授权
                .authorizeHttpRequests(auth -> auth
                        // 允许认证相关接口无需认证
                        .requestMatchers(
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/logout",
                                "/auth/verify",
                                "/auth/user-info",
                                "/auth/health",
                                "/actuator/**",
                                "/error",
                                // Swagger/OpenAPI 文档接口
                                "/doc.html",
                                "/doc.html/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/v2/api-docs",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                // 静态资源
                                "/favicon.ico",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )

                // 禁用表单登录（使用REST API）
                .formLogin(form -> form.disable())

                // 禁用HTTP Basic认证
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
    
    /**
     * 🔒 密码编码器
     * 支持多种编码格式：{bcrypt}、{noop}、{pbkdf2}等
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}

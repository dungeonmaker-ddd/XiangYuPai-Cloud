package com.xypai.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 安全配置
 *
 * @author xypai
 * @date 2025-01-01
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护（因为是API服务）
            .csrf(AbstractHttpConfigurer::disable)
            // 配置请求授权
            .authorizeHttpRequests(authz -> authz
                // 认证相关接口允许匿名访问
                .requestMatchers("/api/v1/auth/**").permitAll()
                // 健康检查接口允许匿名访问
                .requestMatchers("/actuator/**").permitAll()
                // API文档接口允许匿名访问
                .requestMatchers("/doc.html", "/webjars/**", "/swagger-resources/**", "/v3/api-docs/**").permitAll()
                // 静态资源允许匿名访问
                .requestMatchers("/favicon.ico", "/error").permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
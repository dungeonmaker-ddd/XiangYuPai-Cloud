package com.xypai.security.oauth.common.config;

import com.xypai.security.oauth.common.properties.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;

/**
 * 🔐 现代化安全配置类
 * <p>
 * XV03:06 COMMON层 - 现代化Spring Security配置
 * 使用Spring Security 6.x的最新特性和最佳实践
 *
 * @author xypai
 * @since 3.0.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthProperties authProperties;

    /**
     * 🛡️ 现代化安全过滤器链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // 现代化CSRF配置 - 使用Lambda DSL
                .csrf(AbstractHttpConfigurer::disable)

                // 现代化CORS配置
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 现代化会话管理
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .maximumSessions(authProperties.security().maxConcurrentSessions())
                        .maxSessionsPreventsLogin(false))

                // 现代化安全头配置
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .contentTypeOptions(contentTypeOptions -> {
                        })
                        .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                                .maxAgeInSeconds(Duration.ofDays(365).toSeconds())
                                .includeSubDomains(true)
                                .preload(true))
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .cacheControl(cacheControl -> {
                        })
                        .crossOriginEmbedderPolicy(crossOriginEmbedderPolicy -> {
                        })
                        .crossOriginOpenerPolicy(crossOriginOpenerPolicy -> {
                        })
                        .crossOriginResourcePolicy(crossOriginResourcePolicy -> {
                        })
                        .permissionsPolicy(permissions -> permissions
                                .policy("camera=(), microphone=(), geolocation=()"))
                )

                // 现代化请求授权配置
                .authorizeHttpRequests(auth -> auth
                        // 公开接口
                        .requestMatchers(getPublicPaths()).permitAll()

                        // 管理员接口
                        .requestMatchers("/auth/admin/**").hasRole("ADMIN")

                        // 用户接口
                        .requestMatchers("/auth/user/**").hasAnyRole("USER", "ADMIN")

                        // 其他请求需要认证
                        .anyRequest().authenticated())

                // 禁用传统登录方式
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 现代化异常处理
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            var errorResponse = """
                                    {
                                        "code": 401,
                                        "message": "未授权访问",
                                        "timestamp": "%s",
                                        "path": "%s"
                                    }
                                    """.formatted(
                                    java.time.Instant.now().toString(),
                                    request.getRequestURI()
                            );
                            response.getWriter().write(errorResponse);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            var errorResponse = """
                                    {
                                        "code": 403,
                                        "message": "权限不足",
                                        "timestamp": "%s",
                                        "path": "%s"
                                    }
                                    """.formatted(
                                    java.time.Instant.now().toString(),
                                    request.getRequestURI()
                            );
                            response.getWriter().write(errorResponse);
                        })
                )
                .build();
    }

    /**
     * 🔒 现代化密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * 🌐 现代化CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var configuration = new CorsConfiguration();

        // 现代化的CORS配置
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(Duration.ofHours(1).toSeconds());

        // 暴露的响应头
        configuration.setExposedHeaders(List.of(
                "Authorization",
                "X-Total-Count",
                "X-Current-Page",
                "X-Page-Size"
        ));

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * 获取公开访问路径
     */
    private String[] getPublicPaths() {
        return new String[]{
                // 认证相关接口
                "/auth/login",
                "/auth/refresh",
                "/auth/logout",
                "/auth/verify",
                "/auth/user-info",
                "/auth/health",
                "/auth/sms/send",
                "/auth/captcha/**",
                "/auth/sms-code/**",

                // 静态资源和文档
                "/favicon.ico",
                "/css/**",
                "/js/**",
                "/images/**",
                "/static/**",
                "/public/**",

                // API文档
                "/doc.html",
                "/doc.html/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/v2/api-docs",
                "/v3/api-docs/**",
                "/webjars/**",

                // 监控和健康检查
                "/actuator/health",
                "/actuator/info",
                "/actuator/prometheus",
                "/error"
        };
    }
}

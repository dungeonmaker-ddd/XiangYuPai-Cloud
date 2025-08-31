package com.xypai.security.oauth.common.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Set;

/**
 * 自定义Duration验证注解
 */
@interface DurationMin {
    long seconds() default 0;

    long minutes() default 0;

    long hours() default 0;

    long days() default 0;

    String message() default "Duration must be at least {value}";

    Class<?>[] groups() default {};

    Class<? extends jakarta.validation.Payload>[] payload() default {};
}

@interface DurationMax {
    long seconds() default Long.MAX_VALUE;

    long minutes() default Long.MAX_VALUE;

    long hours() default Long.MAX_VALUE;

    long days() default Long.MAX_VALUE;

    String message() default "Duration must be at most {value}";

    Class<?>[] groups() default {};

    Class<? extends jakarta.validation.Payload>[] payload() default {};
}

/**
 * 🔐 现代化认证配置属性
 * <p>
 * XV03:07 COMMON层 - 现代化配置属性管理
 * 使用Records + Bean Validation + 类型安全配置
 *
 * @author xypai
 * @since 3.0.0
 */
@ConfigurationProperties(prefix = "auth")
@Validated
public record AuthProperties(

        @Valid
        @NotNull
        TokenConfig token,

        @Valid
        @NotNull
        SecurityConfig security,

        @Valid
        @NotNull
        StorageConfig storage,

        @Valid
        @NotNull
        VerificationConfig verification
) {

    /**
     * 默认构造器 - 提供合理的默认值
     */
    public AuthProperties() {
        this(
                new TokenConfig(),
                new SecurityConfig(),
                new StorageConfig(),
                new VerificationConfig()
        );
    }

    /**
     * 🔑 令牌配置 - 使用现代化的Duration类型
     */
    public record TokenConfig(
            @NotNull
            @DurationMin(seconds = 60)
            @DurationMax(days = 7)
            Duration expireTime,

            @NotNull
            @DurationMin(seconds = 60)
            @DurationMax(days = 7)
            Duration webExpireTime,

            @NotNull
            @DurationMin(seconds = 60)
            @DurationMax(days = 7)
            Duration appExpireTime,

            @NotNull
            @DurationMin(seconds = 60)
            @DurationMax(days = 7)
            Duration miniExpireTime,

            @NotNull
            @DurationMin(hours = 1)
            @DurationMax(days = 30)
            Duration refreshExpireTime,

            @NotNull
            @DurationMin(seconds = 60)
            @DurationMax(hours = 1)
            Duration expiringSoonThreshold,

            @NotNull
            Boolean autoExtend,

            @NotNull
            @DurationMin(minutes = 5)
            @DurationMax(hours = 1)
            Duration autoExtendDuration
    ) {
        public TokenConfig() {
            this(
                    Duration.ofHours(24),      // 默认24小时
                    Duration.ofHours(2),       // Web端2小时
                    Duration.ofHours(24),      // App端24小时
                    Duration.ofHours(24),      // 小程序24小时
                    Duration.ofDays(7),        // 刷新令牌7天
                    Duration.ofMinutes(5),     // 5分钟即将过期
                    false,                     // 不自动延期
                    Duration.ofMinutes(30)     // 自动延期30分钟
            );
        }

        /**
         * 根据客户端类型获取令牌过期时间
         */
        public Duration getExpireTimeByClientType(String clientType) {
            return switch (clientType) {
                case "web" -> webExpireTime;
                case "app" -> appExpireTime;
                case "mini" -> miniExpireTime;
                default -> expireTime;
            };
        }
    }

    /**
     * 🔒 安全配置
     */
    public record SecurityConfig(
            @Min(3) @Max(10)
            Integer maxLoginAttempts,

            @NotNull
            @DurationMin(minutes = 5)
            @DurationMax(days = 1)
            Duration accountLockDuration,

            @Min(6) @Max(20)
            Integer passwordMinLength,

            @Min(20) @Max(128)
            Integer passwordMaxLength,

            @NotNull
            Boolean passwordComplexityCheck,

            @NotNull
            Boolean ipWhitelistEnabled,

            @NotEmpty
            Set<String> ipWhitelist,

            @NotNull
            Boolean deviceBindingEnabled,

            @Min(1) @Max(10)
            Integer maxConcurrentSessions,

            @NotNull
            Boolean rateLimitEnabled,

            @Min(1) @Max(1000)
            Integer rateLimitPerMinute
    ) {
        public SecurityConfig() {
            this(
                    5,                                  // 最大登录失败次数
                    Duration.ofMinutes(30),             // 锁定30分钟
                    6,                                  // 密码最小6位
                    128,                                // 密码最大128位
                    false,                              // 不检查密码复杂度
                    false,                              // 不启用IP白名单
                    Set.of("127.0.0.1", "::1"),        // 默认本地IP
                    false,                              // 不启用设备绑定
                    3,                                  // 最大3个并发会话
                    true,                               // 启用限流
                    100                                 // 每分钟100次请求
            );
        }
    }

    /**
     * 🗄️ 存储配置
     */
    public record StorageConfig(
            @NotBlank
            @Pattern(regexp = "^(memory|redis|database)$")
            String type,

            @Valid
            @NotNull
            RedisConfig redis,

            @Valid
            @NotNull
            DatabaseConfig database,

            @Valid
            @NotNull
            CleanupConfig cleanup
    ) {
        public StorageConfig() {
            this(
                    "memory",
                    new RedisConfig(),
                    new DatabaseConfig(),
                    new CleanupConfig()
            );
        }

        public record RedisConfig(
                @NotBlank
                String keyPrefix,

                @Min(0) @Max(15)
                Integer database,

                @NotNull
                @DurationMin(seconds = 30)
                @DurationMax(minutes = 10)
                Duration timeout
        ) {
            public RedisConfig() {
                this(
                        "auth:",
                        1,
                        Duration.ofSeconds(30)
                );
            }
        }

        public record DatabaseConfig(
                @NotBlank
                String tablePrefix,

                @NotNull
                Boolean shardingEnabled,

                @Min(1) @Max(1000)
                Integer batchSize
        ) {
            public DatabaseConfig() {
                this(
                        "auth_",
                        false,
                        100
                );
            }
        }

        public record CleanupConfig(
                @NotNull
                Boolean enabled,

                @NotNull
                @DurationMin(minutes = 1)
                @DurationMax(hours = 1)
                Duration interval,

                @Min(100) @Max(10000)
                Integer batchSize
        ) {
            public CleanupConfig() {
                this(
                        true,
                        Duration.ofMinutes(5),
                        1000
                );
            }
        }
    }

    /**
     * 📱 验证码配置
     */
    public record VerificationConfig(
            @Valid
            @NotNull
            SmsConfig sms,

            @Valid
            @NotNull
            CaptchaConfig captcha
    ) {
        public VerificationConfig() {
            this(
                    new SmsConfig(),
                    new CaptchaConfig()
            );
        }

        public record SmsConfig(
                @NotNull
                Boolean enabled,

                @Min(4) @Max(8)
                Integer codeLength,

                @NotNull
                @DurationMin(minutes = 1)
                @DurationMax(minutes = 30)
                Duration expireDuration,

                @Min(5) @Max(100)
                Integer dailyLimit,

                @NotNull
                @DurationMin(seconds = 30)
                @DurationMax(minutes = 5)
                Duration sendInterval
        ) {
            public SmsConfig() {
                this(
                        true,
                        6,
                        Duration.ofMinutes(5),
                        10,
                        Duration.ofSeconds(60)
                );
            }
        }

        public record CaptchaConfig(
                @NotNull
                Boolean enabled,

                @Min(4) @Max(8)
                Integer codeLength,

                @NotNull
                @DurationMin(minutes = 1)
                @DurationMax(minutes = 10)
                Duration expireDuration,

                @Min(80) @Max(200)
                Integer width,

                @Min(30) @Max(80)
                Integer height
        ) {
            public CaptchaConfig() {
                this(
                        false,
                        4,
                        Duration.ofMinutes(5),
                        120,
                        40
                );
            }
        }
    }
}

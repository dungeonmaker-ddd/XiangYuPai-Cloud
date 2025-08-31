-- ==========================================
-- 📱 XyPai SMS 微服务数据库初始化脚本
-- 基于DDD架构设计的短信服务数据库
-- ==========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `xypai_sms`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `xypai_sms`;

-- ==========================================
-- 📋 短信模板表
-- ==========================================
DROP TABLE IF EXISTS `sms_template`;
CREATE TABLE `sms_template`
(
    `id`                 BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `template_code`      VARCHAR(50)  NOT NULL UNIQUE COMMENT '模板代码',
    `template_name`      VARCHAR(100) NOT NULL COMMENT '模板名称',
    `template_content`   TEXT         NOT NULL COMMENT '模板内容',
    `parameter_config`   JSON COMMENT '参数配置',
    `template_type`      VARCHAR(20)  NOT NULL COMMENT '模板类型：NOTIFICATION,VERIFICATION,MARKETING,SYSTEM',
    `supported_channels` JSON         NOT NULL COMMENT '支持的渠道',
    `status`             VARCHAR(20)  NOT NULL DEFAULT 'PENDING_APPROVAL' COMMENT '状态：PENDING_APPROVAL,ACTIVE,INACTIVE,REJECTED',
    `audit_comment`      VARCHAR(500) COMMENT '审核意见',
    `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version`            INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `deleted`            TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',

    INDEX `idx_template_code` (`template_code`),
    INDEX `idx_template_type` (`template_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='短信模板表';

-- ==========================================
-- 📤 短信发送记录表
-- ==========================================
DROP TABLE IF EXISTS `sms_send_record`;
CREATE TABLE `sms_send_record`
(
    `id`                 BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `task_id`            VARCHAR(50) NOT NULL UNIQUE COMMENT '发送任务ID',
    `request_id`         VARCHAR(50) COMMENT '请求ID',
    `template_code`      VARCHAR(50) NOT NULL COMMENT '模板代码',
    `phone_number`       VARCHAR(20) NOT NULL COMMENT '手机号',
    `content`            TEXT        NOT NULL COMMENT '实际发送内容',
    `channel_type`       VARCHAR(20) NOT NULL COMMENT '使用的渠道',
    `channel_message_id` VARCHAR(100) COMMENT '渠道返回的消息ID',
    `send_status`        VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '发送状态：PENDING,SUCCESS,FAILED,TIMEOUT',
    `error_code`         VARCHAR(50) COMMENT '错误代码',
    `error_message`      VARCHAR(500) COMMENT '错误信息',
    `business_tag`       VARCHAR(50) COMMENT '业务标识',
    `send_count`         INT         NOT NULL DEFAULT 1 COMMENT '发送次数（重试）',
    `sent_at`            DATETIME COMMENT '发送时间',
    `delivered_at`       DATETIME COMMENT '送达时间',
    `created_at`         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`            TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',

    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_request_id` (`request_id`),
    INDEX `idx_template_code` (`template_code`),
    INDEX `idx_phone_number` (`phone_number`),
    INDEX `idx_channel_type` (`channel_type`),
    INDEX `idx_send_status` (`send_status`),
    INDEX `idx_business_tag` (`business_tag`),
    INDEX `idx_sent_at` (`sent_at`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='短信发送记录表';

-- ==========================================
-- 📊 短信发送任务表
-- ==========================================
DROP TABLE IF EXISTS `sms_send_task`;
CREATE TABLE `sms_send_task`
(
    `id`                    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `task_id`               VARCHAR(50) NOT NULL UNIQUE COMMENT '任务ID',
    `request_id`            VARCHAR(50) COMMENT '请求ID',
    `template_code`         VARCHAR(50) NOT NULL COMMENT '模板代码',
    `phone_numbers`         JSON        NOT NULL COMMENT '手机号列表',
    `template_params`       JSON COMMENT '模板参数',
    `preferred_channel`     VARCHAR(20) COMMENT '优先渠道',
    `load_balance_strategy` VARCHAR(20) NOT NULL DEFAULT 'ROUND_ROBIN' COMMENT '负载均衡策略',
    `business_tag`          VARCHAR(50) COMMENT '业务标识',
    `task_status`           VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态：PENDING,PROCESSING,SUCCESS,PARTIAL_SUCCESS,FAILED',
    `total_count`           INT         NOT NULL DEFAULT 0 COMMENT '总数量',
    `success_count`         INT         NOT NULL DEFAULT 0 COMMENT '成功数量',
    `failed_count`          INT         NOT NULL DEFAULT 0 COMMENT '失败数量',
    `used_channel`          VARCHAR(20) COMMENT '实际使用的渠道',
    `error_message`         VARCHAR(500) COMMENT '错误信息',
    `started_at`            DATETIME COMMENT '开始处理时间',
    `completed_at`          DATETIME COMMENT '完成时间',
    `created_at`            DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`               TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',

    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_request_id` (`request_id`),
    INDEX `idx_template_code` (`template_code`),
    INDEX `idx_task_status` (`task_status`),
    INDEX `idx_business_tag` (`business_tag`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='短信发送任务表';

-- ==========================================
-- 📡 短信渠道配置表
-- ==========================================
DROP TABLE IF EXISTS `sms_channel`;
CREATE TABLE `sms_channel`
(
    `id`                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `channel_code`      VARCHAR(20) NOT NULL UNIQUE COMMENT '渠道代码',
    `channel_name`      VARCHAR(50) NOT NULL COMMENT '渠道名称',
    `channel_type`      VARCHAR(20) NOT NULL COMMENT '渠道类型：ALIYUN,TENCENT,BAIDU,HUAWEI,JD_CLOUD',
    `config`            JSON        NOT NULL COMMENT '渠道配置信息',
    `priority`          INT         NOT NULL DEFAULT 1 COMMENT '优先级（数字越小优先级越高）',
    `weight`            INT         NOT NULL DEFAULT 1 COMMENT '权重（负载均衡）',
    `rate_limit`        INT         NOT NULL DEFAULT 100 COMMENT '速率限制（条/分钟）',
    `daily_limit`       INT         NOT NULL DEFAULT 10000 COMMENT '日发送限制',
    `status`            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE,INACTIVE,MAINTENANCE',
    `health_status`     VARCHAR(20) NOT NULL DEFAULT 'HEALTHY' COMMENT '健康状态：HEALTHY,UNHEALTHY,UNKNOWN',
    `last_health_check` DATETIME COMMENT '最后健康检查时间',
    `success_rate`      DECIMAL(5, 2)        DEFAULT 0.00 COMMENT '成功率（%）',
    `avg_response_time` INT                  DEFAULT 0 COMMENT '平均响应时间（毫秒）',
    `created_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',

    INDEX `idx_channel_code` (`channel_code`),
    INDEX `idx_channel_type` (`channel_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_priority` (`priority`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='短信渠道配置表';

-- ==========================================
-- 🚫 黑名单表
-- ==========================================
DROP TABLE IF EXISTS `sms_blacklist`;
CREATE TABLE `sms_blacklist`
(
    `id`             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `phone_number`   VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    `blacklist_type` VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT '黑名单类型：MANUAL,AUTO,COMPLAINT',
    `reason`         VARCHAR(200) COMMENT '加入黑名单原因',
    `operator`       VARCHAR(50) COMMENT '操作员',
    `expired_at`     DATETIME COMMENT '过期时间（为空表示永久）',
    `created_at`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',

    INDEX `idx_phone_number` (`phone_number`),
    INDEX `idx_blacklist_type` (`blacklist_type`),
    INDEX `idx_expired_at` (`expired_at`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='短信黑名单表';

-- ==========================================
-- 📈 短信统计表
-- ==========================================
DROP TABLE IF EXISTS `sms_statistics`;
CREATE TABLE `sms_statistics`
(
    `id`                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `stat_date`         DATE     NOT NULL COMMENT '统计日期',
    `stat_hour`         TINYINT COMMENT '统计小时（0-23，为空表示全天）',
    `channel_type`      VARCHAR(20) COMMENT '渠道类型（为空表示全部渠道）',
    `template_type`     VARCHAR(20) COMMENT '模板类型（为空表示全部类型）',
    `business_tag`      VARCHAR(50) COMMENT '业务标识（为空表示全部业务）',
    `total_count`       BIGINT   NOT NULL DEFAULT 0 COMMENT '总发送数',
    `success_count`     BIGINT   NOT NULL DEFAULT 0 COMMENT '成功数',
    `failed_count`      BIGINT   NOT NULL DEFAULT 0 COMMENT '失败数',
    `success_rate`      DECIMAL(5, 2)     DEFAULT 0.00 COMMENT '成功率（%）',
    `avg_response_time` INT               DEFAULT 0 COMMENT '平均响应时间（毫秒）',
    `created_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY `uk_statistics` (`stat_date`, `stat_hour`, `channel_type`, `template_type`, `business_tag`),
    INDEX `idx_stat_date` (`stat_date`),
    INDEX `idx_channel_type` (`channel_type`),
    INDEX `idx_template_type` (`template_type`),
    INDEX `idx_business_tag` (`business_tag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='短信统计表';

-- ==========================================
-- 🎯 领域事件表
-- ==========================================
DROP TABLE IF EXISTS `sms_domain_event`;
CREATE TABLE `sms_domain_event`
(
    `id`             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `event_id`       VARCHAR(50)  NOT NULL UNIQUE COMMENT '事件ID',
    `event_type`     VARCHAR(100) NOT NULL COMMENT '事件类型',
    `aggregate_id`   VARCHAR(50)  NOT NULL COMMENT '聚合根ID',
    `aggregate_type` VARCHAR(50)  NOT NULL COMMENT '聚合根类型',
    `event_data`     JSON         NOT NULL COMMENT '事件数据',
    `version`        INT          NOT NULL DEFAULT 1 COMMENT '事件版本',
    `occurred_on`    DATETIME     NOT NULL COMMENT '事件发生时间',
    `processed`      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已处理',
    `processed_at`   DATETIME COMMENT '处理时间',
    `retry_count`    INT          NOT NULL DEFAULT 0 COMMENT '重试次数',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX `idx_event_id` (`event_id`),
    INDEX `idx_event_type` (`event_type`),
    INDEX `idx_aggregate_id` (`aggregate_id`),
    INDEX `idx_occurred_on` (`occurred_on`),
    INDEX `idx_processed` (`processed`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='领域事件表';

-- ==========================================
-- 📋 初始化数据
-- ==========================================

-- 插入默认渠道配置
INSERT INTO `sms_channel` (`channel_code`, `channel_name`, `channel_type`, `config`, `priority`, `weight`, `status`)
VALUES ('ALIYUN_DEFAULT', '阿里云短信', 'ALIYUN', '{
  "accessKeyId": "",
  "accessKeySecret": "",
  "endpoint": "dysmsapi.aliyuncs.com"
}', 1, 3, 'ACTIVE'),
       ('TENCENT_DEFAULT', '腾讯云短信', 'TENCENT', '{
         "secretId": "",
         "secretKey": "",
         "region": "ap-guangzhou",
         "sdkAppId": ""
       }', 2, 2, 'ACTIVE'),
       ('BAIDU_DEFAULT', '百度云短信', 'BAIDU', '{
         "accessKeyId": "",
         "secretAccessKey": "",
         "endpoint": "sms.bj.baidubce.com"
       }', 3, 1, 'INACTIVE');

-- 插入默认短信模板
INSERT INTO `sms_template` (`template_code`, `template_name`, `template_content`, `parameter_config`, `template_type`,
                            `supported_channels`, `status`)
VALUES ('USER_REGISTER_VERIFY', '用户注册验证码', '您的注册验证码是{code}，请在{minutes}分钟内输入。', '{
  "code": "验证码",
  "minutes": "有效时间"
}', 'VERIFICATION', '[
  "ALIYUN",
  "TENCENT"
]', 'ACTIVE'),
       ('PASSWORD_RESET_VERIFY', '密码重置验证码', '您的密码重置验证码是{code}，请在{minutes}分钟内输入。', '{
         "code": "验证码",
         "minutes": "有效时间"
       }', 'VERIFICATION', '[
         "ALIYUN",
         "TENCENT"
       ]', 'ACTIVE'),
       ('LOGIN_NOTIFY', '登录通知', '您的账号于{time}在{location}登录，如非本人操作请及时修改密码。', '{
         "time": "登录时间",
         "location": "登录地点"
       }', 'NOTIFICATION', '[
         "ALIYUN",
         "TENCENT",
         "BAIDU"
       ]', 'ACTIVE'),
       ('ORDER_STATUS_NOTIFY', '订单状态通知', '您的订单{orderNo}状态已更新为{status}，详情请查看APP。', '{
         "orderNo": "订单号",
         "status": "订单状态"
       }', 'NOTIFICATION', '[
         "ALIYUN",
         "TENCENT"
       ]', 'ACTIVE');

-- 创建视图：统计概览
CREATE OR REPLACE VIEW `v_sms_statistics_overview` AS
SELECT DATE(created_at)                                                                      as stat_date,
       COUNT(*)                                                                              as total_sent,
       SUM(CASE WHEN send_status = 'SUCCESS' THEN 1 ELSE 0 END)                              as success_count,
       SUM(CASE WHEN send_status = 'FAILED' THEN 1 ELSE 0 END)                               as failed_count,
       ROUND(SUM(CASE WHEN send_status = 'SUCCESS' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as success_rate,
       channel_type
FROM `sms_send_record`
WHERE deleted = 0
GROUP BY DATE(created_at), channel_type
ORDER BY stat_date DESC, channel_type;

-- 创建视图：模板使用统计
CREATE OR REPLACE VIEW `v_template_usage_stats` AS
SELECT t.template_code,
       t.template_name,
       t.template_type,
       COUNT(r.id)                                                                                as usage_count,
       SUM(CASE WHEN r.send_status = 'SUCCESS' THEN 1 ELSE 0 END)                                 as success_count,
       ROUND(SUM(CASE WHEN r.send_status = 'SUCCESS' THEN 1 ELSE 0 END) * 100.0 / COUNT(r.id), 2) as success_rate,
       MAX(r.created_at)                                                                          as last_used_at
FROM `sms_template` t
         LEFT JOIN `sms_send_record` r ON t.template_code = r.template_code
WHERE t.deleted = 0
GROUP BY t.template_code, t.template_name, t.template_type
ORDER BY usage_count DESC;

-- ==========================================
-- 📝 注释说明
-- ==========================================
/*
数据库设计说明：

1. 表设计遵循DDD原则：
   - sms_template：短信模板聚合根
   - sms_send_record：发送记录实体
   - sms_send_task：发送任务聚合根
   - sms_channel：渠道配置
   - domain_event：领域事件存储

2. 索引设计：
   - 主要查询字段都建立了索引
   - 复合索引用于复杂查询场景
   - 覆盖索引优化常见查询

3. 数据类型选择：
   - JSON类型存储复杂配置和参数
   - DATETIME存储时间信息
   - VARCHAR合理长度设计

4. 约束设计：
   - 唯一约束防止重复
   - 外键关系保证数据一致性
   - 逻辑删除支持数据恢复

5. 性能优化：
   - 分区表设计（可选）
   - 读写分离支持
   - 缓存友好的查询
*/

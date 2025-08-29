-- ==========================================
-- 🤝 社交服务数据库初始化 - DDD架构
-- 聚合根：SocialAggregate
-- ==========================================

-- 创建数据库
CREATE
DATABASE IF NOT EXISTS xypai_social DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE
xypai_social;

-- 社交关系表 (聚合根表)
DROP TABLE IF EXISTS `social_relation`;
CREATE TABLE `social_relation`
(
    `id`             BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`        BIGINT NOT NULL COMMENT '用户ID',
    `target_user_id` BIGINT NOT NULL COMMENT '目标用户ID',
    `relation_type`  ENUM('FOLLOW', 'BLOCK', 'FRIEND') NOT NULL COMMENT '关系类型',
    `status`         ENUM('ACTIVE', 'PENDING', 'REJECTED', 'DELETED') DEFAULT 'ACTIVE' COMMENT '关系状态',
    `create_time`    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT  DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target_type` (`user_id`, `target_user_id`, `relation_type`),
    KEY              `idx_user_relation` (`user_id`, `relation_type`, `status`),
    KEY              `idx_target_user` (`target_user_id`, `relation_type`, `status`),
    KEY              `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社交关系表-DDD聚合根';

-- 社交设置表 (实体表)
DROP TABLE IF EXISTS `social_settings`;
CREATE TABLE `social_settings`
(
    `id`                  BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`             BIGINT NOT NULL COMMENT '用户ID',
    `allow_follow`        BOOLEAN  DEFAULT TRUE COMMENT '允许被关注',
    `follow_need_approve` BOOLEAN  DEFAULT FALSE COMMENT '关注需要审核',
    `allow_message`       BOOLEAN  DEFAULT TRUE COMMENT '允许私信',
    `allow_tag`           BOOLEAN  DEFAULT TRUE COMMENT '允许被@',
    `privacy_level`       ENUM('PUBLIC', 'FRIENDS', 'PRIVATE') DEFAULT 'PUBLIC' COMMENT '隐私级别',
    `auto_accept_friend`  BOOLEAN  DEFAULT FALSE COMMENT '自动接受好友请求',
    `show_online_status`  BOOLEAN  DEFAULT TRUE COMMENT '显示在线状态',
    `create_time`         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社交设置表-DDD实体';

-- 社交统计表 (值对象表)
DROP TABLE IF EXISTS `social_stats`;
CREATE TABLE `social_stats`
(
    `id`                  BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`             BIGINT NOT NULL COMMENT '用户ID',
    `following_count`     INT      DEFAULT 0 COMMENT '关注数',
    `followers_count`     INT      DEFAULT 0 COMMENT '粉丝数',
    `friends_count`       INT      DEFAULT 0 COMMENT '好友数',
    `blocked_count`       INT      DEFAULT 0 COMMENT '拉黑数',
    `last_calculate_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后计算时间',
    `update_time`         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY                   `idx_following_count` (`following_count`),
    KEY                   `idx_followers_count` (`followers_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社交统计表-DDD值对象';

-- 领域事件表
DROP TABLE IF EXISTS `domain_event`;
CREATE TABLE `domain_event`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `event_id`     VARCHAR(64)  NOT NULL COMMENT '事件ID',
    `event_type`   VARCHAR(100) NOT NULL COMMENT '事件类型',
    `aggregate_id` VARCHAR(64)  NOT NULL COMMENT '聚合根ID',
    `event_data`   JSON         NOT NULL COMMENT '事件数据',
    `status`       ENUM('PENDING', 'PUBLISHED', 'FAILED') DEFAULT 'PENDING' COMMENT '状态',
    `occurred_on`  DATETIME(3) NOT NULL COMMENT '发生时间',
    `published_on` DATETIME(3) DEFAULT NULL COMMENT '发布时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_event_id` (`event_id`),
    KEY            `idx_event_type` (`event_type`),
    KEY            `idx_aggregate_id` (`aggregate_id`),
    KEY            `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='领域事件表';

-- 插入测试数据
INSERT INTO `social_settings` (`user_id`, `allow_follow`, `follow_need_approve`, `privacy_level`)
VALUES (1, TRUE, FALSE, 'PUBLIC'),
       (2, TRUE, FALSE, 'PUBLIC'),
       (3, TRUE, TRUE, 'FRIENDS'),
       (4, TRUE, FALSE, 'PUBLIC');

INSERT INTO `social_stats` (`user_id`, `following_count`, `followers_count`)
VALUES (1, 2, 1),
       (2, 1, 2),
       (3, 0, 1),
       (4, 1, 0);

-- 插入关注关系
INSERT INTO `social_relation` (`user_id`, `target_user_id`, `relation_type`, `status`)
VALUES (1, 2, 'FOLLOW', 'ACTIVE'),
       (1, 3, 'FOLLOW', 'ACTIVE'),
       (2, 1, 'FOLLOW', 'ACTIVE'),
       (4, 3, 'FOLLOW', 'ACTIVE');

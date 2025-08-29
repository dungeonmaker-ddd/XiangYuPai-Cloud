-- ==========================================
-- 📱 动态服务数据库初始化 - DDD架构
-- 聚合根：FeedAggregate + InteractionAggregate
-- ==========================================

-- 创建数据库
CREATE
DATABASE IF NOT EXISTS xypai_feed DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE
xypai_feed;

-- 用户动态表 (FeedAggregate聚合根表)
DROP TABLE IF EXISTS `user_feed`;
CREATE TABLE `user_feed`
(
    `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       BIGINT NOT NULL COMMENT '用户ID',
    `content`       TEXT   NOT NULL COMMENT '动态内容',
    `media_urls`    JSON         DEFAULT NULL COMMENT '媒体URL列表',
    `feed_type`     ENUM('TEXT', 'IMAGE', 'VIDEO', 'LINK', 'ACTIVITY') DEFAULT 'TEXT' COMMENT '动态类型',
    `status`        ENUM('DRAFT', 'PUBLISHED', 'HIDDEN', 'DELETED') DEFAULT 'DRAFT' COMMENT '动态状态',
    `privacy_level` ENUM('PUBLIC', 'FRIENDS', 'PRIVATE') DEFAULT 'PUBLIC' COMMENT '隐私级别',
    `location`      VARCHAR(200) DEFAULT NULL COMMENT '发布地点',
    `tags`          VARCHAR(500) DEFAULT NULL COMMENT '标签',
    `view_count`    BIGINT       DEFAULT 0 COMMENT '浏览次数',
    `like_count`    INT          DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT          DEFAULT 0 COMMENT '评论数',
    `share_count`   INT          DEFAULT 0 COMMENT '分享数',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `publish_time`  DATETIME     DEFAULT NULL COMMENT '发布时间',
    `deleted`       TINYINT      DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    KEY             `idx_user_time` (`user_id`, `publish_time`),
    KEY             `idx_status` (`status`),
    KEY             `idx_feed_type` (`feed_type`),
    KEY             `idx_publish_time` (`publish_time`),
    KEY             `idx_privacy_level` (`privacy_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户动态表-DDD聚合根';

-- 动态设置表 (FeedAggregate实体表)
DROP TABLE IF EXISTS `feed_settings`;
CREATE TABLE `feed_settings`
(
    `id`                    BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `feed_id`               BIGINT NOT NULL COMMENT '动态ID',
    `allow_comment`         BOOLEAN  DEFAULT TRUE COMMENT '允许评论',
    `allow_share`           BOOLEAN  DEFAULT TRUE COMMENT '允许分享',
    `allow_like`            BOOLEAN  DEFAULT TRUE COMMENT '允许点赞',
    `notify_on_interaction` BOOLEAN  DEFAULT TRUE COMMENT '互动通知',
    `auto_archive_days`     INT      DEFAULT 0 COMMENT '自动归档天数(0=不归档)',
    `create_time`           DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`           DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_feed_id` (`feed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态设置表-DDD实体';

-- 互动目标表 (InteractionAggregate聚合根表)
DROP TABLE IF EXISTS `interaction_target`;
CREATE TABLE `interaction_target`
(
    `id`                    BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `target_id`             BIGINT NOT NULL COMMENT '目标ID',
    `target_type`           ENUM('FEED', 'ACTIVITY', 'COMMENT', 'USER') NOT NULL COMMENT '目标类型',
    `like_count`            INT      DEFAULT 0 COMMENT '点赞数',
    `favorite_count`        INT      DEFAULT 0 COMMENT '收藏数',
    `comment_count`         INT      DEFAULT 0 COMMENT '评论数',
    `share_count`           INT      DEFAULT 0 COMMENT '分享数',
    `last_interaction_time` DATETIME DEFAULT NULL COMMENT '最后互动时间',
    `create_time`           DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`           DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_target_type` (`target_id`, `target_type`),
    KEY                     `idx_target_type` (`target_type`),
    KEY                     `idx_like_count` (`like_count`),
    KEY                     `idx_last_interaction` (`last_interaction_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='互动目标表-DDD聚合根';

-- 互动记录表 (InteractionAggregate实体表)
DROP TABLE IF EXISTS `interaction_record`;
CREATE TABLE `interaction_record`
(
    `id`               BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`          BIGINT NOT NULL COMMENT '用户ID',
    `target_id`        BIGINT NOT NULL COMMENT '目标ID',
    `target_type`      ENUM('FEED', 'ACTIVITY', 'COMMENT', 'USER') NOT NULL COMMENT '目标类型',
    `interaction_type` ENUM('LIKE', 'FAVORITE', 'SHARE', 'COMMENT', 'VIEW') NOT NULL COMMENT '互动类型',
    `content`          TEXT         DEFAULT NULL COMMENT '互动内容(评论内容)',
    `parent_id`        BIGINT       DEFAULT NULL COMMENT '父评论ID',
    `device_info`      VARCHAR(200) DEFAULT NULL COMMENT '设备信息',
    `ip_address`       VARCHAR(50)  DEFAULT NULL COMMENT 'IP地址',
    `create_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`          TINYINT      DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    KEY                `idx_user_target` (`user_id`, `target_id`, `target_type`),
    KEY                `idx_target_type` (`target_id`, `target_type`),
    KEY                `idx_interaction_type` (`interaction_type`),
    KEY                `idx_parent_id` (`parent_id`),
    KEY                `idx_create_time` (`create_time`),
    UNIQUE KEY `uk_user_target_type` (`user_id`, `target_id`, `target_type`, `interaction_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='互动记录表-DDD实体';

-- 动态标签表 (值对象表)
DROP TABLE IF EXISTS `feed_tag`;
CREATE TABLE `feed_tag`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tag_name`    VARCHAR(50) NOT NULL COMMENT '标签名称',
    `usage_count` INT      DEFAULT 0 COMMENT '使用次数',
    `is_hot`      BOOLEAN  DEFAULT FALSE COMMENT '是否热门标签',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_name` (`tag_name`),
    KEY           `idx_usage_count` (`usage_count`),
    KEY           `idx_is_hot` (`is_hot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态标签表-DDD值对象';

-- 动态标签关联表
DROP TABLE IF EXISTS `feed_tag_relation`;
CREATE TABLE `feed_tag_relation`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `feed_id`     BIGINT NOT NULL COMMENT '动态ID',
    `tag_id`      BIGINT NOT NULL COMMENT '标签ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_feed_tag` (`feed_id`, `tag_id`),
    KEY           `idx_feed_id` (`feed_id`),
    KEY           `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态标签关联表';

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
INSERT INTO `user_feed` (`user_id`, `content`, `feed_type`, `status`, `privacy_level`, `publish_time`)
VALUES (1, '这是我的第一条动态！', 'TEXT', 'PUBLISHED', 'PUBLIC', NOW()),
       (2, '分享一张美丽的风景照片', 'IMAGE', 'PUBLISHED', 'PUBLIC', NOW()),
       (3, '今天天气真好，适合出门走走', 'TEXT', 'PUBLISHED', 'FRIENDS', NOW()),
       (4, '推荐一个不错的技术博客', 'LINK', 'PUBLISHED', 'PUBLIC', NOW());

INSERT INTO `feed_settings` (`feed_id`, `allow_comment`, `allow_share`, `allow_like`)
VALUES (1, TRUE, TRUE, TRUE),
       (2, TRUE, TRUE, TRUE),
       (3, TRUE, FALSE, TRUE),
       (4, TRUE, TRUE, TRUE);

INSERT INTO `interaction_target` (`target_id`, `target_type`, `like_count`, `comment_count`)
VALUES (1, 'FEED', 5, 2),
       (2, 'FEED', 8, 3),
       (3, 'FEED', 2, 1),
       (4, 'FEED', 3, 0);

INSERT INTO `interaction_record` (`user_id`, `target_id`, `target_type`, `interaction_type`)
VALUES (2, 1, 'FEED', 'LIKE'),
       (3, 1, 'FEED', 'LIKE'),
       (4, 1, 'FEED', 'LIKE'),
       (1, 2, 'FEED', 'LIKE'),
       (3, 2, 'FEED', 'LIKE'),
       (4, 2, 'FEED', 'LIKE');

INSERT INTO `feed_tag` (`tag_name`, `usage_count`, `is_hot`)
VALUES ('生活', 2, TRUE),
       ('摄影', 1, FALSE),
       ('技术', 1, FALSE),
       ('分享', 3, TRUE);

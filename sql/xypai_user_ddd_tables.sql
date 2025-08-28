-- ========================================
-- XyPai User模块 - DDD架构完整表设计
-- 版本: 2.0 DDD Architecture
-- 创建时间: 2025-01-02
-- 描述: 基于DDD聚合根架构的完整数据库表设计
-- ========================================

-- 设置字符集
SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;

-- ========================================
-- 1. 用户聚合根相关表
-- ========================================

-- 用户基础信息表 (UserAggregate)
DROP TABLE IF EXISTS `app_user`;
CREATE TABLE `app_user`
(
    `user_id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `mobile`          VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
    `username`        VARCHAR(50)           DEFAULT NULL COMMENT '用户名',
    `nickname`        VARCHAR(100) NOT NULL COMMENT '昵称',
    `avatar`          VARCHAR(500)          DEFAULT NULL COMMENT '头像URL',
    `bio`             TEXT COMMENT '个人简介',
    `gender`          ENUM('MALE', 'FEMALE', 'UNKNOWN') DEFAULT 'UNKNOWN' COMMENT '性别',
    `birth_date`      DATE                  DEFAULT NULL COMMENT '生日',
    `location`        VARCHAR(200)          DEFAULT NULL COMMENT '地理位置',
    `status`          ENUM('ACTIVE', 'INACTIVE', 'BANNED', 'DELETED') DEFAULT 'ACTIVE' COMMENT '用户状态',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_login_time` DATETIME              DEFAULT NULL COMMENT '最后登录时间',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_mobile` (`mobile`),
    UNIQUE KEY `uk_username` (`username`),
    KEY               `idx_status` (`status`),
    KEY               `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基础信息表';

-- ========================================
-- 2. 社交聚合根相关表
-- ========================================

-- 社交关系表 (SocialAggregate)
DROP TABLE IF EXISTS `social_relation`;
CREATE TABLE `social_relation`
(
    `id`             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`        BIGINT   NOT NULL COMMENT '用户ID',
    `target_user_id` BIGINT   NOT NULL COMMENT '目标用户ID',
    `relation_type`  ENUM('FOLLOW', 'BLOCK', 'FRIEND') NOT NULL COMMENT '关系类型',
    `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target_type` (`user_id`, `target_user_id`, `relation_type`),
    KEY              `idx_user_relation` (`user_id`, `relation_type`),
    KEY              `idx_target_user` (`target_user_id`, `relation_type`),
    KEY              `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社交关系表';

-- 社交设置表 (SocialAggregate)
DROP TABLE IF EXISTS `social_settings`;
CREATE TABLE `social_settings`
(
    `id`                  BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`             BIGINT   NOT NULL COMMENT '用户ID',
    `allow_follow`        BOOLEAN           DEFAULT TRUE COMMENT '允许被关注',
    `follow_need_approve` BOOLEAN           DEFAULT FALSE COMMENT '关注需要审核',
    `allow_message`       BOOLEAN           DEFAULT TRUE COMMENT '允许私信',
    `allow_tag`           BOOLEAN           DEFAULT TRUE COMMENT '允许被@',
    `privacy_level`       ENUM('PUBLIC', 'FRIENDS', 'PRIVATE') DEFAULT 'PUBLIC' COMMENT '隐私级别',
    `update_time`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社交设置表';

-- ========================================
-- 3. 活动聚合根相关表
-- ========================================

-- 活动表 (ActivityAggregate)
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity`
(
    `activity_id`      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    `organizer_id`     BIGINT       NOT NULL COMMENT '组织者ID',
    `title`            VARCHAR(200) NOT NULL COMMENT '活动标题',
    `description`      TEXT COMMENT '活动描述',
    `location`         VARCHAR(500)          DEFAULT NULL COMMENT '活动地点',
    `start_time`       DATETIME     NOT NULL COMMENT '开始时间',
    `end_time`         DATETIME     NOT NULL COMMENT '结束时间',
    `max_participants` INT                   DEFAULT NULL COMMENT '最大参与人数',
    `fee`              DECIMAL(10, 2)        DEFAULT 0.00 COMMENT '活动费用',
    `status`           ENUM('DRAFT', 'PUBLISHED', 'ONGOING', 'FINISHED', 'CANCELLED') DEFAULT 'DRAFT' COMMENT '活动状态',
    `type`             ENUM('SOCIAL', 'SPORTS', 'CULTURE', 'EDUCATION', 'BUSINESS', 'OTHER') DEFAULT 'SOCIAL' COMMENT '活动类型',
    `settings`         JSON                  DEFAULT NULL COMMENT '活动设置',
    `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`activity_id`),
    KEY                `idx_organizer` (`organizer_id`),
    KEY                `idx_status` (`status`),
    KEY                `idx_type` (`type`),
    KEY                `idx_start_time` (`start_time`),
    KEY                `idx_location` (`location`(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- 活动参与者表 (ActivityAggregate)
DROP TABLE IF EXISTS `activity_participant`;
CREATE TABLE `activity_participant`
(
    `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `activity_id`  BIGINT   NOT NULL COMMENT '活动ID',
    `user_id`      BIGINT   NOT NULL COMMENT '用户ID',
    `status`       ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'WAITLIST') DEFAULT 'PENDING' COMMENT '参与状态',
    `message`      TEXT COMMENT '申请留言',
    `join_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `approve_time` DATETIME          DEFAULT NULL COMMENT '审核时间',
    `approver_id`  BIGINT            DEFAULT NULL COMMENT '审核人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
    KEY            `idx_activity_status` (`activity_id`, `status`),
    KEY            `idx_user_status` (`user_id`, `status`),
    KEY            `idx_join_time` (`join_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动参与者表';

-- ========================================
-- 4. 互动聚合根相关表
-- ========================================

-- 互动目标统计表 (InteractionAggregate)
DROP TABLE IF EXISTS `interaction_target`;
CREATE TABLE `interaction_target`
(
    `id`             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `target_id`      BIGINT   NOT NULL COMMENT '目标ID',
    `target_type`    ENUM('FEED', 'ACTIVITY', 'COMMENT') NOT NULL COMMENT '目标类型',
    `like_count`     INT               DEFAULT 0 COMMENT '点赞数',
    `favorite_count` INT               DEFAULT 0 COMMENT '收藏数',
    `comment_count`  INT               DEFAULT 0 COMMENT '评论数',
    `share_count`    INT               DEFAULT 0 COMMENT '分享数',
    `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_target_type` (`target_id`, `target_type`),
    KEY              `idx_target_type` (`target_type`),
    KEY              `idx_like_count` (`like_count`),
    KEY              `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='互动目标统计表';

-- 互动记录表 (InteractionAggregate)
DROP TABLE IF EXISTS `interaction_record`;
CREATE TABLE `interaction_record`
(
    `id`               BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`          BIGINT   NOT NULL COMMENT '用户ID',
    `target_id`        BIGINT   NOT NULL COMMENT '目标ID',
    `target_type`      ENUM('FEED', 'ACTIVITY', 'COMMENT') NOT NULL COMMENT '目标类型',
    `interaction_type` ENUM('LIKE', 'FAVORITE', 'COMMENT', 'SHARE') NOT NULL COMMENT '互动类型',
    `content`          TEXT COMMENT '互动内容(评论内容)',
    `parent_id`        BIGINT            DEFAULT NULL COMMENT '父评论ID',
    `create_time`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target_type` (`user_id`, `target_id`, `target_type`, `interaction_type`),
    KEY                `idx_user_target` (`user_id`, `target_id`),
    KEY                `idx_target_type` (`target_id`, `target_type`),
    KEY                `idx_parent` (`parent_id`),
    KEY                `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='互动记录表';

-- ========================================
-- 5. 动态聚合根相关表
-- ========================================

-- 用户动态表 (FeedAggregate)
DROP TABLE IF EXISTS `user_feed`;
CREATE TABLE `user_feed`
(
    `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`      BIGINT   NOT NULL COMMENT '用户ID',
    `content`      TEXT     NOT NULL COMMENT '动态内容',
    `media_urls`   JSON              DEFAULT NULL COMMENT '媒体URL列表',
    `feed_type`    ENUM('TEXT', 'IMAGE', 'VIDEO', 'ACTIVITY', 'TOPIC') DEFAULT 'TEXT' COMMENT '动态类型',
    `status`       ENUM('DRAFT', 'PUBLISHED', 'HIDDEN', 'DELETED') DEFAULT 'DRAFT' COMMENT '动态状态',
    `settings`     JSON              DEFAULT NULL COMMENT '动态设置',
    `view_count`   BIGINT            DEFAULT 0 COMMENT '浏览次数',
    `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `publish_time` DATETIME          DEFAULT NULL COMMENT '发布时间',
    PRIMARY KEY (`id`),
    KEY            `idx_user_time` (`user_id`, `create_time` DESC),
    KEY            `idx_status` (`status`),
    KEY            `idx_feed_type` (`feed_type`),
    KEY            `idx_publish_time` (`publish_time` DESC),
    KEY            `idx_view_count` (`view_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户动态表';

-- ========================================
-- 6. 钱包聚合根相关表
-- ========================================

-- 钱包表 (WalletAggregate)
DROP TABLE IF EXISTS `wallet`;
CREATE TABLE `wallet`
(
    `wallet_id`             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '钱包ID',
    `user_id`               BIGINT   NOT NULL COMMENT '用户ID',
    `balance`               DECIMAL(15, 2)    DEFAULT 0.00 COMMENT '余额',
    `frozen_balance`        DECIMAL(15, 2)    DEFAULT 0.00 COMMENT '冻结余额',
    `currency`              VARCHAR(10)       DEFAULT 'CNY' COMMENT '货币类型',
    `status`                ENUM('ACTIVE', 'FROZEN', 'RESTRICTED', 'CLOSED', 'UNDER_REVIEW') DEFAULT 'ACTIVE' COMMENT '钱包状态',
    `payment_password`      VARCHAR(255)      DEFAULT NULL COMMENT '支付密码(加密)',
    `settings`              JSON              DEFAULT NULL COMMENT '钱包设置',
    `create_time`           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_transaction_time` DATETIME          DEFAULT NULL COMMENT '最后交易时间',
    PRIMARY KEY (`wallet_id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY                     `idx_status` (`status`),
    KEY                     `idx_balance` (`balance`),
    KEY                     `idx_last_transaction` (`last_transaction_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包表';

-- 钱包交易表 (WalletAggregate)
DROP TABLE IF EXISTS `wallet_transaction`;
CREATE TABLE `wallet_transaction`
(
    `id`                      BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `transaction_id`          VARCHAR(100)   NOT NULL COMMENT '交易ID',
    `wallet_id`               BIGINT         NOT NULL COMMENT '钱包ID',
    `from_user_id`            BIGINT                  DEFAULT NULL COMMENT '发送用户ID',
    `to_user_id`              BIGINT                  DEFAULT NULL COMMENT '接收用户ID',
    `amount`                  DECIMAL(15, 2) NOT NULL COMMENT '交易金额',
    `fee`                     DECIMAL(15, 2)          DEFAULT 0.00 COMMENT '手续费',
    `type`                    ENUM('RECHARGE', 'WITHDRAW', 'TRANSFER_OUT', 'TRANSFER_IN', 'PAYMENT', 'REFUND', 'REWARD', 'RED_PACKET_SEND', 'RED_PACKET_RECEIVE', 'FEE', 'ADJUSTMENT') NOT NULL COMMENT '交易类型',
    `status`                  ENUM('PENDING', 'PROCESSING', 'SUCCESS', 'FAILED', 'CANCELLED', 'REFUNDED') DEFAULT 'PENDING' COMMENT '交易状态',
    `description`             VARCHAR(500)            DEFAULT NULL COMMENT '交易描述',
    `memo`                    TEXT COMMENT '交易备注',
    `external_transaction_id` VARCHAR(100)            DEFAULT NULL COMMENT '外部交易ID',
    `create_time`             DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `complete_time`           DATETIME                DEFAULT NULL COMMENT '完成时间',
    `failure_reason`          TEXT COMMENT '失败原因',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_transaction_id` (`transaction_id`),
    KEY                       `idx_wallet_time` (`wallet_id`, `create_time` DESC),
    KEY                       `idx_from_user` (`from_user_id`),
    KEY                       `idx_to_user` (`to_user_id`),
    KEY                       `idx_type_status` (`type`, `status`),
    KEY                       `idx_external_id` (`external_transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包交易表';

-- ========================================
-- 7. 领域事件相关表
-- ========================================

-- 领域事件日志表
DROP TABLE IF EXISTS `domain_event_log`;
CREATE TABLE `domain_event_log`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `event_id`       VARCHAR(100) NOT NULL COMMENT '事件ID',
    `event_type`     VARCHAR(100) NOT NULL COMMENT '事件类型',
    `aggregate_type` VARCHAR(50)  NOT NULL COMMENT '聚合根类型',
    `aggregate_id`   VARCHAR(100) NOT NULL COMMENT '聚合根ID',
    `event_data`     JSON         NOT NULL COMMENT '事件数据',
    `status`         ENUM('PENDING', 'PROCESSING', 'SUCCESS', 'FAILED', 'SKIPPED') DEFAULT 'PENDING' COMMENT '处理状态',
    `retry_count`    INT      DEFAULT 0 COMMENT '重试次数',
    `occurred_on`    DATETIME     NOT NULL COMMENT '发生时间',
    `processed_on`   DATETIME DEFAULT NULL COMMENT '处理时间',
    `error_message`  TEXT COMMENT '错误信息',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_event_id` (`event_id`),
    KEY              `idx_event_type` (`event_type`),
    KEY              `idx_aggregate` (`aggregate_type`, `aggregate_id`),
    KEY              `idx_status` (`status`),
    KEY              `idx_occurred_time` (`occurred_on`),
    KEY              `idx_retry` (`retry_count`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='领域事件日志表';

-- ========================================
-- 8. 创建外键约束
-- ========================================

-- 社交关系外键
ALTER TABLE `social_relation`
    ADD CONSTRAINT `fk_social_relation_user` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_social_relation_target` FOREIGN KEY (`target_user_id`) REFERENCES `app_user` (`user_id`) ON
DELETE
CASCADE;

-- 社交设置外键
ALTER TABLE `social_settings`
    ADD CONSTRAINT `fk_social_settings_user` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`user_id`) ON DELETE CASCADE;

-- 活动外键
ALTER TABLE `activity`
    ADD CONSTRAINT `fk_activity_organizer` FOREIGN KEY (`organizer_id`) REFERENCES `app_user` (`user_id`) ON DELETE CASCADE;

-- 活动参与者外键
ALTER TABLE `activity_participant`
    ADD CONSTRAINT `fk_activity_participant_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`activity_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_activity_participant_user` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`user_id`) ON
DELETE
CASCADE,
  ADD CONSTRAINT `fk_activity_participant_approver` FOREIGN KEY (`approver_id`) REFERENCES `app_user` (`user_id`) ON DELETE
SET NULL;

-- 互动记录外键
ALTER TABLE `interaction_record`
    ADD CONSTRAINT `fk_interaction_record_user` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_interaction_record_parent` FOREIGN KEY (`parent_id`) REFERENCES `interaction_record` (`id`) ON
DELETE
CASCADE;

-- 用户动态外键
ALTER TABLE `user_feed`
    ADD CONSTRAINT `fk_user_feed_user` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`user_id`) ON DELETE CASCADE;

-- 钱包外键
ALTER TABLE `wallet`
    ADD CONSTRAINT `fk_wallet_user` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`user_id`) ON DELETE CASCADE;

-- 钱包交易外键
ALTER TABLE `wallet_transaction`
    ADD CONSTRAINT `fk_wallet_transaction_wallet` FOREIGN KEY (`wallet_id`) REFERENCES `wallet` (`wallet_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_wallet_transaction_from` FOREIGN KEY (`from_user_id`) REFERENCES `app_user` (`user_id`) ON
DELETE
SET NULL,
  ADD CONSTRAINT `fk_wallet_transaction_to` FOREIGN KEY (`to_user_id`) REFERENCES `app_user` (`user_id`) ON DELETE
SET NULL;

-- ========================================
-- 9. 插入初始数据
-- ========================================

-- 插入测试用户数据
INSERT INTO `app_user` (`user_id`, `mobile`, `username`, `nickname`, `avatar`, `bio`, `gender`, `status`)
VALUES (1, '13800000001', 'admin', '系统管理员', NULL, '我是系统管理员', 'UNKNOWN', 'ACTIVE'),
       (2, '13800000002', 'testuser1', '测试用户1', NULL, '我是测试用户1', 'MALE', 'ACTIVE'),
       (3, '13800000003', 'testuser2', '测试用户2', NULL, '我是测试用户2', 'FEMALE', 'ACTIVE');

-- 插入测试社交设置
INSERT INTO `social_settings` (`user_id`, `allow_follow`, `follow_need_approve`, `privacy_level`)
VALUES (1, TRUE, FALSE, 'PUBLIC'),
       (2, TRUE, FALSE, 'PUBLIC'),
       (3, TRUE, TRUE, 'FRIENDS');

-- 插入测试钱包
INSERT INTO `wallet` (`user_id`, `balance`, `currency`, `status`)
VALUES (1, 10000.00, 'CNY', 'ACTIVE'),
       (2, 1000.00, 'CNY', 'ACTIVE'),
       (3, 500.00, 'CNY', 'ACTIVE');

-- ========================================
-- 10. 设置完成
-- ========================================

SET
FOREIGN_KEY_CHECKS = 1;

-- 显示创建的表
SHOW
TABLES LIKE 'app_user';
SHOW
TABLES LIKE 'social_%';
SHOW
TABLES LIKE 'activity%';
SHOW
TABLES LIKE 'interaction_%';
SHOW
TABLES LIKE 'user_feed';
SHOW
TABLES LIKE 'wallet%';
SHOW
TABLES LIKE 'domain_event_log';

-- 输出完成信息
SELECT 'XyPai User DDD架构数据库表创建完成!' AS 'Status', COUNT(*) AS 'Total_Tables'
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
                     'app_user', 'social_relation', 'social_settings',
                     'activity', 'activity_participant', 'interaction_target',
                     'interaction_record', 'user_feed', 'wallet',
                     'wallet_transaction', 'domain_event_log'
    );

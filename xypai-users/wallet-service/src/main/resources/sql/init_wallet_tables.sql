-- ==========================================
-- 💰 钱包服务数据库初始化 - DDD架构
-- 聚合根：WalletAggregate
-- ==========================================

-- 创建数据库
CREATE
DATABASE IF NOT EXISTS xypai_wallet DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE
xypai_wallet;

-- 钱包表 (聚合根表)
DROP TABLE IF EXISTS `wallet`;
CREATE TABLE `wallet`
(
    `id`                    BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`               BIGINT NOT NULL COMMENT '用户ID',
    `balance`               DECIMAL(15, 2) DEFAULT 0.00 COMMENT '余额',
    `frozen_balance`        DECIMAL(15, 2) DEFAULT 0.00 COMMENT '冻结余额',
    `currency`              VARCHAR(10)    DEFAULT 'CNY' COMMENT '货币类型',
    `status`                ENUM('ACTIVE', 'FROZEN', 'SUSPENDED', 'CLOSED') DEFAULT 'ACTIVE' COMMENT '钱包状态',
    `payment_password`      VARCHAR(255)   DEFAULT NULL COMMENT '支付密码(加密)',
    `daily_limit`           DECIMAL(10, 2) DEFAULT 10000.00 COMMENT '日限额',
    `monthly_limit`         DECIMAL(10, 2) DEFAULT 50000.00 COMMENT '月限额',
    `version`               INT            DEFAULT 1 COMMENT '版本号(乐观锁)',
    `create_time`           DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`           DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_transaction_time` DATETIME       DEFAULT NULL COMMENT '最后交易时间',
    `deleted`               TINYINT        DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY                     `idx_status` (`status`),
    KEY                     `idx_currency` (`currency`),
    KEY                     `idx_last_transaction` (`last_transaction_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包表-DDD聚合根';

-- 钱包交易表 (实体表)
DROP TABLE IF EXISTS `wallet_transaction`;
CREATE TABLE `wallet_transaction`
(
    `id`                      BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `transaction_id`          VARCHAR(64)    NOT NULL COMMENT '交易ID(唯一)',
    `wallet_id`               BIGINT         NOT NULL COMMENT '钱包ID',
    `from_user_id`            BIGINT         DEFAULT NULL COMMENT '发送用户ID',
    `to_user_id`              BIGINT         DEFAULT NULL COMMENT '接收用户ID',
    `amount`                  DECIMAL(15, 2) NOT NULL COMMENT '交易金额',
    `fee`                     DECIMAL(10, 2) DEFAULT 0.00 COMMENT '手续费',
    `type`                    ENUM('RECHARGE', 'WITHDRAW', 'TRANSFER_OUT', 'TRANSFER_IN', 'PAYMENT', 'REFUND', 'FREEZE', 'UNFREEZE') NOT NULL COMMENT '交易类型',
    `status`                  ENUM('PENDING', 'PROCESSING', 'SUCCESS', 'FAILED', 'CANCELLED') DEFAULT 'PENDING' COMMENT '交易状态',
    `description`             VARCHAR(255)   DEFAULT NULL COMMENT '交易描述',
    `memo`                    TEXT           DEFAULT NULL COMMENT '交易备注',
    `external_transaction_id` VARCHAR(128)   DEFAULT NULL COMMENT '外部交易ID',
    `payment_method`          VARCHAR(50)    DEFAULT NULL COMMENT '支付方式',
    `balance_before`          DECIMAL(15, 2) NOT NULL COMMENT '交易前余额',
    `balance_after`           DECIMAL(15, 2) NOT NULL COMMENT '交易后余额',
    `create_time`             DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `complete_time`           DATETIME       DEFAULT NULL COMMENT '完成时间',
    `failure_reason`          TEXT           DEFAULT NULL COMMENT '失败原因',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_transaction_id` (`transaction_id`),
    KEY                       `idx_wallet_time` (`wallet_id`, `create_time`),
    KEY                       `idx_from_user` (`from_user_id`),
    KEY                       `idx_to_user` (`to_user_id`),
    KEY                       `idx_type_status` (`type`, `status`),
    KEY                       `idx_external_id` (`external_transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包交易表-DDD实体';

-- 钱包设置表 (值对象表)
DROP TABLE IF EXISTS `wallet_settings`;
CREATE TABLE `wallet_settings`
(
    `id`                          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `wallet_id`                   BIGINT NOT NULL COMMENT '钱包ID',
    `enable_payment`              BOOLEAN  DEFAULT TRUE COMMENT '启用支付',
    `enable_transfer`             BOOLEAN  DEFAULT TRUE COMMENT '启用转账',
    `enable_withdraw`             BOOLEAN  DEFAULT TRUE COMMENT '启用提现',
    `auto_accept_transfer`        BOOLEAN  DEFAULT TRUE COMMENT '自动接收转账',
    `notification_on_transaction` BOOLEAN  DEFAULT TRUE COMMENT '交易通知',
    `security_level`              TINYINT  DEFAULT 1 COMMENT '安全级别(1-5)',
    `create_time`                 DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`                 DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wallet_id` (`wallet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包设置表-DDD值对象';

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
INSERT INTO `wallet` (`user_id`, `balance`, `currency`, `status`)
VALUES (1, 1000.00, 'CNY', 'ACTIVE'),
       (2, 500.00, 'CNY', 'ACTIVE'),
       (3, 0.00, 'CNY', 'ACTIVE'),
       (4, 2000.00, 'CNY', 'ACTIVE');

INSERT INTO `wallet_settings` (`wallet_id`, `enable_payment`, `enable_transfer`, `security_level`)
VALUES (1, TRUE, TRUE, 2),
       (2, TRUE, TRUE, 1),
       (3, TRUE, FALSE, 1),
       (4, TRUE, TRUE, 3);

-- 插入交易记录
INSERT INTO `wallet_transaction` (`transaction_id`, `wallet_id`, `from_user_id`, `to_user_id`, `amount`, `type`,
                                  `status`, `description`, `balance_before`, `balance_after`)
VALUES ('TXN001', 1, NULL, 1, 1000.00, 'RECHARGE', 'SUCCESS', '初始充值', 0.00, 1000.00),
       ('TXN002', 2, NULL, 2, 500.00, 'RECHARGE', 'SUCCESS', '初始充值', 0.00, 500.00),
       ('TXN003', 4, NULL, 4, 2000.00, 'RECHARGE', 'SUCCESS', '初始充值', 0.00, 2000.00);

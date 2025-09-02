-- ==========================================
-- 👤 用户模块 - 数据库表结构
-- 4张表：user, user_profile, user_wallet, transaction
-- ==========================================

-- 1. 用户基础信息表
CREATE TABLE `user` (
    `id` BIGINT NOT NULL COMMENT '用户唯一标识(雪花ID)',
    `username` VARCHAR(50) NOT NULL COMMENT '登录用户名(唯一)',
    `mobile` VARCHAR(20) NOT NULL COMMENT '手机号(唯一,登录凭证)',
    `password` VARCHAR(100) NOT NULL COMMENT '密码哈希值',
    `status` TINYINT DEFAULT 1 COMMENT '用户状态(0=禁用,1=正常,2=冻结)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_mobile` (`mobile`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基础信息表';

-- 2. 用户资料扩展表
CREATE TABLE `user_profile` (
    `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '用户昵称(显示名)',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `metadata` JSON DEFAULT NULL COMMENT '扩展信息JSON{email,real_name,location,bio...}',
    PRIMARY KEY (`user_id`),
    KEY `idx_nickname` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户资料扩展表';

-- 3. 用户钱包表
CREATE TABLE `user_wallet` (
    `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
    `balance` BIGINT DEFAULT 0 COMMENT '余额(分为单位,避免精度问题)',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本号(并发控制)',
    PRIMARY KEY (`user_id`),
    KEY `idx_balance` (`balance`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户钱包表';

-- 4. 统一交易流水表
CREATE TABLE `transaction` (
    `id` BIGINT NOT NULL COMMENT '交易记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `amount` BIGINT NOT NULL COMMENT '交易金额(正负表示收支)',
    `type` VARCHAR(20) NOT NULL COMMENT '交易类型(充值/消费/积分/退款)',
    `ref_id` VARCHAR(50) DEFAULT NULL COMMENT '关联业务ID(订单号/活动ID等)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_ref_id` (`ref_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一交易流水表';

-- 7. 用户关系表
CREATE TABLE `user_relation` (
    `id` BIGINT NOT NULL COMMENT '关系记录ID',
    `user_id` BIGINT NOT NULL COMMENT '发起用户ID',
    `target_id` BIGINT NOT NULL COMMENT '目标用户ID',
    `type` TINYINT NOT NULL COMMENT '关系类型(1=关注,2=拉黑,3=分组)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立关系时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_target_id` (`target_id`),
    KEY `idx_type` (`type`),
    UNIQUE KEY `uk_user_target_type` (`user_id`, `target_id`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关系表';

-- 外键约束
ALTER TABLE `user_profile` ADD CONSTRAINT `fk_user_profile_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
ALTER TABLE `user_wallet` ADD CONSTRAINT `fk_user_wallet_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
ALTER TABLE `transaction` ADD CONSTRAINT `fk_transaction_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL;
ALTER TABLE `user_relation` ADD CONSTRAINT `fk_user_relation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
ALTER TABLE `user_relation` ADD CONSTRAINT `fk_user_relation_target` FOREIGN KEY (`target_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

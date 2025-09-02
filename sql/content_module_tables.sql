-- ==========================================
-- 📱 内容模块 - 数据库表结构
-- 2张表：content, content_action
-- ==========================================

-- 5. 万能内容表(动态/活动/技能)
CREATE TABLE `content` (
    `id` BIGINT NOT NULL COMMENT '内容唯一ID',
    `user_id` BIGINT NOT NULL COMMENT '创建者ID',
    `type` TINYINT NOT NULL COMMENT '内容类型(1=动态,2=活动,3=技能)',
    `title` VARCHAR(200) NOT NULL COMMENT '内容标题',
    `data` JSON NOT NULL COMMENT '类型特定数据JSON',
    `status` TINYINT DEFAULT 1 COMMENT '内容状态(0=草稿,1=发布,2=下架)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='万能内容表(动态/活动/技能)';

-- 6. 内容行为统一表
CREATE TABLE `content_action` (
    `id` BIGINT NOT NULL COMMENT '行为记录ID',
    `content_id` BIGINT NOT NULL COMMENT '关联内容ID',
    `user_id` BIGINT NOT NULL COMMENT '操作用户ID',
    `action` TINYINT NOT NULL COMMENT '行为类型(1=点赞,2=评论,6=报名)',
    `data` JSON DEFAULT NULL COMMENT '行为扩展数据JSON',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '行为时间',
    PRIMARY KEY (`id`),
    KEY `idx_content_id` (`content_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_action` (`action`),
    KEY `idx_created_at` (`created_at`),
    UNIQUE KEY `uk_user_content_action` (`user_id`, `content_id`, `action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容行为统一表';

-- 外键约束 (需要引用user表，在完整系统中生效)
-- ALTER TABLE `content` ADD CONSTRAINT `fk_content_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL;
-- ALTER TABLE `content_action` ADD CONSTRAINT `fk_content_action_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`id`) ON DELETE CASCADE;
-- ALTER TABLE `content_action` ADD CONSTRAINT `fk_content_action_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

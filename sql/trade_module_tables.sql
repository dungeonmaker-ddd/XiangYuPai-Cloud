-- ==========================================
-- 💰 交易模块 - 数据库表结构
-- 1张表：service_order
-- ==========================================

-- 8. 服务订单表
CREATE TABLE `service_order` (
    `id` BIGINT NOT NULL COMMENT '订单唯一ID',
    `buyer_id` BIGINT NOT NULL COMMENT '买家用户ID',
    `seller_id` BIGINT NOT NULL COMMENT '卖家用户ID',
    `content_id` BIGINT NOT NULL COMMENT '关联技能内容ID',
    `amount` BIGINT NOT NULL COMMENT '订单金额(分)',
    `duration` INT DEFAULT NULL COMMENT '服务时长(小时)',
    `status` TINYINT DEFAULT 0 COMMENT '订单状态(0=待付款,3=已完成)',
    `data` JSON DEFAULT NULL COMMENT '订单扩展信息JSON',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    PRIMARY KEY (`id`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_content_id` (`content_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务订单表';

-- 外键约束 (需要引用user和content表，在完整系统中生效)
-- ALTER TABLE `service_order` ADD CONSTRAINT `fk_service_order_buyer` FOREIGN KEY (`buyer_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT;
-- ALTER TABLE `service_order` ADD CONSTRAINT `fk_service_order_seller` FOREIGN KEY (`seller_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT;
-- ALTER TABLE `service_order` ADD CONSTRAINT `fk_service_order_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`id`) ON DELETE RESTRICT;

-- ==========================================
-- 🏗️ XY相遇派核心系统 - 数据库表结构设计
-- 11张表实现完整功能：用户+内容+交易+聊天
-- 设计理念：计算优于存储，JSON灵活扩展
-- ==========================================

-- ===== 核心用户模块 (4张表) =====

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

-- ===== 内容模块 (3张表) =====

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

-- ===== 交易模块 (1张表) =====

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

-- ===== 聊天模块 (3张表) =====

-- 9. 聊天会话表
CREATE TABLE `chat_conversation` (
    `id` BIGINT NOT NULL COMMENT '会话唯一ID(雪花ID)',
    `type` TINYINT NOT NULL COMMENT '会话类型(1=私聊,2=群聊,3=系统通知)',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '会话标题(群聊名称,私聊可为空)',
    `creator_id` BIGINT DEFAULT NULL COMMENT '创建者ID(群主/发起人)',
    `metadata` JSON DEFAULT NULL COMMENT '扩展信息JSON{description,avatar,settings...}',
    `status` TINYINT DEFAULT 1 COMMENT '会话状态(0=已解散,1=正常,2=已归档)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_creator_id` (`creator_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- 10. 聊天消息表
CREATE TABLE `chat_message` (
    `id` BIGINT NOT NULL COMMENT '消息唯一ID',
    `conversation_id` BIGINT NOT NULL COMMENT '所属会话ID',
    `sender_id` BIGINT DEFAULT NULL COMMENT '发送者ID(NULL=系统消息)',
    `message_type` TINYINT NOT NULL COMMENT '消息类型(1=文本,2=图片,3=语音,4=视频,5=文件,6=系统通知)',
    `content` TEXT NOT NULL COMMENT '消息内容(文本/文件名/系统通知文本)',
    `media_data` JSON DEFAULT NULL COMMENT '媒体数据JSON{url,size,duration...}',
    `reply_to_id` BIGINT DEFAULT NULL COMMENT '回复的消息ID(引用回复)',
    `status` TINYINT DEFAULT 1 COMMENT '消息状态(0=已删除,1=正常,2=已撤回)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_message_type` (`message_type`),
    KEY `idx_reply_to_id` (`reply_to_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 11. 会话参与者表
CREATE TABLE `chat_participant` (
    `id` BIGINT NOT NULL COMMENT '参与记录ID',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '参与用户ID',
    `role` TINYINT DEFAULT 1 COMMENT '角色权限(1=成员,2=管理员,3=群主)',
    `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `last_read_time` DATETIME DEFAULT NULL COMMENT '最后已读时间(未读消息计算)',
    `status` TINYINT DEFAULT 1 COMMENT '参与状态(0=已退出,1=正常,2=已禁言)',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`),
    UNIQUE KEY `uk_conversation_user` (`conversation_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话参与者表';

-- ==========================================
-- 🔗 外键约束定义
-- ==========================================

-- 用户模块外键
ALTER TABLE `user_profile` ADD CONSTRAINT `fk_user_profile_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
ALTER TABLE `user_wallet` ADD CONSTRAINT `fk_user_wallet_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
ALTER TABLE `transaction` ADD CONSTRAINT `fk_transaction_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL;

-- 内容模块外键
ALTER TABLE `content` ADD CONSTRAINT `fk_content_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL;
ALTER TABLE `content_action` ADD CONSTRAINT `fk_content_action_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`id`) ON DELETE CASCADE;
ALTER TABLE `content_action` ADD CONSTRAINT `fk_content_action_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
ALTER TABLE `user_relation` ADD CONSTRAINT `fk_user_relation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
ALTER TABLE `user_relation` ADD CONSTRAINT `fk_user_relation_target` FOREIGN KEY (`target_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

-- 交易模块外键
ALTER TABLE `service_order` ADD CONSTRAINT `fk_service_order_buyer` FOREIGN KEY (`buyer_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT;
ALTER TABLE `service_order` ADD CONSTRAINT `fk_service_order_seller` FOREIGN KEY (`seller_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT;
ALTER TABLE `service_order` ADD CONSTRAINT `fk_service_order_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`id`) ON DELETE RESTRICT;

-- 聊天模块外键
ALTER TABLE `chat_conversation` ADD CONSTRAINT `fk_chat_conversation_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE SET NULL;
ALTER TABLE `chat_message` ADD CONSTRAINT `fk_chat_message_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `chat_conversation` (`id`) ON DELETE CASCADE;
ALTER TABLE `chat_message` ADD CONSTRAINT `fk_chat_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`) ON DELETE SET NULL;
ALTER TABLE `chat_message` ADD CONSTRAINT `fk_chat_message_reply` FOREIGN KEY (`reply_to_id`) REFERENCES `chat_message` (`id`) ON DELETE SET NULL;
ALTER TABLE `chat_participant` ADD CONSTRAINT `fk_chat_participant_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `chat_conversation` (`id`) ON DELETE CASCADE;
ALTER TABLE `chat_participant` ADD CONSTRAINT `fk_chat_participant_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

-- ==========================================
-- 📝 设计说明
-- ==========================================

/*
🏗️ 架构设计理念：

1. **模块化设计**
   - 4个核心模块：用户、内容、交易、聊天
   - 每个模块相对独立，支持微服务拆分

2. **计算优于存储**
   - 避免冗余字段，通过计算获取统计数据
   - 如：关注数、粉丝数、点赞数等动态计算

3. **JSON扩展设计**
   - metadata、data字段提供灵活扩展
   - 支持业务快速迭代，避免频繁修改表结构

4. **类型统一设计**
   - content表支持动态/活动/技能三种类型
   - content_action表统一处理各种行为
   - chat_conversation表统一处理各种会话

5. **关系约束策略**
   - CASCADE：组合关系，如用户-资料
   - SET NULL：聚合关系，保留历史数据
   - RESTRICT：重要业务约束，如订单

6. **性能优化设计**
   - 合理的索引设计
   - 金额使用分为单位(BIGINT)
   - 乐观锁支持并发控制
   - 时间字段支持范围查询

📊 表数量统计：
- 用户模块：4张表
- 内容模块：3张表  
- 交易模块：1张表
- 聊天模块：3张表
- 总计：11张表

🔗 关系类型：
- 组合关系：5个 (CASCADE DELETE)
- 聚合关系：6个 (SET NULL)
- 约束关系：3个 (RESTRICT)
*/

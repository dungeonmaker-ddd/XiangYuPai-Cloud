-- ==========================================
-- 🏗️ XY相遇派完整系统数据库 - 重置+初始化
-- 一键重置并创建所有模块数据库
-- 包含：用户、内容、交易、聊天 4个模块
-- ⚠️ 警告：此脚本会删除现有数据！
-- ==========================================

-- ===== 重置所有数据库 =====
DROP DATABASE IF EXISTS `xypai_user`;
DROP DATABASE IF EXISTS `xypai_content`;
DROP DATABASE IF EXISTS `xypai_trade`;
DROP DATABASE IF EXISTS `xypai_chat`;

-- ===== 用户模块数据库 =====
CREATE DATABASE `xypai_user` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `xypai_user`;

-- 1. 用户基础信息表
CREATE TABLE `user` (
    `id` BIGINT NOT NULL COMMENT '用户唯一标识(雪花ID)',
    `username` VARCHAR(50) NOT NULL COMMENT '登录用户名(唯一)',
    `mobile` VARCHAR(20) NOT NULL COMMENT '手机号(唯一,登录凭证)',
    `password` VARCHAR(100) DEFAULT NULL COMMENT '密码哈希值(短信注册可为空)',
    `status` TINYINT DEFAULT 1 COMMENT '用户状态(0=禁用,1=正常,2=冻结)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标志(0=正常,1=已删除)',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_mobile` (`mobile`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基础信息表';

-- 2. 用户资料扩展表
CREATE TABLE `user_profile` (
    `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '用户昵称(显示名)',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `metadata` JSON DEFAULT NULL COMMENT '扩展信息JSON{email,real_name,location,bio...}',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
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
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID(允许为空,用户删除后保留交易记录)',
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

-- 5. 用户关系表
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

-- 用户模块外键约束
ALTER TABLE `user_profile` ADD CONSTRAINT `fk_user_profile_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
ALTER TABLE `user_wallet` ADD CONSTRAINT `fk_user_wallet_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
ALTER TABLE `transaction` ADD CONSTRAINT `fk_transaction_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL;
ALTER TABLE `user_relation` ADD CONSTRAINT `fk_user_relation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
ALTER TABLE `user_relation` ADD CONSTRAINT `fk_user_relation_target` FOREIGN KEY (`target_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

-- ===== 内容模块数据库 =====
CREATE DATABASE `xypai_content` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `xypai_content`;

-- 1. 万能内容表(动态/活动/技能)
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

-- 2. 内容行为统一表
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

-- 内容模块外键约束
ALTER TABLE `content_action` ADD CONSTRAINT `fk_content_action_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`id`) ON DELETE CASCADE;

-- ===== 交易模块数据库 =====
CREATE DATABASE `xypai_trade` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `xypai_trade`;

-- 1. 服务订单表
CREATE TABLE `service_order` (
    `id` BIGINT NOT NULL COMMENT '订单唯一ID',
    `buyer_id` BIGINT NOT NULL COMMENT '买家用户ID',
    `seller_id` BIGINT NOT NULL COMMENT '卖家用户ID',
    `content_id` BIGINT NOT NULL COMMENT '关联技能内容ID',
    `amount` BIGINT NOT NULL COMMENT '订单金额(分)',
    `duration` INT DEFAULT NULL COMMENT '服务时长(小时)',
    `status` TINYINT DEFAULT 0 COMMENT '订单状态(0=待付款,1=已付款,2=服务中,3=已完成,4=已取消,5=已退款)',
    `data` JSON DEFAULT NULL COMMENT '订单扩展信息JSON',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_content_id` (`content_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务订单表';

-- ===== 聊天模块数据库 =====
CREATE DATABASE `xypai_chat` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `xypai_chat`;

-- 1. 聊天会话表
CREATE TABLE `chat_conversation` (
    `id` BIGINT NOT NULL COMMENT '会话唯一ID(雪花ID)',
    `type` TINYINT NOT NULL COMMENT '会话类型(1=私聊,2=群聊,3=系统通知,4=订单会话)',
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

-- 2. 聊天消息表
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

-- 3. 会话参与者表
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
    KEY `idx_last_read_time` (`last_read_time`),
    UNIQUE KEY `uk_conversation_user` (`conversation_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话参与者表';

-- 聊天模块外键约束
ALTER TABLE `chat_message` ADD CONSTRAINT `fk_chat_message_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `chat_conversation` (`id`) ON DELETE CASCADE;
ALTER TABLE `chat_message` ADD CONSTRAINT `fk_chat_message_reply` FOREIGN KEY (`reply_to_id`) REFERENCES `chat_message` (`id`) ON DELETE SET NULL;
ALTER TABLE `chat_participant` ADD CONSTRAINT `fk_chat_participant_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `chat_conversation` (`id`) ON DELETE CASCADE;

-- ===== 初始化Mock数据 - 完整业务场景 =====

-- ====================================
-- 🧑‍💼 用户模块 Mock 数据
-- ====================================
USE `xypai_user`;

-- 插入10个模拟用户（不同身份：学生、工程师、设计师、老师等）
INSERT INTO `user` (`id`, `username`, `mobile`, `password`, `status`) VALUES
(1001, 'alice_dev', '13800138001', '$2a$10$YVHGqq9L.8vfA3.SQ8W9i.YnKDQWkDqyJ3A5wFgZsE5JNfGULkGzi', 1),
(1002, 'bob_designer', '13800138002', '$2a$10$YVHGqq9L.8vfA3.SQ8W9i.YnKDQWkDqyJ3A5wFgZsE5JNfGULkGzi', 1),
(1003, 'charlie_student', '13800138003', '$2a$10$YVHGqq9L.8vfA3.SQ8W9i.YnKDQWkDqyJ3A5wFgZsE5JNfGULkGzi', 1),
(1004, 'diana_teacher', '13800138004', '$2a$10$YVHGqq9L.8vfA3.SQ8W9i.YnKDQWkDqyJ3A5wFgZsE5JNfGULkGzi', 1),
(1005, 'erik_freelancer', '13800138005', '$2a$10$YVHGqq9L.8vfA3.SQ8W9i.YnKDQWkDqyJ3A5wFgZsE5JNfGULkGzi', 1),
(1006, 'fiona_pm', '13800138006', '$2a$10$YVHGqq9L.8vfA3.SQ8W9i.YnKDQWkDqyJ3A5wFgZsE5JNfGULkGzi', 1),
(1007, 'george_analyst', '13800138007', '$2a$10$YVHGqq9L.8vfA3.SQ8W9i.YnKDQWkDqyJ3A5wFgZsE5JNfGULkGzi', 1),
(1008, 'helen_photographer', '13800138008', '$2a$10$YVHGqq9L.8vfA3.SQ8W9i.YnKDQWkDqyJ3A5wFgZsE5JNfGULkGzi', 1),
(1009, 'ivan_coach', '13800138009', '$2a$10$YVHGqq9L.8vfA3.SQ8W9i.YnKDQWkDqyJ3A5wFgZsE5JNfGULkGzi', 1),
(1010, 'julia_writer', '13800138010', '$2a$10$YVHGqq9L.8vfA3.SQ8W9i.YnKDQWkDqyJ3A5wFgZsE5JNfGULkGzi', 1);

-- 用户资料信息
INSERT INTO `user_profile` (`user_id`, `nickname`, `avatar`, `metadata`, `version`) VALUES
(1001, 'Alice·全栈开发', 'https://picsum.photos/200/200?random=1', '{"email": "alice@example.com", "location": "北京", "bio": "5年全栈开发经验，擅长React/Vue+Spring Boot", "age": 28, "profession": "软件工程师", "company": "字节跳动"}', 0),
(1002, 'Bob·UI设计师', 'https://picsum.photos/200/200?random=2', '{"email": "bob@example.com", "location": "上海", "bio": "专业UI/UX设计，服务过多家知名互联网公司", "age": 26, "profession": "UI设计师", "company": "美团"}', 0),
(1003, 'Charlie·计算机学生', 'https://picsum.photos/200/200?random=3', '{"email": "charlie@example.com", "location": "杭州", "bio": "浙大计算机在读，热爱编程和开源", "age": 21, "profession": "学生", "school": "浙江大学"}', 0),
(1004, 'Diana·编程老师', 'https://picsum.photos/200/200?random=4', '{"email": "diana@example.com", "location": "深圳", "bio": "10年编程教育经验，Python/Java专家", "age": 35, "profession": "编程讲师", "company": "腾讯学院"}', 0),
(1005, 'Erik·自由职业', 'https://picsum.photos/200/200?random=5', '{"email": "erik@example.com", "location": "成都", "bio": "自由开发者，专注移动端开发", "age": 30, "profession": "自由职业者", "skills": ["Flutter", "React Native"]}', 0),
(1006, 'Fiona·产品经理', 'https://picsum.photos/200/200?random=6', '{"email": "fiona@example.com", "location": "广州", "bio": "资深产品经理，擅长用户体验设计", "age": 32, "profession": "产品经理", "company": "网易"}', 0),
(1007, 'George·数据分析', 'https://picsum.photos/200/200?random=7', '{"email": "george@example.com", "location": "西安", "bio": "数据科学专家，精通Python和机器学习", "age": 29, "profession": "数据分析师", "company": "阿里巴巴"}', 0),
(1008, 'Helen·摄影师', 'https://picsum.photos/200/200?random=8', '{"email": "helen@example.com", "location": "厦门", "bio": "专业摄影师，擅长人像和商业摄影", "age": 27, "profession": "摄影师", "experience": "8年"}', 0),
(1009, 'Ivan·健身教练', 'https://picsum.photos/200/200?random=9', '{"email": "ivan@example.com", "location": "青岛", "bio": "国家级健身教练，专业减脂增肌指导", "age": 31, "profession": "健身教练", "certifications": ["ACSM", "NASM"]}', 0),
(1010, 'Julia·文案策划', 'https://picsum.photos/200/200?random=10', '{"email": "julia@example.com", "location": "南京", "bio": "资深文案策划，服务过多个知名品牌", "age": 28, "profession": "文案策划", "company": "奥美广告"}', 0);

-- 用户钱包（不同余额体现活跃度）
INSERT INTO `user_wallet` (`user_id`, `balance`, `version`) VALUES
(1001, 50000, 0),  -- Alice - 活跃用户
(1002, 38000, 0),  -- Bob - 中等活跃
(1003, 5000, 0),   -- Charlie - 学生，余额较少
(1004, 45000, 0),  -- Diana - 老师，收入稳定
(1005, 32000, 0),  -- Erik - 自由职业
(1006, 28000, 0),  -- Fiona - 产品经理
(1007, 41000, 0),  -- George - 数据分析师
(1008, 23000, 0),  -- Helen - 摄影师
(1009, 19000, 0),  -- Ivan - 健身教练
(1010, 15000, 0);  -- Julia - 文案策划

-- 用户关系（关注、好友关系）
INSERT INTO `user_relation` (`id`, `user_id`, `target_id`, `type`, `created_at`) VALUES
-- Alice的关注
(1, 1001, 1002, 1, '2024-12-15 10:00:00'),  -- Alice关注Bob
(2, 1001, 1004, 1, '2024-12-16 11:00:00'),  -- Alice关注Diana
(3, 1001, 1007, 1, '2024-12-17 12:00:00'),  -- Alice关注George
-- Bob的关注
(4, 1002, 1001, 1, '2024-12-15 15:00:00'),  -- Bob关注Alice（互关）
(5, 1002, 1008, 1, '2024-12-18 16:00:00'),  -- Bob关注Helen
(6, 1002, 1010, 1, '2024-12-19 17:00:00'),  -- Bob关注Julia
-- Charlie的关注（学生关注老师和前辈）
(7, 1003, 1001, 1, '2024-12-20 09:00:00'),  -- Charlie关注Alice
(8, 1003, 1004, 1, '2024-12-20 10:00:00'),  -- Charlie关注Diana
(9, 1003, 1007, 1, '2024-12-20 11:00:00'),  -- Charlie关注George
-- 其他关系
(10, 1004, 1003, 1, '2024-12-21 14:00:00'), -- Diana关注Charlie
(11, 1005, 1002, 1, '2024-12-22 15:00:00'), -- Erik关注Bob
(12, 1006, 1001, 1, '2024-12-23 16:00:00'), -- Fiona关注Alice
(13, 1009, 1008, 1, '2024-12-24 17:00:00'), -- Ivan关注Helen
(14, 1010, 1006, 1, '2024-12-25 18:00:00'); -- Julia关注Fiona

-- 交易流水记录（充值、消费、收入）
INSERT INTO `transaction` (`id`, `user_id`, `amount`, `type`, `ref_id`, `created_at`) VALUES
-- 充值记录
(1, 1001, 100000, '充值', 'RECHARGE_001', '2024-12-01 10:00:00'),
(2, 1002, 50000, '充值', 'RECHARGE_002', '2024-12-02 11:00:00'),
(3, 1004, 80000, '充值', 'RECHARGE_003', '2024-12-03 12:00:00'),
-- 技能服务收入
(4, 1001, 15000, '收入', 'ORDER_4001', '2024-12-20 14:00:00'),
(5, 1002, 12000, '收入', 'ORDER_4002', '2024-12-21 15:00:00'),
(6, 1004, 20000, '收入', 'ORDER_4003', '2024-12-22 16:00:00'),
-- 技能服务支出
(7, 1003, -15000, '消费', 'ORDER_4001', '2024-12-20 14:00:00'),
(8, 1006, -12000, '消费', 'ORDER_4002', '2024-12-21 15:00:00'),
(9, 1003, -20000, '消费', 'ORDER_4003', '2024-12-22 16:00:00'),
-- 活动相关消费
(10, 1005, -3000, '消费', 'ACTIVITY_001', '2024-12-25 18:00:00'),
(11, 1009, -2500, '消费', 'ACTIVITY_002', '2024-12-26 19:00:00');

-- ====================================
-- 📱 内容模块 Mock 数据
-- ====================================
USE `xypai_content`;

-- 多样化内容：动态、活动、技能服务
INSERT INTO `content` (`id`, `user_id`, `type`, `title`, `data`, `status`, `created_at`) VALUES
-- 动态内容 (type=1)
(2001, 1001, 1, '今天完成了一个React项目', '{"text": "刚刚完成了一个电商项目的前端开发，使用React+TypeScript+Ant Design，感觉很有成就感！", "images": ["https://picsum.photos/400/300?random=101"], "tags": ["React", "前端开发", "TypeScript"]}', 1, '2024-12-28 09:00:00'),
(2002, 1002, 1, '设计作品分享', '{"text": "分享一个最近设计的App界面，简约风格，大家觉得怎么样？", "images": ["https://picsum.photos/400/600?random=102", "https://picsum.photos/400/600?random=103"], "tags": ["UI设计", "App设计", "简约风格"]}', 1, '2024-12-28 10:30:00'),
(2003, 1003, 1, '学习进度分享', '{"text": "今天学习了SpringBoot的AOP，终于理解了面向切面编程的精髓！", "images": [], "tags": ["SpringBoot", "AOP", "学习笔记"]}', 1, '2024-12-28 14:00:00'),
(2004, 1008, 1, '摄影作品分享', '{"text": "周末在西湖拍的几张照片，杭州的冬天也很美", "images": ["https://picsum.photos/600/400?random=104", "https://picsum.photos/600/400?random=105"], "tags": ["摄影", "风景", "杭州西湖"]}', 1, '2024-12-28 16:00:00'),

-- 活动内容 (type=2)
(2101, 1004, 2, '周末编程学习沙龙', '{"description": "本周六下午2点，Java学习沙龙，大家一起交流学习心得，分享编程经验", "location": "深圳科技园咖啡厅", "time": "2025-01-04 14:00", "maxParticipants": 12, "fee": 0, "requirements": "有一定Java基础", "contact": "微信：diana_teacher"}', 1, '2024-12-27 09:00:00'),
(2102, 1006, 2, '产品设计思维工作坊', '{"description": "产品设计思维训练营，学习用户体验设计方法", "location": "广州天河区创客空间", "time": "2025-01-05 10:00", "maxParticipants": 20, "fee": 299, "requirements": "对产品设计有兴趣", "contact": "电话：13800138006"}', 1, '2024-12-27 11:00:00'),
(2103, 1009, 2, '新年健身挑战', '{"description": "新年21天健身挑战，每天1小时训练，塑造更好的自己", "location": "青岛奥体中心", "time": "2025-01-01 08:00", "maxParticipants": 30, "fee": 399, "requirements": "身体健康，无重大疾病", "contact": "微信：ivan_coach"}', 1, '2024-12-26 15:00:00'),
(2104, 1008, 2, '摄影外拍活动', '{"description": "厦门环岛路摄影外拍，拍摄海景人像，提供专业指导", "location": "厦门环岛路", "time": "2025-01-06 08:00", "maxParticipants": 8, "fee": 199, "requirements": "自备相机", "contact": "QQ：helen_photo"}', 1, '2024-12-27 20:00:00'),

-- 技能服务 (type=3)
(2201, 1001, 3, 'React全栈开发一对一辅导', '{"description": "提供React+Node.js全栈开发指导，包括项目实战经验分享", "price": 15000, "duration": 2, "skills": ["React", "Node.js", "全栈开发"], "portfolio": ["https://github.com/alice/project1", "https://alice-portfolio.com"], "availability": "工作日晚上7-9点，周末全天"}', 1, '2024-12-25 10:00:00'),
(2202, 1002, 3, 'UI设计&Figma使用教学', '{"description": "专业UI设计指导，Figma工具使用，设计思维培养", "price": 12000, "duration": 1.5, "skills": ["UI设计", "Figma", "设计思维"], "portfolio": ["https://bobdesign.behance.net", "https://figma.com/@bob"], "availability": "周一到周五晚上，周末上午"}', 1, '2024-12-25 14:00:00'),
(2203, 1004, 3, 'Java编程从入门到精通', '{"description": "系统性Java编程教学，从基础语法到Spring Boot项目实战", "price": 20000, "duration": 3, "skills": ["Java", "Spring Boot", "数据库"], "portfolio": ["10年教学经验", "培养学员500+"], "availability": "周末全天，工作日晚上"}', 1, '2024-12-25 16:00:00'),
(2204, 1007, 3, 'Python数据分析实战', '{"description": "Python数据分析项目实战，包括数据清洗、可视化、机器学习", "price": 18000, "duration": 2.5, "skills": ["Python", "数据分析", "机器学习"], "portfolio": ["阿里巴巴数据分析专家", "GitHub开源项目"], "availability": "工作日晚上8-10点"}', 1, '2024-12-25 18:00:00'),
(2205, 1008, 3, '人像摄影技巧指导', '{"description": "专业人像摄影技巧教学，包括构图、用光、后期处理", "price": 8000, "duration": 1, "skills": ["人像摄影", "后期处理", "Lightroom"], "portfolio": ["8年摄影经验", "服务客户1000+"], "availability": "周末全天"}', 1, '2024-12-26 10:00:00'),
(2206, 1009, 3, '私人健身指导', '{"description": "一对一健身指导，制定专属健身计划，包括饮食建议", "price": 30000, "duration": 1, "skills": ["健身指导", "营养搭配", "体能训练"], "portfolio": ["国家级教练认证", "成功案例200+"], "availability": "每天早上6-8点，晚上7-9点"}', 1, '2024-12-26 12:00:00'),
(2207, 1010, 3, '品牌文案策划服务', '{"description": "专业品牌文案策划，包括文案写作、营销策略制定", "price": 25000, "duration": 2, "skills": ["文案策划", "品牌营销", "创意策略"], "portfolio": ["奥美广告5年经验", "服务知名品牌50+"], "availability": "工作日下午2-6点"}', 1, '2024-12-26 14:00:00');

-- 内容互动行为
INSERT INTO `content_action` (`id`, `content_id`, `user_id`, `action`, `data`, `created_at`) VALUES
-- 对动态的点赞和评论
(3001, 2001, 1002, 1, NULL, '2024-12-28 09:15:00'),  -- Bob给Alice点赞
(3002, 2001, 1003, 1, NULL, '2024-12-28 09:30:00'),  -- Charlie给Alice点赞
(3003, 2001, 1003, 2, '{"comment": "学习了！期待看到完整项目代码"}', '2024-12-28 09:35:00'),
(3004, 2001, 1007, 2, '{"comment": "React+TS是很好的技术栈选择"}', '2024-12-28 10:00:00'),

(3005, 2002, 1001, 1, NULL, '2024-12-28 10:45:00'),  -- Alice给Bob点赞
(3006, 2002, 1006, 1, NULL, '2024-12-28 11:00:00'),  -- Fiona给Bob点赞
(3007, 2002, 1006, 2, '{"comment": "设计风格很棒，简约而不简单！"}', '2024-12-28 11:05:00'),

(3008, 2003, 1001, 1, NULL, '2024-12-28 14:15:00'),
(3009, 2003, 1004, 2, '{"comment": "学习态度很好，继续加油！"}', '2024-12-28 14:30:00'),

-- 对活动的报名
(3010, 2101, 1003, 6, '{"message": "我想参加学习沙龙，已有半年Java经验", "contact": "微信：charlie_student"}', '2024-12-27 10:00:00'),
(3011, 2101, 1001, 6, '{"message": "很感兴趣，想和大家交流学习", "contact": "微信：alice_dev"}', '2024-12-27 11:00:00'),
(3012, 2102, 1002, 6, '{"message": "想学习产品设计思维，报名参加", "contact": "电话：13800138002"}', '2024-12-27 12:00:00'),
(3013, 2103, 1005, 6, '{"message": "新年新开始，想挑战一下自己", "contact": "微信：erik_freelancer"}', '2024-12-26 16:00:00'),
(3014, 2104, 1002, 6, '{"message": "想学习人像摄影技巧", "contact": "QQ：bob_designer"}', '2024-12-27 21:00:00'),

-- 对技能服务的咨询和收藏
(3015, 2201, 1003, 2, '{"comment": "请问可以从零基础开始学习吗？"}', '2024-12-25 11:00:00'),
(3016, 2201, 1003, 4, '{"category": "编程学习"}', '2024-12-25 11:05:00'),
(3017, 2202, 1010, 2, '{"comment": "正好想学Figma，价格很合理"}', '2024-12-25 15:00:00'),
(3018, 2203, 1003, 4, '{"category": "编程学习"}', '2024-12-25 17:00:00'),
(3019, 2204, 1005, 2, '{"comment": "对机器学习很感兴趣，请问需要什么基础？"}', '2024-12-25 19:00:00'),
(3020, 2205, 1002, 2, '{"comment": "想学习人像摄影构图技巧"}', '2024-12-26 11:00:00'),
(3021, 2206, 1006, 4, '{"category": "健身运动"}', '2024-12-26 13:00:00'),
(3022, 2207, 1006, 2, '{"comment": "我们公司正好需要品牌文案策划"}', '2024-12-26 15:00:00');

-- ====================================
-- 💰 交易模块 Mock 数据
-- ====================================
USE `xypai_trade`;

-- 真实的订单数据（与内容和用户关联）
INSERT INTO `service_order` (`id`, `buyer_id`, `seller_id`, `content_id`, `amount`, `duration`, `status`, `data`, `created_at`, `updated_at`) VALUES
-- 已完成的订单
(4001, 1003, 1001, 2201, 15000, 2, 3, '{"requirements": "希望从React基础开始学习，目标是能独立开发项目", "contact": "微信：charlie_student", "appointmentTime": "2024-12-20 19:00", "completionTime": "2024-12-20 21:00", "rating": 5, "review": "Alice老师讲解很详细，受益匪浅！"}', '2024-12-19 10:00:00', '2024-12-20 21:30:00'),

(4002, 1006, 1002, 2202, 12000, 1.5, 3, '{"requirements": "想学习Figma高级功能和设计规范", "contact": "电话：13800138006", "appointmentTime": "2024-12-21 14:00", "completionTime": "2024-12-21 15:30", "rating": 5, "review": "Bob的设计思维很棒，学到了很多实用技巧"}', '2024-12-20 11:00:00', '2024-12-21 16:00:00'),

(4003, 1003, 1004, 2203, 20000, 3, 3, '{"requirements": "想系统学习Java和Spring Boot", "contact": "微信：charlie_student", "appointmentTime": "2024-12-22 14:00", "completionTime": "2024-12-22 17:00", "rating": 5, "review": "Diana老师经验丰富，课程设计很合理"}', '2024-12-21 12:00:00', '2024-12-22 17:30:00'),

-- 进行中的订单
(4004, 1005, 1007, 2204, 18000, 2.5, 2, '{"requirements": "想学习数据分析和机器学习在实际项目中的应用", "contact": "微信：erik_freelancer", "appointmentTime": "2024-12-29 20:00", "progressNotes": "已完成数据清洗部分，下次学习可视化"}', '2024-12-28 15:00:00', '2024-12-29 22:30:00'),

(4005, 1002, 1008, 2205, 8000, 1, 2, '{"requirements": "想学习人像摄影的构图和用光技巧", "contact": "QQ：bob_designer", "appointmentTime": "2024-12-29 10:00", "progressNotes": "已学习基础构图技巧，下次学习用光"}', '2024-12-28 20:00:00', '2024-12-29 12:00:00'),

-- 已付款待开始的订单
(4006, 1006, 1009, 2206, 30000, 1, 1, '{"requirements": "想制定专业的减脂健身计划", "contact": "电话：13800138006", "appointmentTime": "2025-01-02 07:00", "notes": "目标减重10kg，提高身体素质"}', '2024-12-29 09:00:00', '2024-12-29 09:00:00'),

(4007, 1005, 1010, 2207, 25000, 2, 1, '{"requirements": "为初创公司制定品牌文案策略", "contact": "微信：erik_freelancer", "appointmentTime": "2025-01-03 14:00", "projectDetails": "科技类创业公司，目标用户是年轻程序员"}', '2024-12-29 11:00:00', '2024-12-29 11:00:00'),

-- 待付款的订单
(4008, 1001, 1004, 2203, 20000, 3, 0, '{"requirements": "想深入学习Spring Boot微服务架构", "contact": "微信：alice_dev", "appointmentTime": "2025-01-05 14:00", "notes": "有一定Java基础，想学习企业级开发"}', '2024-12-29 16:00:00', '2024-12-29 16:00:00'),

-- 已取消的订单
(4009, 1003, 1002, 2202, 12000, 1.5, 4, '{"requirements": "想学习UI设计基础", "contact": "微信：charlie_student", "cancelReason": "时间冲突，无法参加", "cancelTime": "2024-12-28 10:00"}', '2024-12-27 14:00:00', '2024-12-28 10:00:00');

-- ====================================
-- 💬 聊天模块 Mock 数据
-- ====================================
USE `xypai_chat`;

-- 多样化的会话类型
INSERT INTO `chat_conversation` (`id`, `type`, `title`, `creator_id`, `metadata`, `status`, `created_at`, `updated_at`) VALUES
-- 私聊会话
(5001, 1, NULL, 1003, '{"lastMessagePreview": "好的，期待下次课程！", "participantIds": [1001, 1003]}', 1, '2024-12-19 09:30:00', '2024-12-20 21:35:00'),
(5002, 1, NULL, 1006, '{"lastMessagePreview": "设计文件我已经准备好了", "participantIds": [1002, 1006]}', 1, '2024-12-20 10:30:00', '2024-12-21 15:45:00'),
(5003, 1, NULL, 1005, '{"lastMessagePreview": "明天见！", "participantIds": [1007, 1005]}', 1, '2024-12-28 14:30:00', '2024-12-29 22:35:00'),

-- 群聊会话
(5004, 2, '前端技术交流群', 1001, '{"description": "React、Vue等前端技术交流讨论", "avatar": "https://picsum.photos/100/100?random=201", "maxMembers": 50, "memberCount": 8}', 1, '2024-12-20 09:00:00', '2024-12-28 20:15:00'),
(5005, 2, '设计师联盟', 1002, '{"description": "UI/UX设计师交流分享平台", "avatar": "https://picsum.photos/100/100?random=202", "maxMembers": 30, "memberCount": 6}', 1, '2024-12-22 10:00:00', '2024-12-28 18:30:00'),
(5006, 2, 'Java学习小组', 1004, '{"description": "Java编程学习互助小组", "avatar": "https://picsum.photos/100/100?random=203", "maxMembers": 20, "memberCount": 5}', 1, '2024-12-25 11:00:00', '2024-12-28 22:00:00'),

-- 订单自动创建的会话
(5007, 4, NULL, NULL, '{"orderId": 4001, "orderTitle": "React全栈开发一对一辅导", "autoCreated": true, "participantIds": [1001, 1003]}', 1, '2024-12-19 10:00:00', '2024-12-20 21:30:00'),
(5008, 4, NULL, NULL, '{"orderId": 4002, "orderTitle": "UI设计&Figma使用教学", "autoCreated": true, "participantIds": [1002, 1006]}', 1, '2024-12-20 11:00:00', '2024-12-21 16:00:00'),
(5009, 4, NULL, NULL, '{"orderId": 4004, "orderTitle": "Python数据分析实战", "autoCreated": true, "participantIds": [1007, 1005]}', 1, '2024-12-28 15:00:00', '2024-12-29 22:30:00'),

-- 系统通知会话
(5010, 3, '系统通知', NULL, '{"description": "系统重要通知和公告", "autoCreated": true}', 1, '2024-12-01 00:00:00', '2024-12-29 12:00:00');

-- 丰富的聊天消息
INSERT INTO `chat_message` (`id`, `conversation_id`, `sender_id`, `message_type`, `content`, `media_data`, `reply_to_id`, `status`, `created_at`) VALUES
-- 订单会话中的消息 (5007 - Alice和Charlie的React课程)
(6001, 5007, 1003, 1, '你好Alice老师，我是Charlie，明天的React课程我准备好了', NULL, NULL, 1, '2024-12-19 10:05:00'),
(6002, 5007, 1001, 1, '你好Charlie！很高兴为你授课，请问你现在的前端基础怎么样？', NULL, 6001, 1, '2024-12-19 10:08:00'),
(6003, 5007, 1003, 1, '我学过HTML、CSS和基础JavaScript，但对React还是新手', NULL, NULL, 1, '2024-12-19 10:10:00'),
(6004, 5007, 1001, 1, '好的，那我们明天从React基础概念开始，我会发一些预习资料给你', NULL, NULL, 1, '2024-12-19 10:12:00'),
(6005, 5007, 1001, 5, 'React入门资料.pdf', '{"url": "https://files.example.com/react-basics.pdf", "originalName": "React入门资料.pdf", "size": 2048000}', NULL, 1, '2024-12-19 10:15:00'),
(6006, 5007, 1003, 1, '收到了，谢谢老师！我会认真预习的', NULL, NULL, 1, '2024-12-19 10:16:00'),
(6007, 5007, 1001, 1, '课程结束了，Charlie你今天表现很好！', NULL, NULL, 1, '2024-12-20 21:00:00'),
(6008, 5007, 1003, 1, '谢谢老师！学到了很多，React确实很强大', NULL, NULL, 1, '2024-12-20 21:02:00'),
(6009, 5007, 1003, 1, '好的，期待下次课程！', NULL, NULL, 1, '2024-12-20 21:35:00'),

-- UI设计课程会话 (5008 - Bob和Fiona)
(6010, 5008, 1006, 1, 'Hi Bob，我是Fiona，对你的UI设计课程很感兴趣', NULL, NULL, 1, '2024-12-20 11:05:00'),
(6011, 5008, 1002, 1, '你好Fiona！我看到你是产品经理，这对学习UI设计很有帮助', NULL, 6010, 1, '2024-12-20 11:08:00'),
(6012, 5008, 1006, 1, '是的，想提升设计能力，更好地和设计师沟通', NULL, NULL, 1, '2024-12-20 11:10:00'),
(6013, 5008, 1002, 1, '那我们明天重点讲设计规范和组件化思维', NULL, NULL, 1, '2024-12-20 11:12:00'),
(6014, 5008, 1006, 1, '设计文件我已经准备好了', NULL, NULL, 1, '2024-12-21 15:45:00'),

-- 前端技术交流群 (5004)
(6015, 5004, 1001, 1, '欢迎大家加入前端技术交流群！', NULL, NULL, 1, '2024-12-20 09:05:00'),
(6016, 5004, 1002, 1, '谢谢Alice建群，虽然我是设计师，但也想了解前端技术', NULL, NULL, 1, '2024-12-20 09:10:00'),
(6017, 5004, 1003, 1, '正在学React，请大家多多指教', NULL, NULL, 1, '2024-12-20 09:15:00'),
(6018, 5004, 1005, 1, '我主要做Flutter开发，也可以交流跨端技术', NULL, NULL, 1, '2024-12-20 09:20:00'),
(6019, 5004, 1001, 1, '今天分享一个React性能优化的技巧', NULL, NULL, 1, '2024-12-28 20:00:00'),
(6020, 5004, 1001, 2, '使用React.memo和useMemo可以有效避免不必要的重渲染', '{"url": "https://example.com/code-snippet.png", "size": 156800}', 6019, 1, '2024-12-28 20:02:00'),
(6021, 5004, 1003, 1, '学习了！这个技巧很实用', NULL, 6020, 1, '2024-12-28 20:05:00'),
(6022, 5004, 1005, 1, 'React的优化和Flutter的有相似之处', NULL, NULL, 1, '2024-12-28 20:15:00'),

-- Java学习小组 (5006)
(6023, 5006, 1004, 1, '建立这个Java学习小组，希望大家一起进步', NULL, NULL, 1, '2024-12-25 11:05:00'),
(6024, 5006, 1003, 1, '谢谢Diana老师！正好在学Java', NULL, NULL, 1, '2024-12-25 11:10:00'),
(6025, 5006, 1001, 1, '虽然我主要做前端，但Java后端也会一些，可以交流', NULL, NULL, 1, '2024-12-25 11:15:00'),
(6026, 5006, 1004, 1, '今天我们讨论一下Spring Boot的自动配置原理', NULL, NULL, 1, '2024-12-28 21:45:00'),
(6027, 5006, 1003, 1, '这个我还不太理解，请老师详细讲解', NULL, 6026, 1, '2024-12-28 21:50:00'),
(6028, 5006, 1004, 1, '简单来说，就是通过条件注解实现按需加载', NULL, NULL, 1, '2024-12-28 22:00:00'),

-- 数据分析课程进行中 (5009 - George和Erik)
(6029, 5009, 1005, 1, 'George老师，我已经准备好学习数据分析了', NULL, NULL, 1, '2024-12-28 15:05:00'),
(6030, 5009, 1007, 1, 'Erik你好！我看你是做移动端开发的，学数据分析是为了什么？', NULL, 6029, 1, '2024-12-28 15:08:00'),
(6031, 5009, 1005, 1, '想分析App的用户行为数据，优化产品', NULL, NULL, 1, '2024-12-28 15:10:00'),
(6032, 5009, 1007, 1, '很好的想法！那我们从数据清洗开始', NULL, NULL, 1, '2024-12-28 15:12:00'),
(6033, 5009, 1005, 1, '今天的数据清洗课程很有用，学会了pandas的高级用法', NULL, NULL, 1, '2024-12-29 22:30:00'),
(6034, 5009, 1007, 1, '明天见！', NULL, NULL, 1, '2024-12-29 22:35:00'),

-- 系统通知消息 (5010)
(6035, 5010, NULL, 6, '🎉 欢迎来到XY相遇派！在这里你可以分享技能、学习新知识、结识新朋友', '{"notificationType": "welcome", "priority": "normal"}', NULL, 1, '2024-12-01 00:05:00'),
(6036, 5010, NULL, 6, '📢 平台新增了技能认证功能，完成认证可获得更多曝光机会', '{"notificationType": "feature", "priority": "high"}', NULL, 1, '2024-12-15 10:00:00'),
(6037, 5010, NULL, 6, '🔔 你有新的订单消息，请及时查看', '{"notificationType": "order", "priority": "high", "relatedOrderId": 4008}', NULL, 1, '2024-12-29 12:00:00');

-- 会话参与者
INSERT INTO `chat_participant` (`id`, `conversation_id`, `user_id`, `role`, `join_time`, `last_read_time`, `status`) VALUES
-- 私聊参与者
(7001, 5001, 1001, 1, '2024-12-19 09:30:00', '2024-12-20 21:35:00', 1),
(7002, 5001, 1003, 1, '2024-12-19 09:30:00', '2024-12-20 21:35:00', 1),
(7003, 5002, 1002, 1, '2024-12-20 10:30:00', '2024-12-21 15:45:00', 1),
(7004, 5002, 1006, 1, '2024-12-20 10:30:00', '2024-12-21 15:45:00', 1),
(7005, 5003, 1007, 1, '2024-12-28 14:30:00', '2024-12-29 22:35:00', 1),
(7006, 5003, 1005, 1, '2024-12-28 14:30:00', '2024-12-29 22:35:00', 1),

-- 前端技术交流群参与者
(7007, 5004, 1001, 3, '2024-12-20 09:00:00', '2024-12-28 20:15:00', 1),  -- Alice是群主
(7008, 5004, 1002, 1, '2024-12-20 09:10:00', '2024-12-28 18:30:00', 1),
(7009, 5004, 1003, 1, '2024-12-20 09:15:00', '2024-12-28 20:10:00', 1),
(7010, 5004, 1005, 1, '2024-12-20 09:20:00', '2024-12-28 20:15:00', 1),
(7011, 5004, 1006, 1, '2024-12-22 10:30:00', '2024-12-28 15:00:00', 1),
(7012, 5004, 1007, 1, '2024-12-23 14:00:00', '2024-12-28 11:00:00', 1),

-- 设计师联盟参与者
(7013, 5005, 1002, 3, '2024-12-22 10:00:00', '2024-12-28 18:30:00', 1),  -- Bob是群主
(7014, 5005, 1001, 1, '2024-12-22 10:15:00', '2024-12-28 10:00:00', 1),
(7015, 5005, 1006, 1, '2024-12-22 10:20:00', '2024-12-28 16:00:00', 1),
(7016, 5005, 1008, 1, '2024-12-22 11:00:00', '2024-12-28 12:00:00', 1),
(7017, 5005, 1010, 1, '2024-12-23 15:00:00', '2024-12-27 20:00:00', 1),

-- Java学习小组参与者
(7018, 5006, 1004, 3, '2024-12-25 11:00:00', '2024-12-28 22:00:00', 1),  -- Diana是群主
(7019, 5006, 1001, 1, '2024-12-25 11:15:00', '2024-12-28 21:55:00', 1),
(7020, 5006, 1003, 1, '2024-12-25 11:10:00', '2024-12-28 22:00:00', 1),
(7021, 5006, 1007, 1, '2024-12-26 09:00:00', '2024-12-28 15:00:00', 1),

-- 订单会话参与者
(7022, 5007, 1001, 1, '2024-12-19 10:00:00', '2024-12-20 21:30:00', 1),  -- Alice
(7023, 5007, 1003, 1, '2024-12-19 10:00:00', '2024-12-20 21:35:00', 1),  -- Charlie
(7024, 5008, 1002, 1, '2024-12-20 11:00:00', '2024-12-21 16:00:00', 1),  -- Bob
(7025, 5008, 1006, 1, '2024-12-20 11:00:00', '2024-12-21 15:45:00', 1),  -- Fiona
(7026, 5009, 1007, 1, '2024-12-28 15:00:00', '2024-12-29 22:35:00', 1),  -- George
(7027, 5009, 1005, 1, '2024-12-28 15:00:00', '2024-12-29 22:30:00', 1),  -- Erik

-- 系统通知参与者（所有用户都能看到）
(7028, 5010, 1001, 1, '2024-12-01 00:00:00', '2024-12-29 08:00:00', 1),
(7029, 5010, 1002, 1, '2024-12-01 00:00:00', '2024-12-28 18:00:00', 1),
(7030, 5010, 1003, 1, '2024-12-01 00:00:00', '2024-12-29 10:00:00', 1),
(7031, 5010, 1004, 1, '2024-12-01 00:00:00', '2024-12-28 22:00:00', 1),
(7032, 5010, 1005, 1, '2024-12-01 00:00:00', '2024-12-29 12:00:00', 1),
(7033, 5010, 1006, 1, '2024-12-01 00:00:00', '2024-12-29 11:00:00', 1),
(7034, 5010, 1007, 1, '2024-12-01 00:00:00', '2024-12-28 15:00:00', 1),
(7035, 5010, 1008, 1, '2024-12-01 00:00:00', '2024-12-28 12:00:00', 1),
(7036, 5010, 1009, 1, '2024-12-01 00:00:00', '2024-12-28 19:00:00', 1),
(7037, 5010, 1010, 1, '2024-12-01 00:00:00', '2024-12-27 20:00:00', 1);

-- ==========================================
-- 📝 重置+初始化完成提示
-- ==========================================
SELECT '🎉 XY相遇派数据库重置+初始化完成！' AS message,
       '🔄 已重置所有数据库' AS reset_status,
       '✅ 用户模块: xypai_user (5张表)' AS user_module,
       '✅ 内容模块: xypai_content (2张表)' AS content_module,
       '✅ 交易模块: xypai_trade (1张表)' AS trade_module,
       '✅ 聊天模块: xypai_chat (3张表)' AS chat_module,
       '📊 测试数据已初始化' AS test_data;

-- ========================================
-- XyPai APP 核心用户表设计 (YAGNI原则)
-- 版本: v1.0
-- 创建时间: 2025-01-28  
-- 描述: 只包含必要的用户和用户资料表
-- 原则: YAGNI - 你不会需要它
-- ========================================

-- 使用xypai_app数据库
USE
`xypai_app`;

-- ========================================
-- 1. APP用户基础表 (核心必需)
-- ========================================

DROP TABLE IF EXISTS `app_user`;

CREATE TABLE `app_user`
(
    `user_id`         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `mobile`          VARCHAR(11) NOT NULL UNIQUE COMMENT '手机号（唯一）',
    `username`        VARCHAR(30)  DEFAULT NULL COMMENT '用户名（可选）',
    `nickname`        VARCHAR(30) NOT NULL COMMENT '昵称',
    `avatar`          VARCHAR(200) DEFAULT NULL COMMENT '头像URL',
    `gender`          TINYINT      DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    `birth_date`      DATE         DEFAULT NULL COMMENT '生日',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态：1-正常 0-禁用',
    `register_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `last_login_time` DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    `client_type`     VARCHAR(10)  DEFAULT 'app' COMMENT '客户端类型：web/app/mini',

    -- 索引设计
    INDEX             `idx_mobile` (`mobile`),
    INDEX             `idx_status` (`status`),
    INDEX             `idx_client_type` (`client_type`),
    INDEX             `idx_register_time` (`register_time`),

    -- 唯一约束
    UNIQUE KEY `uk_mobile` (`mobile`),
    UNIQUE KEY `uk_username` (`username`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='APP用户基础信息表';

-- ========================================
-- 2. APP用户资料表 (扩展信息)
-- ========================================

DROP TABLE IF EXISTS `app_user_profile`;

CREATE TABLE `app_user_profile`
(
    `profile_id`         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '资料ID',
    `user_id`            BIGINT NOT NULL COMMENT '用户ID',
    `real_name`          VARCHAR(50)   DEFAULT NULL COMMENT '真实姓名',
    `id_card`            VARCHAR(18)   DEFAULT NULL COMMENT '身份证号',
    `email`              VARCHAR(100)  DEFAULT NULL COMMENT '邮箱',
    `qq`                 VARCHAR(20)   DEFAULT NULL COMMENT 'QQ号',
    `wechat`             VARCHAR(50)   DEFAULT NULL COMMENT '微信号',
    `occupation`         VARCHAR(50)   DEFAULT NULL COMMENT '职业',
    `company`            VARCHAR(100)  DEFAULT NULL COMMENT '公司',
    `education`          TINYINT       DEFAULT NULL COMMENT '学历：1-初中 2-高中 3-大专 4-本科 5-硕士 6-博士',
    `income_level`       TINYINT       DEFAULT NULL COMMENT '收入水平：1-3K以下 2-3-5K 3-5-8K 4-8-15K 5-15K以上',
    `bio`                VARCHAR(500)  DEFAULT NULL COMMENT '个人简介',
    `interests`          VARCHAR(200)  DEFAULT NULL COMMENT '兴趣爱好（逗号分隔）',
    `height`             DECIMAL(5, 2) DEFAULT NULL COMMENT '身高（厘米）',
    `weight`             DECIMAL(5, 2) DEFAULT NULL COMMENT '体重（公斤）',
    `location`           VARCHAR(100)  DEFAULT NULL COMMENT '常居地',
    `timezone`           VARCHAR(50)   DEFAULT 'Asia/Shanghai' COMMENT '时区',
    `language`           VARCHAR(10)   DEFAULT 'zh-CN' COMMENT '语言偏好',
    `notification_email` TINYINT       DEFAULT 1 COMMENT '邮件通知：1-开启 0-关闭',
    `notification_sms`   TINYINT       DEFAULT 1 COMMENT '短信通知：1-开启 0-关闭',
    `notification_push`  TINYINT       DEFAULT 1 COMMENT '推送通知：1-开启 0-关闭',
    `privacy_level`      TINYINT       DEFAULT 1 COMMENT '隐私级别：1-公开 2-好友可见 3-仅自己',
    `create_time`        DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 外键约束
    FOREIGN KEY (`user_id`) REFERENCES `app_user` (`user_id`) ON DELETE CASCADE,

    -- 索引设计
    UNIQUE KEY `uk_user_id` (`user_id`),
    INDEX                `idx_real_name` (`real_name`),
    INDEX                `idx_email` (`email`),
    INDEX                `idx_create_time` (`create_time`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='APP用户详细资料表';

-- ========================================
-- 3. 初始化测试数据
-- ========================================

-- 插入测试用户
INSERT INTO `app_user` (`mobile`, `username`, `nickname`, `avatar`, `gender`,
                        `birth_date`, `status`, `client_type`)
VALUES ('13888888888',
        'testuser',
        '测试用户',
        'https://example.com/avatar/default.jpg',
        1,
        '1990-01-01',
        1,
        'app'),
       ('13999999999',
        'vipuser',
        'VIP用户',
        'https://example.com/avatar/vip.jpg',
        2,
        '1995-05-15',
        1,
        'mini');

-- 插入对应的用户资料
INSERT INTO `app_user_profile` (`user_id`, `real_name`, `email`, `occupation`, `education`,
                                `income_level`, `bio`, `interests`)
VALUES (1,
        '张三',
        'zhangsan@example.com',
        '软件工程师',
        4,
        3,
        '热爱技术，喜欢钻研新技术',
        '编程,阅读,旅游'),
       (2,
        '李四',
        'lisi@example.com',
        '产品经理',
        4,
        4,
        '关注用户体验，追求产品极致',
        '设计,摄影,美食');

-- ========================================
-- 4. 查询验证
-- ========================================

-- 验证表结构
DESCRIBE `app_user`;
DESCRIBE `app_user_profile`;

-- 验证数据和关联
SELECT u.user_id,
       u.mobile,
       u.nickname,
       u.client_type,
       p.real_name,
       p.email,
       p.occupation,
       p.interests
FROM app_user u
         LEFT JOIN app_user_profile p ON u.user_id = p.user_id;

-- 验证索引
SHOW
INDEX FROM `app_user`;
SHOW
INDEX FROM `app_user_profile`;

-- ========================================
-- 5. 常用查询示例
-- ========================================

-- 用户基础信息查询
-- SELECT * FROM app_user WHERE mobile = '13888888888';

-- 用户完整资料查询（连表）
-- SELECT u.*, p.real_name, p.email, p.occupation, p.bio 
-- FROM app_user u 
-- LEFT JOIN app_user_profile p ON u.user_id = p.user_id 
-- WHERE u.user_id = 1;

-- 按兴趣查找用户
-- SELECT u.nickname, p.interests 
-- FROM app_user u 
-- INNER JOIN app_user_profile p ON u.user_id = p.user_id 
-- WHERE p.interests LIKE '%编程%';

-- 按职业统计用户
-- SELECT p.occupation, COUNT(*) as user_count 
-- FROM app_user u 
-- INNER JOIN app_user_profile p ON u.user_id = p.user_id 
-- GROUP BY p.occupation;

-- ========================================
-- 6. 性能优化说明
-- ========================================

/*
表设计说明：

1. 分表策略：
   - app_user：高频访问的基础信息
   - app_user_profile：低频访问的详细资料
   
2. 索引策略：
   - 主键索引：快速定位记录
   - 业务唯一索引：手机号、用户名防重复
   - 查询索引：状态、客户端类型、注册时间
   - 关联索引：user_id外键
   
3. 存储引擎：
   - InnoDB：支持事务、外键约束
   - utf8mb4：支持emoji等特殊字符
   
4. 扩展性：
   - 用户资料表可随业务需求增加字段
   - 支持一对一关联查询
   - 可根据需要拆分更细的子表

5. YAGNI实践：
   - 暂不创建订单、商品、会员等表
   - 专注核心用户功能
   - 后续按需扩展
*/

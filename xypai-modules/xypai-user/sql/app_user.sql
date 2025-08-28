-- ========================================
-- APP用户表设计 (XyPai-User微服务)
-- 版本: v1.0
-- 创建时间: 2025-01-28
-- 描述: APP端用户独立表设计，支持手机号注册
-- ========================================

-- 删除表（如果存在）
DROP TABLE IF EXISTS `app_user`;

-- 创建APP用户表
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

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='APP用户表';

-- ========================================
-- 初始化数据
-- ========================================

-- 插入测试数据
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
        'mini'),
       ('13777777777',
        NULL,
        '微信用户',
        NULL,
        0,
        NULL,
        1,
        'web');

-- ========================================
-- 查询验证
-- ========================================

-- 验证表结构
DESCRIBE `app_user`;

-- 验证数据
SELECT *
FROM `app_user`;

-- 验证索引
SHOW
INDEX FROM `app_user`;

-- ========================================
-- 性能优化建议
-- ========================================

-- 1. 手机号查询优化（已创建唯一索引）
-- SELECT * FROM app_user WHERE mobile = '13888888888';

-- 2. 状态筛选优化（已创建索引）
-- SELECT * FROM app_user WHERE status = 1;

-- 3. 客户端类型统计优化（已创建索引）
-- SELECT client_type, COUNT(*) FROM app_user GROUP BY client_type;

-- 4. 注册时间范围查询优化（已创建索引）
-- SELECT * FROM app_user WHERE register_time >= '2025-01-01' AND register_time < '2025-02-01';

-- ========================================
-- 数据字典
-- ========================================

/*
字段说明：
- user_id: 主键，自增ID
- mobile: 手机号，11位数字，唯一约束
- username: 用户名，可选，最长30字符，唯一约束
- nickname: 昵称，必填，最长30字符
- avatar: 头像URL，最长200字符
- gender: 性别，0=未知 1=男 2=女
- birth_date: 生日，DATE类型
- status: 状态，1=正常 0=禁用
- register_time: 注册时间，自动设置
- last_login_time: 最后登录时间，登录时更新
- client_type: 客户端类型，限制为 web/app/mini

索引说明：
- idx_mobile: 手机号索引，用于快速查询
- idx_status: 状态索引，用于筛选正常/禁用用户
- idx_client_type: 客户端类型索引，用于统计分析
- idx_register_time: 注册时间索引，用于时间范围查询
- uk_mobile: 手机号唯一约束
- uk_username: 用户名唯一约束

约束说明：
- 手机号必须唯一，支持快速注册验证
- 用户名可选但如果设置必须唯一
- 昵称必填，用于显示
- 客户端类型限制为指定值
*/

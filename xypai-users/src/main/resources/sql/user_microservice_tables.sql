-- 🏗️ Users 微服务数据库表结构 - 企业架构实现
-- 遵循企业微服务架构规范的数据表设计

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `users` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `users`;

-- ================================
-- 🏗️ 用户核心表
-- ================================

-- 用户基础信息表
DROP TABLE IF EXISTS `xypai_users`;
CREATE TABLE `xypai_users`
(
    `user_id`         bigint       NOT NULL COMMENT '用户ID - 主键',
    `user_code`       varchar(32)  NOT NULL COMMENT '用户编码 - 业务唯一标识',
    `mobile`          varchar(11)  NOT NULL COMMENT '手机号',
    `username`        varchar(50)  NOT NULL COMMENT '用户名',
    `nickname`        varchar(100) NOT NULL COMMENT '用户昵称',
    `email`           varchar(100)          DEFAULT NULL COMMENT '邮箱',
    `real_name`       varchar(50)           DEFAULT NULL COMMENT '真实姓名',
    `id_card`         varchar(18)           DEFAULT NULL COMMENT '身份证号',
    `gender`          tinyint      NOT NULL DEFAULT '0' COMMENT '性别: 0-未知, 1-男, 2-女, 3-其他',
    `avatar_url`      varchar(500)          DEFAULT NULL COMMENT '头像URL',
    `birthday`        datetime              DEFAULT NULL COMMENT '生日',
    `location`        varchar(200)          DEFAULT NULL COMMENT '所在地区',
    `bio`             varchar(500)          DEFAULT NULL COMMENT '个人简介',
    `status`          tinyint      NOT NULL DEFAULT '1' COMMENT '用户状态: 0-禁用, 1-正常, 2-冻结, 3-注销',
    `user_type`       tinyint      NOT NULL DEFAULT '0' COMMENT '用户类型: 0-普通用户, 1-VIP用户, 2-SVIP用户, 3-企业用户',
    `is_verified`     tinyint      NOT NULL DEFAULT '0' COMMENT '是否实名认证: 0-未认证, 1-已认证',
    `platform`        varchar(50)           DEFAULT NULL COMMENT '注册平台: iOS, Android, Web, WeChat',
    `source_channel`  varchar(100)          DEFAULT NULL COMMENT '注册来源渠道',
    `last_login_time` datetime              DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip`   varchar(50)           DEFAULT NULL COMMENT '最后登录IP',
    `login_count`     int          NOT NULL DEFAULT '0' COMMENT '登录次数',
    `user_level`      int          NOT NULL DEFAULT '1' COMMENT '用户等级',
    `user_points`     int          NOT NULL DEFAULT '0' COMMENT '用户积分',
    `balance`         bigint       NOT NULL DEFAULT '0' COMMENT '用户余额(分)',
    `dept_id`         bigint                DEFAULT NULL COMMENT '部门ID - 数据权限关联',
    `tenant_id`       varchar(20)           DEFAULT NULL COMMENT '租户ID - 多租户支持',
    `del_flag`        char(1)      NOT NULL DEFAULT '0' COMMENT '逻辑删除标志: 0-正常, 1-删除',
    `version`         int          NOT NULL DEFAULT '1' COMMENT '版本号 - 乐观锁',
    `create_by`       varchar(64)           DEFAULT NULL COMMENT '创建者',
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       varchar(64)           DEFAULT NULL COMMENT '更新者',
    `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`          varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_user_code` (`user_code`),
    UNIQUE KEY `uk_mobile` (`mobile`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`),
    KEY `idx_user_type` (`user_type`),
    KEY `idx_is_verified` (`is_verified`),
    KEY `idx_platform` (`platform`),
    KEY `idx_source_channel` (`source_channel`),
    KEY `idx_location` (`location`),
    KEY `idx_last_login_time` (`last_login_time`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_dept_id` (`dept_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='🏗️ XY相遇派用户基础信息表 - 企业架构实现';

-- ================================
-- 🔐 用户安全表
-- ================================

-- 用户密码表(安全隔离)
DROP TABLE IF EXISTS `xypai_user_passwords`;
CREATE TABLE `xypai_user_passwords`
(
    `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`          bigint       NOT NULL COMMENT '用户ID',
    `password_hash`    varchar(255) NOT NULL COMMENT '密码哈希',
    `salt`             varchar(64)  NOT NULL COMMENT '密码盐值',
    `password_type`    tinyint      NOT NULL DEFAULT '1' COMMENT '密码类型: 1-登录密码, 2-支付密码',
    `is_default`       tinyint      NOT NULL DEFAULT '0' COMMENT '是否默认密码: 0-否, 1-是',
    `expire_time`      datetime              DEFAULT NULL COMMENT '密码过期时间',
    `error_count`      int          NOT NULL DEFAULT '0' COMMENT '错误次数',
    `lock_time`        datetime              DEFAULT NULL COMMENT '锁定时间',
    `last_change_time` datetime              DEFAULT NULL COMMENT '最后修改时间',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_password_type` (`user_id`, `password_type`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='🔐 用户密码表 - 安全隔离';

-- ================================
-- 📊 用户扩展信息表
-- ================================

-- 用户统计表
DROP TABLE IF EXISTS `xypai_user_stats`;
CREATE TABLE `xypai_user_stats`
(
    `id`                        bigint   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`                   bigint   NOT NULL COMMENT '用户ID',
    `total_login_count`         int      NOT NULL DEFAULT '0' COMMENT '总登录次数',
    `today_login_count`         int      NOT NULL DEFAULT '0' COMMENT '今日登录次数',
    `continuous_login_days`     int      NOT NULL DEFAULT '0' COMMENT '连续登录天数',
    `max_continuous_login_days` int      NOT NULL DEFAULT '0' COMMENT '最大连续登录天数',
    `total_online_time`         bigint   NOT NULL DEFAULT '0' COMMENT '总在线时长(秒)',
    `last_active_time`          datetime          DEFAULT NULL COMMENT '最后活跃时间',
    `register_ip`               varchar(50)       DEFAULT NULL COMMENT '注册IP',
    `register_device`           varchar(200)      DEFAULT NULL COMMENT '注册设备',
    `create_time`               datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`               datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_last_active_time` (`last_active_time`),
    KEY `idx_continuous_login_days` (`continuous_login_days`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='📊 用户统计表';

-- 用户偏好设置表
DROP TABLE IF EXISTS `xypai_user_preferences`;
CREATE TABLE `xypai_user_preferences`
(
    `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`          bigint       NOT NULL COMMENT '用户ID',
    `preference_key`   varchar(100) NOT NULL COMMENT '偏好设置键',
    `preference_value` text COMMENT '偏好设置值',
    `preference_type`  varchar(20)  NOT NULL DEFAULT 'string' COMMENT '值类型: string, number, boolean, json',
    `is_public`        tinyint      NOT NULL DEFAULT '0' COMMENT '是否公开: 0-私有, 1-公开',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_preference` (`user_id`, `preference_key`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='🎛️ 用户偏好设置表';

-- ================================
-- 📝 用户操作日志表
-- ================================

-- 用户登录日志表
DROP TABLE IF EXISTS `xypai_user_login_logs`;
CREATE TABLE `xypai_user_login_logs`
(
    `id`              bigint   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         bigint   NOT NULL COMMENT '用户ID',
    `login_type`      tinyint  NOT NULL DEFAULT '1' COMMENT '登录类型: 1-密码, 2-短信, 3-微信, 4-其他',
    `login_platform`  varchar(50)       DEFAULT NULL COMMENT '登录平台',
    `login_ip`        varchar(50)       DEFAULT NULL COMMENT '登录IP',
    `login_location`  varchar(200)      DEFAULT NULL COMMENT '登录地点',
    `login_device`    varchar(200)      DEFAULT NULL COMMENT '登录设备',
    `user_agent`      varchar(500)      DEFAULT NULL COMMENT '用户代理',
    `login_status`    tinyint  NOT NULL DEFAULT '1' COMMENT '登录状态: 0-失败, 1-成功',
    `fail_reason`     varchar(200)      DEFAULT NULL COMMENT '失败原因',
    `session_id`      varchar(128)      DEFAULT NULL COMMENT '会话ID',
    `login_time`      datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `logout_time`     datetime          DEFAULT NULL COMMENT '登出时间',
    `online_duration` int               DEFAULT NULL COMMENT '在线时长(秒)',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_login_time` (`login_time`),
    KEY `idx_login_ip` (`login_ip`),
    KEY `idx_login_status` (`login_status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='📝 用户登录日志表';

-- ================================
-- 💰 用户资产相关表
-- ================================

-- 用户余额变动记录表
DROP TABLE IF EXISTS `xypai_user_balance_logs`;
CREATE TABLE `xypai_user_balance_logs`
(
    `id`             bigint   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`        bigint   NOT NULL COMMENT '用户ID',
    `change_type`    tinyint  NOT NULL COMMENT '变动类型: 1-充值, 2-消费, 3-退款, 4-奖励, 5-扣除',
    `change_amount`  bigint   NOT NULL COMMENT '变动金额(分)',
    `balance_before` bigint   NOT NULL COMMENT '变动前余额(分)',
    `balance_after`  bigint   NOT NULL COMMENT '变动后余额(分)',
    `business_type`  varchar(50)       DEFAULT NULL COMMENT '业务类型',
    `business_id`    varchar(64)       DEFAULT NULL COMMENT '业务ID',
    `order_no`       varchar(64)       DEFAULT NULL COMMENT '订单号',
    `description`    varchar(500)      DEFAULT NULL COMMENT '变动描述',
    `operator_id`    varchar(64)       DEFAULT NULL COMMENT '操作员ID',
    `create_time`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_change_type` (`change_type`),
    KEY `idx_business_id` (`business_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='💰 用户余额变动记录表';

-- 用户积分变动记录表
DROP TABLE IF EXISTS `xypai_user_points_logs`;
CREATE TABLE `xypai_user_points_logs`
(
    `id`            bigint   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       bigint   NOT NULL COMMENT '用户ID',
    `change_type`   tinyint  NOT NULL COMMENT '变动类型: 1-获得, 2-消费, 3-过期, 4-管理员调整',
    `change_points` int      NOT NULL COMMENT '变动积分',
    `points_before` int      NOT NULL COMMENT '变动前积分',
    `points_after`  int      NOT NULL COMMENT '变动后积分',
    `business_type` varchar(50)       DEFAULT NULL COMMENT '业务类型',
    `business_id`   varchar(64)       DEFAULT NULL COMMENT '业务ID',
    `description`   varchar(500)      DEFAULT NULL COMMENT '变动描述',
    `expire_time`   datetime          DEFAULT NULL COMMENT '积分过期时间',
    `operator_id`   varchar(64)       DEFAULT NULL COMMENT '操作员ID',
    `create_time`   datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_change_type` (`change_type`),
    KEY `idx_business_id` (`business_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='🎯 用户积分变动记录表';

-- ================================
-- 📋 初始化数据
-- ================================

-- 插入测试用户数据
INSERT INTO `xypai_users` (`user_id`, `user_code`, `mobile`, `username`, `nickname`, `email`,
                           `gender`, `platform`, `source_channel`, `user_level`, `user_points`, `user_type`,
                           `create_by`, `remark`)
VALUES (100000, 'XY20250102001', '13900000001', 'xypai_admin', 'XY管理员', 'admin@xypai.com',
        1, 'Web', 'admin_init', 10, 1000, 0, 'system', '系统管理员账号'),
       (100001, 'XY20250102002', '13900000002', 'xypai_test', 'XY测试用户', 'test@xypai.com',
        2, 'iOS', 'app_store', 1, 100, 0, 'system', '测试用户账号'),
       (100002, 'XY20250102003', '13900000003', 'xypai_vip', 'XY VIP用户', 'vip@xypai.com',
        1, 'Android', 'google_play', 5, 2000, 1, 'system', 'VIP测试账号');


-- 初始化用户统计数据
INSERT INTO `xypai_user_stats` (`user_id`, `register_ip`, `register_device`)
SELECT `user_id`, '127.0.0.1', 'System Init'
FROM `xypai_users`;

-- ================================
-- 📊 创建视图
-- ================================

-- 用户概览视图
CREATE OR REPLACE VIEW `v_user_overview` AS
SELECT u.user_id,
       u.user_code,
       u.username,
       u.nickname,
       u.mobile,
       u.email,
       u.gender,
       u.status,
       u.user_type,
       u.is_verified,
       u.platform,
       u.user_level,
       u.user_points,
       u.balance,
       u.login_count,
       u.last_login_time,
       u.create_time,
       s.continuous_login_days,
       s.total_online_time,
       s.last_active_time
FROM `xypai_users` u
         LEFT JOIN `xypai_user_stats` s ON u.user_id = s.user_id
WHERE u.del_flag = '0';

-- ================================
-- 🔧 存储过程
-- ================================

-- 用户统计更新存储过程
DELIMITER $$
CREATE PROCEDURE `sp_update_user_stats`(
    IN p_user_id BIGINT,
    IN p_login_type TINYINT
)
BEGIN
    -- 声明异常处理器必须在最前面
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;

    -- 处理默认值
    IF p_login_type IS NULL THEN
        SET p_login_type = 1;
    END IF;

    START TRANSACTION;

    -- 更新用户登录信息
    UPDATE `xypai_users`
    SET `last_login_time` = NOW(),
        `login_count`     = `login_count` + 1
    WHERE `user_id` = p_user_id;

    -- 更新用户统计信息
    INSERT INTO `xypai_user_stats` (`user_id`, `total_login_count`, `today_login_count`, `last_active_time`)
    VALUES (p_user_id, 1, 1, NOW())
    ON DUPLICATE KEY UPDATE `total_login_count`         = `total_login_count` + 1,
                            `today_login_count`         = CASE
                                                              WHEN DATE(`last_active_time`) = CURDATE()
                                                                  THEN `today_login_count` + 1
                                                              ELSE 1
                                END,
                            `continuous_login_days`     = CASE
                                                              WHEN DATE(`last_active_time`) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)
                                                                  THEN `continuous_login_days` + 1
                                                              WHEN DATE(`last_active_time`) = CURDATE()
                                                                  THEN `continuous_login_days`
                                                              ELSE 1
                                END,
                            `max_continuous_login_days` = GREATEST(`max_continuous_login_days`,
                                                                   CASE
                                                                       WHEN DATE(`last_active_time`) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)
                                                                           THEN `continuous_login_days` + 1
                                                                       WHEN DATE(`last_active_time`) = CURDATE()
                                                                           THEN `continuous_login_days`
                                                                       ELSE 1
                                                                       END),
                            `last_active_time`          = NOW();
    COMMIT;
END$$
DELIMITER ;

-- ================================
-- 📈 创建索引优化
-- ================================

-- 复合索引优化查询
CREATE INDEX `idx_user_status_type` ON `xypai_users` (`status`, `user_type`);
CREATE INDEX `idx_user_create_status` ON `xypai_users` (`create_time`, `status`);
CREATE INDEX `idx_user_login_status` ON `xypai_users` (`last_login_time`, `status`);

-- 日志表索引优化
CREATE INDEX `idx_login_user_time` ON `xypai_user_login_logs` (`user_id`, `login_time`);
CREATE INDEX `idx_balance_user_time` ON `xypai_user_balance_logs` (`user_id`, `create_time`);
CREATE INDEX `idx_points_user_time` ON `xypai_user_points_logs` (`user_id`, `create_time`);

-- ================================
-- 🎯 数据库表结构完成
-- ================================

-- 显示创建结果
SELECT 'Users 微服务数据库表结构创建完成！' AS result;
SELECT COUNT(*) AS table_count
FROM information_schema.tables
WHERE table_schema = 'users'
  AND table_type = 'BASE TABLE';

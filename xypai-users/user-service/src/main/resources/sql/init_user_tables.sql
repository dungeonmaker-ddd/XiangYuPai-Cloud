-- ==========================================
-- 👤 用户服务数据库初始化 - MVP版本
-- 设计原则：简单够用，快速迭代
-- ==========================================

-- 创建数据库
CREATE
DATABASE IF NOT EXISTS xypai_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE
xypai_user;

-- 用户表 (MVP精简版)
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `mobile`      VARCHAR(20) NOT NULL COMMENT '手机号',
    `username`    VARCHAR(50) NOT NULL COMMENT '用户名',
    `nickname`    VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    `avatar`      VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `gender`      TINYINT      DEFAULT 0 COMMENT '性别(0-未知,1-男,2-女)',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态(1-正常,2-禁用)',
    `client_type` VARCHAR(10)  DEFAULT 'app' COMMENT '客户端类型(web/app/mini)',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      DEFAULT 0 COMMENT '删除标记(0-正常,1-删除)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_mobile` (`mobile`),
    UNIQUE KEY `uk_username` (`username`),
    KEY           `idx_status` (`status`),
    KEY           `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表-MVP版本';

-- 用户扩展信息表 (可选信息)
DROP TABLE IF EXISTS `user_profile`;
CREATE TABLE `user_profile`
(
    `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       BIGINT NOT NULL COMMENT '用户ID',
    `real_name`     VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    `email`         VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `birth_date`    DATE         DEFAULT NULL COMMENT '生日',
    `location`      VARCHAR(200) DEFAULT NULL COMMENT '地理位置',
    `bio`           TEXT         DEFAULT NULL COMMENT '个人简介',
    `interests`     VARCHAR(500) DEFAULT NULL COMMENT '兴趣爱好',
    `privacy_level` TINYINT      DEFAULT 1 COMMENT '隐私级别(1-5)',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY             `idx_real_name` (`real_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户扩展信息表';

-- 插入测试数据
INSERT INTO `user` (`mobile`, `username`, `nickname`, `gender`, `status`, `client_type`)
VALUES ('13800138000', 'admin', '管理员', 1, 1, 'web'),
       ('13800138001', 'test01', '测试用户1', 1, 1, 'app'),
       ('13800138002', 'test02', '测试用户2', 2, 1, 'mini'),
       ('13800138003', 'test03', '测试用户3', 0, 1, 'app');

-- 插入扩展信息
INSERT INTO `user_profile` (`user_id`, `real_name`, `email`, `bio`)
VALUES (1, '系统管理员', 'admin@xypai.com', '系统管理员账号'),
       (2, '张三', 'test01@xypai.com', '测试用户'),
       (3, '李四', 'test02@xypai.com', '测试用户'),
       (4, '王五', 'test03@xypai.com', '测试用户');

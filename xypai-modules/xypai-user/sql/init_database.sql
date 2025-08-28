-- ========================================
-- XyPai-User 数据库初始化脚本
-- 版本: v1.0
-- 创建时间: 2025-01-28
-- 用途: 创建数据库和基础配置
-- ========================================

-- 创建APP业务专用数据库（如果不存在）
CREATE
DATABASE IF NOT EXISTS `xypai_app`
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci
COMMENT 'XyPai APP业务全模块数据库 - 包含用户、订单、商品、会员等';

-- 使用数据库
USE
`xypai_app`;

-- 设置时区（可选）
SET
time_zone = '+08:00';

-- 显示当前数据库信息
SELECT SCHEMA_NAME as '数据库名', DEFAULT_CHARACTER_SET_NAME as '字符集', DEFAULT_COLLATION_NAME as '排序规则'
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME = 'xypai_app';

-- 提示信息
SELECT '✅ XyPai-App 数据库初始化完成！' as '初始化状态';
SELECT '📝 请继续执行 xypai_app_tables.sql 创建所有APP业务表' as '下一步操作';

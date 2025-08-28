-- ========================================
-- XyPai User模块 - 初始化数据脚本
-- 版本: 2.0 DDD Architecture
-- 创建时间: 2025-01-02
-- 描述: 初始化测试数据和基础配置数据
-- ========================================

USE
xypai_user;

-- ========================================
-- 1. 清理现有数据（可选）
-- ========================================

-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE domain_event_log;
-- TRUNCATE TABLE wallet_transaction;
-- TRUNCATE TABLE wallet;
-- TRUNCATE TABLE user_feed;
-- TRUNCATE TABLE interaction_record;
-- TRUNCATE TABLE interaction_target;
-- TRUNCATE TABLE activity_participant;
-- TRUNCATE TABLE activity;
-- TRUNCATE TABLE social_settings;
-- TRUNCATE TABLE social_relation;
-- TRUNCATE TABLE app_user;
-- SET FOREIGN_KEY_CHECKS = 1;

-- ========================================
-- 2. 插入测试用户数据
-- ========================================

-- 插入系统管理员和测试用户
INSERT INTO `app_user` (`user_id`, `mobile`, `username`, `nickname`, `avatar`, `bio`, `gender`, `status`)
VALUES (1, '13800000001', 'admin', '系统管理员', 'https://avatar.example.com/admin.jpg',
        '我是系统管理员，负责平台管理和维护', 'UNKNOWN', 'ACTIVE'),
       (2, '13800000002', 'alice', '爱丽丝', 'https://avatar.example.com/alice.jpg',
        '热爱生活的女孩，喜欢摄影和旅行 📸✈️', 'FEMALE', 'ACTIVE'),
       (3, '13800000003', 'bob', '鲍勃', 'https://avatar.example.com/bob.jpg', '技术宅男，专注于软件开发 💻', 'MALE',
        'ACTIVE'),
       (4, '13800000004', 'charlie', '查理', 'https://avatar.example.com/charlie.jpg', '运动达人，马拉松爱好者 🏃‍♂️',
        'MALE', 'ACTIVE'),
       (5, '13800000005', 'diana', '戴安娜', 'https://avatar.example.com/diana.jpg', '美食博主，分享各地美食文化 🍽️',
        'FEMALE', 'ACTIVE'),
       (6, '13800000006', 'eve', '伊芙', 'https://avatar.example.com/eve.jpg', '艺术设计师，热爱创意设计 🎨', 'FEMALE',
        'ACTIVE'),
       (7, '13800000007', 'frank', '弗兰克', 'https://avatar.example.com/frank.jpg', '音乐制作人，独立音乐创作者 🎵',
        'MALE', 'ACTIVE'),
       (8, '13800000008', 'grace', '格蕾丝', 'https://avatar.example.com/grace.jpg', '瑜伽教练，提倡健康生活方式 🧘‍♀️',
        'FEMALE', 'ACTIVE'),
       (9, '13800000009', 'henry', '亨利', 'https://avatar.example.com/henry.jpg', '创业者，专注于科技创新 🚀', 'MALE',
        'ACTIVE'),
       (10, '13800000010', 'iris', '艾里斯', 'https://avatar.example.com/iris.jpg', '环保志愿者，关注可持续发展 🌱',
        'FEMALE', 'ACTIVE');

-- ========================================
-- 3. 插入社交设置数据
-- ========================================

-- 为每个用户创建社交设置
INSERT INTO `social_settings` (`user_id`, `allow_follow`, `follow_need_approve`, `allow_message`, `allow_tag`,
                               `privacy_level`)
VALUES (1, TRUE, FALSE, TRUE, TRUE, 'PUBLIC'),
       (2, TRUE, FALSE, TRUE, TRUE, 'PUBLIC'),
       (3, TRUE, TRUE, TRUE, FALSE, 'FRIENDS'),
       (4, TRUE, FALSE, TRUE, TRUE, 'PUBLIC'),
       (5, TRUE, FALSE, TRUE, TRUE, 'PUBLIC'),
       (6, TRUE, TRUE, FALSE, TRUE, 'FRIENDS'),
       (7, TRUE, FALSE, TRUE, TRUE, 'PUBLIC'),
       (8, TRUE, FALSE, TRUE, TRUE, 'PUBLIC'),
       (9, TRUE, TRUE, TRUE, FALSE, 'PRIVATE'),
       (10, TRUE, FALSE, TRUE, TRUE, 'PUBLIC');

-- ========================================
-- 4. 插入社交关系数据
-- ========================================

-- 创建一些关注关系，构建社交网络
INSERT INTO `social_relation` (`user_id`, `target_user_id`, `relation_type`)
VALUES
-- Alice的关注关系
(2, 3, 'FOLLOW'),
(2, 4, 'FOLLOW'),
(2, 5, 'FOLLOW'),
(2, 8, 'FOLLOW'),
-- Bob的关注关系
(3, 2, 'FOLLOW'),
(3, 7, 'FOLLOW'),
(3, 9, 'FOLLOW'),
-- Charlie的关注关系
(4, 2, 'FOLLOW'),
(4, 8, 'FOLLOW'),
(4, 10, 'FOLLOW'),
-- Diana的关注关系
(5, 2, 'FOLLOW'),
(5, 6, 'FOLLOW'),
(5, 8, 'FOLLOW'),
(5, 10, 'FOLLOW'),
-- Eve的关注关系
(6, 5, 'FOLLOW'),
(6, 7, 'FOLLOW'),
(6, 8, 'FOLLOW'),
-- Frank的关注关系
(7, 3, 'FOLLOW'),
(7, 6, 'FOLLOW'),
(7, 9, 'FOLLOW'),
-- Grace的关注关系
(8, 2, 'FOLLOW'),
(8, 4, 'FOLLOW'),
(8, 5, 'FOLLOW'),
(8, 10, 'FOLLOW'),
-- Henry的关注关系
(9, 3, 'FOLLOW'),
(9, 7, 'FOLLOW'),
-- Iris的关注关系
(10, 4, 'FOLLOW'),
(10, 5, 'FOLLOW'),
(10, 8, 'FOLLOW');

-- ========================================
-- 5. 插入钱包数据
-- ========================================

-- 为每个用户创建钱包
INSERT INTO `wallet` (`user_id`, `balance`, `currency`, `status`)
VALUES (1, 50000.00, 'CNY', 'ACTIVE'), -- 管理员钱包，余额5万
       (2, 1500.00, 'CNY', 'ACTIVE'),  -- Alice，余额1500
       (3, 2000.00, 'CNY', 'ACTIVE'),  -- Bob，余额2000
       (4, 800.00, 'CNY', 'ACTIVE'),   -- Charlie，余额800
       (5, 3000.00, 'CNY', 'ACTIVE'),  -- Diana，余额3000
       (6, 1200.00, 'CNY', 'ACTIVE'),  -- Eve，余额1200
       (7, 2500.00, 'CNY', 'ACTIVE'),  -- Frank，余额2500
       (8, 1800.00, 'CNY', 'ACTIVE'),  -- Grace，余额1800
       (9, 10000.00, 'CNY', 'ACTIVE'), -- Henry，余额1万
       (10, 500.00, 'CNY', 'ACTIVE');
-- Iris，余额500

-- ========================================
-- 6. 插入示例交易数据
-- ========================================

-- 插入一些示例交易记录
INSERT INTO `wallet_transaction` (`transaction_id`, `wallet_id`, `from_user_id`, `to_user_id`, `amount`, `fee`, `type`,
                                  `status`, `description`, `create_time`, `complete_time`)
VALUES
-- 充值记录
('txn_recharge_001', 2, NULL, 2, 1000.00, 0.00, 'RECHARGE', 'SUCCESS', '支付宝充值', DATE_SUB(NOW(), INTERVAL 7 DAY),
 DATE_SUB(NOW(), INTERVAL 7 DAY)),
('txn_recharge_002', 3, NULL, 3, 2000.00, 0.00, 'RECHARGE', 'SUCCESS', '银行卡充值', DATE_SUB(NOW(), INTERVAL 5 DAY),
 DATE_SUB(NOW(), INTERVAL 5 DAY)),
('txn_recharge_003', 5, NULL, 5, 3000.00, 0.00, 'RECHARGE', 'SUCCESS', '微信充值', DATE_SUB(NOW(), INTERVAL 3 DAY),
 DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- 转账记录
('txn_transfer_001', 2, 2, 4, 200.00, 2.00, 'TRANSFER_OUT', 'SUCCESS', '转账给Charlie', DATE_SUB(NOW(), INTERVAL 2 DAY),
 DATE_SUB(NOW(), INTERVAL 2 DAY)),
('txn_transfer_002', 4, 2, 4, 200.00, 0.00, 'TRANSFER_IN', 'SUCCESS', '接收Alice的转账',
 DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('txn_transfer_003', 3, 3, 7, 500.00, 5.00, 'TRANSFER_OUT', 'SUCCESS', '转账给Frank', DATE_SUB(NOW(), INTERVAL 1 DAY),
 DATE_SUB(NOW(), INTERVAL 1 DAY)),
('txn_transfer_004', 7, 3, 7, 500.00, 0.00, 'TRANSFER_IN', 'SUCCESS', '接收Bob的转账', DATE_SUB(NOW(), INTERVAL 1 DAY),
 DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- 消费记录
('txn_payment_001', 5, 5, NULL, 150.00, 0.00, 'PAYMENT', 'SUCCESS', '购买活动门票', NOW(), NOW()),
('txn_payment_002', 8, 8, NULL, 88.00, 0.00, 'PAYMENT', 'SUCCESS', '瑜伽课程费用', NOW(), NOW());

-- ========================================
-- 7. 插入活动数据
-- ========================================

-- 插入示例活动
INSERT INTO `activity` (`organizer_id`, `title`, `description`, `location`, `start_time`, `end_time`,
                        `max_participants`, `fee`, `status`, `type`)
VALUES (2, '周末摄影外拍活动', '欢迎摄影爱好者参加，我们将前往市郊拍摄自然风景。活动包含专业指导和作品分享交流。',
        '西山森林公园', DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY), 15, 50.00, 'PUBLISHED',
        'CULTURE'),
       (4, '城市马拉松训练营', '为准备参加下月马拉松比赛的跑友提供专业训练指导。包含跑步技巧、体能训练和营养建议。',
        '市体育中心', DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY), 30, 0.00, 'PUBLISHED',
        'SPORTS'),
       (5, '美食品鉴会', '本月主题：川菜文化。邀请川菜大师现场制作，参与者可品尝和学习经典川菜制作技巧。', '美食文化中心',
        DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 20, 120.00, 'PUBLISHED', 'SOCIAL'),
       (7, '独立音乐分享会', '本地独立音乐人作品分享，现场演奏和音乐创作交流。欢迎音乐爱好者参加。', '音乐咖啡厅',
        DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY), 50, 30.00, 'PUBLISHED', 'CULTURE'),
       (8, '瑜伽与冥想工作坊', '结合瑜伽体式和冥想练习，帮助参与者缓解压力，提升身心健康。适合所有水平。', '禅修中心',
        DATE_ADD(NOW(), INTERVAL 12 DAY), DATE_ADD(NOW(), INTERVAL 12 DAY), 25, 80.00, 'PUBLISHED', 'SPORTS'),
       (9, '科技创新论坛', '邀请行业专家分享最新科技趋势，探讨创新创业机会。面向创业者和科技从业者。', '创新大厦会议厅',
        DATE_ADD(NOW(), INTERVAL 15 DAY), DATE_ADD(NOW(), INTERVAL 15 DAY), 100, 200.00, 'PUBLISHED', 'BUSINESS'),
       (10, '环保志愿者行动', '清理公园垃圾，种植树木，宣传环保理念。为地球贡献一份力量。', '滨江公园',
        DATE_ADD(NOW(), INTERVAL 20 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY), 40, 0.00, 'PUBLISHED', 'OTHER');

-- ========================================
-- 8. 插入活动参与者数据
-- ========================================

-- 为活动添加参与者
INSERT INTO `activity_participant` (`activity_id`, `user_id`, `status`, `message`, `join_time`)
VALUES
-- 摄影活动参与者
(1, 3, 'APPROVED', '很期待这次摄影活动！', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 6, 'APPROVED', '作为设计师，想学习摄影技巧', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(1, 8, 'PENDING', '希望能参加，时间刚好', NOW()),

-- 马拉松训练营参与者
(2, 8, 'APPROVED', '瑜伽教练想学习跑步技巧', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 10, 'APPROVED', '为了更好地参与环保跑步活动', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 5, 'APPROVED', '想通过跑步保持健康', DATE_SUB(NOW(), INTERVAL 12 HOUR)),

-- 美食品鉴会参与者
(3, 2, 'APPROVED', '爱丽丝想学习川菜制作', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 6, 'APPROVED', '对美食文化很感兴趣', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 8, 'PENDING', '希望学习健康美食制作', NOW()),

-- 音乐分享会参与者
(4, 6, 'APPROVED', '设计师对音乐艺术很感兴趣', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 9, 'APPROVED', '想了解音乐产业', DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- 瑜伽工作坊参与者
(5, 2, 'APPROVED', '想学习瑜伽放松身心', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 4, 'APPROVED', '马拉松选手需要拉伸训练', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(5, 10, 'PENDING', '环保工作压力大，需要放松', NOW());

-- ========================================
-- 9. 插入用户动态数据
-- ========================================

-- 插入用户动态
INSERT INTO `user_feed` (`user_id`, `content`, `media_urls`, `feed_type`, `status`, `publish_time`)
VALUES (2, '今天拍摄了一组日落照片，光线真的太美了！📸 下周的摄影活动欢迎大家一起来～',
        '["https://img.example.com/sunset1.jpg", "https://img.example.com/sunset2.jpg"]',
        'IMAGE', 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 6 HOUR)),

       (3, '刚完成了一个新的开源项目，使用Spring Boot + DDD架构。代码已经上传到GitHub，欢迎大家star和贡献代码！💻',
        NULL, 'TEXT', 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 12 HOUR)),

       (4, '今天晨跑10公里，感觉状态越来越好了！为下个月的马拉松比赛做准备 🏃‍♂️',
        '["https://img.example.com/running_route.jpg"]', 'IMAGE', 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 18 HOUR)),

       (5, '周末去了新开的川菜馆，麻婆豆腐做得超级正宗！分享一下制作心得 🌶️',
        '["https://img.example.com/mapo_tofu.jpg", "https://img.example.com/restaurant.jpg"]',
        'IMAGE', 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 1 DAY)),

       (7, '昨晚录制了一首新歌demo，灵感来自于城市夜景。音乐就是生活的写照 🎵',
        '["https://audio.example.com/new_song_demo.mp3"]', 'IMAGE', 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 2 DAY)),

       (8, '今天的瑜伽课学员们都很棒！看到大家在练习中找到内心的平静，我也很有成就感 🧘‍♀️',
        '["https://img.example.com/yoga_class.jpg"]', 'IMAGE', 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 3 DAY)),

       (10, '参加了海滩清理活动，收集了50公斤垃圾。每个人的小行动都能为环保贡献力量 🌊♻️',
        '["https://img.example.com/beach_cleanup.jpg", "https://img.example.com/collected_trash.jpg"]',
        'IMAGE', 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 4 DAY));

-- ========================================
-- 10. 插入互动数据
-- ========================================

-- 创建互动目标统计
INSERT INTO `interaction_target` (`target_id`, `target_type`, `like_count`, `favorite_count`, `comment_count`,
                                  `share_count`)
VALUES
-- 动态的互动统计
(1, 'FEED', 15, 8, 5, 3),     -- Alice的摄影动态
(2, 'FEED', 12, 4, 8, 2),     -- Bob的技术动态
(3, 'FEED', 20, 2, 6, 4),     -- Charlie的跑步动态
(4, 'FEED', 18, 12, 9, 5),    -- Diana的美食动态
(5, 'FEED', 10, 3, 4, 1),     -- Frank的音乐动态
(6, 'FEED', 25, 6, 12, 3),    -- Grace的瑜伽动态
(7, 'FEED', 22, 15, 8, 6),    -- Iris的环保动态

-- 活动的互动统计
(1, 'ACTIVITY', 8, 5, 3, 2),  -- 摄影活动
(2, 'ACTIVITY', 12, 3, 6, 1), -- 马拉松训练营
(3, 'ACTIVITY', 15, 8, 7, 4), -- 美食品鉴会
(4, 'ACTIVITY', 6, 2, 4, 1),  -- 音乐分享会
(5, 'ACTIVITY', 10, 4, 5, 2);
-- 瑜伽工作坊

-- 插入一些互动记录示例
INSERT INTO `interaction_record` (`user_id`, `target_id`, `target_type`, `interaction_type`, `content`)
VALUES
-- 对动态的互动
(3, 1, 'FEED', 'LIKE', NULL),         -- Bob给Alice摄影动态点赞
(4, 1, 'FEED', 'LIKE', NULL),         -- Charlie给Alice摄影动态点赞
(5, 1, 'FEED', 'FAVORITE', NULL),     -- Diana收藏Alice摄影动态
(6, 1, 'FEED', 'COMMENT', '照片拍得真棒！下次一起拍照呀～'),

(2, 2, 'FEED', 'LIKE', NULL),         -- Alice给Bob技术动态点赞
(7, 2, 'FEED', 'COMMENT', '代码写得很优雅，学习了！'),
(9, 2, 'FEED', 'FAVORITE', NULL),     -- Henry收藏Bob技术动态

(2, 3, 'FEED', 'LIKE', NULL),         -- Alice给Charlie跑步动态点赞
(8, 3, 'FEED', 'LIKE', NULL),         -- Grace给Charlie跑步动态点赞
(8, 3, 'FEED', 'COMMENT', '跑步状态真棒！要注意拉伸哦'),

-- 对活动的互动
(3, 1, 'ACTIVITY', 'LIKE', NULL),     -- Bob给摄影活动点赞
(6, 1, 'ACTIVITY', 'FAVORITE', NULL), -- Eve收藏摄影活动
(8, 2, 'ACTIVITY', 'LIKE', NULL),     -- Grace给马拉松训练营点赞
(2, 3, 'ACTIVITY', 'FAVORITE', NULL);
-- Alice收藏美食品鉴会

-- ========================================
-- 11. 插入一些领域事件日志
-- ========================================

-- 插入示例领域事件
INSERT INTO `domain_event_log` (`event_id`, `event_type`, `aggregate_type`, `aggregate_id`, `event_data`, `status`,
                                `occurred_on`, `processed_on`)
VALUES ('evt_user_followed_001', 'user.followed', 'SocialAggregate', '2',
        '{"followerId": "2", "followeeId": "3", "timestamp": "2025-01-02T10:00:00Z"}', 'SUCCESS',
        DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_SUB(NOW(), INTERVAL 6 HOUR)),
       ('evt_activity_joined_001', 'activity.joined', 'ActivityAggregate', '1',
        '{"activityId": "1", "participantId": "3", "timestamp": "2025-01-02T14:00:00Z"}', 'SUCCESS',
        DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR)),
       ('evt_wallet_transferred_001', 'wallet.transferred', 'WalletAggregate', '2',
        '{"fromUserId": "2", "toUserId": "4", "amount": 200.00, "transactionId": "txn_transfer_001"}', 'SUCCESS',
        DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
       ('evt_feed_published_001', 'feed.published', 'FeedAggregate', '1',
        '{"feedId": "1", "userId": "2", "feedType": "IMAGE", "timestamp": "2025-01-02T08:00:00Z"}', 'SUCCESS',
        DATE_SUB(NOW(), INTERVAL 8 HOUR), DATE_SUB(NOW(), INTERVAL 8 HOUR)),
       ('evt_target_liked_001', 'target.liked', 'InteractionAggregate', '1',
        '{"targetId": "1", "targetType": "FEED", "userId": "3", "timestamp": "2025-01-02T12:00:00Z"}', 'SUCCESS',
        DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 4 HOUR));

-- ========================================
-- 12. 数据验证和统计
-- ========================================

-- 显示数据统计信息
SELECT '用户总数' AS metric,
       COUNT(*) AS count
FROM app_user
WHERE status = 'ACTIVE'

UNION ALL

SELECT '社交关系总数' AS metric,
       COUNT(*) AS count
FROM social_relation

UNION ALL

SELECT '钱包总数' AS metric,
       COUNT(*) AS count
FROM wallet
WHERE status = 'ACTIVE'

UNION ALL

SELECT '活动总数' AS metric,
       COUNT(*) AS count
FROM activity

UNION ALL

SELECT '动态总数' AS metric,
       COUNT(*) AS count
FROM user_feed
WHERE status = 'PUBLISHED'

UNION ALL

SELECT '交易记录总数' AS metric,
       COUNT(*) AS count
FROM wallet_transaction

UNION ALL

SELECT '领域事件总数' AS metric,
       COUNT(*) AS count
FROM domain_event_log;

-- 显示钱包余额统计
SELECT 'CNY'        AS currency,
       COUNT(*)     AS wallet_count,
       SUM(balance) AS total_balance,
       AVG(balance) AS avg_balance,
       MIN(balance) AS min_balance,
       MAX(balance) AS max_balance
FROM wallet
WHERE status = 'ACTIVE';

-- 显示社交网络统计
SELECT u.nickname,
       COALESCE(following.count, 0) AS following_count,
       COALESCE(followers.count, 0) AS follower_count
FROM app_user u
         LEFT JOIN (SELECT user_id, COUNT(*) as count
                    FROM social_relation
                    WHERE relation_type = 'FOLLOW'
                    GROUP BY user_id) following ON u.user_id = following.user_id
         LEFT JOIN (SELECT target_user_id, COUNT(*) as count
                    FROM social_relation
                    WHERE relation_type = 'FOLLOW'
                    GROUP BY target_user_id) followers ON u.user_id = followers.target_user_id
WHERE u.status = 'ACTIVE'
ORDER BY follower_count DESC, following_count DESC;

-- ========================================
-- 完成提示
-- ========================================

SELECT 'XyPai User DDD架构测试数据初始化完成!' AS status,
       NOW()                                   AS completion_time;

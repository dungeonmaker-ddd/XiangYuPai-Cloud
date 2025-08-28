package com.xypai.user.infrastructure.cache;

import com.xypai.user.domain.valueobject.FeedId;
import com.xypai.user.domain.valueobject.TargetId;
import com.xypai.user.domain.valueobject.UserId;

/**
 * 缓存键管理器 - 统一管理所有缓存键
 *
 * @author XyPai
 * @since 2025-01-02
 */
public final class CacheKeys {

    // ========================================
    // 缓存前缀定义
    // ========================================

    private static final String PREFIX = "xypai:user:";

    // 用户相关
    private static final String USER_PREFIX = PREFIX + "user:";
    private static final String USER_PROFILE = USER_PREFIX + "profile:";

    // 社交相关
    private static final String SOCIAL_PREFIX = PREFIX + "social:";
    private static final String FOLLOWING = SOCIAL_PREFIX + "following:";
    private static final String FOLLOWERS = SOCIAL_PREFIX + "followers:";
    private static final String SOCIAL_STATS = SOCIAL_PREFIX + "stats:";

    // 动态相关
    private static final String FEED_PREFIX = PREFIX + "feed:";
    private static final String FEED_DETAIL = FEED_PREFIX + "detail:";
    private static final String FEED_TIMELINE = FEED_PREFIX + "timeline:";
    private static final String FEED_HOT = FEED_PREFIX + "hot:";

    // 互动相关
    private static final String INTERACTION_PREFIX = PREFIX + "interaction:";
    private static final String INTERACTION_STATS = INTERACTION_PREFIX + "stats:";
    private static final String USER_LIKES = INTERACTION_PREFIX + "likes:";
    private static final String USER_FAVORITES = INTERACTION_PREFIX + "favorites:";

    // 热点数据
    private static final String HOT_PREFIX = PREFIX + "hot:";
    private static final String HOT_USERS = HOT_PREFIX + "users";
    private static final String HOT_FEEDS = HOT_PREFIX + "feeds";
    private static final String HOT_HASHTAGS = HOT_PREFIX + "hashtags";

    // ========================================
    // 用户缓存键
    // ========================================

    // 私有构造器防止实例化
    private CacheKeys() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 用户详情缓存键
     */
    public static String userProfile(UserId userId) {
        return USER_PROFILE + userId.value();
    }

    // ========================================
    // 社交缓存键
    // ========================================

    /**
     * 用户基本信息缓存键
     */
    public static String userBasic(UserId userId) {
        return USER_PREFIX + "basic:" + userId.value();
    }

    /**
     * 用户关注列表缓存键
     */
    public static String userFollowings(UserId userId) {
        return FOLLOWING + userId.value();
    }

    /**
     * 用户粉丝列表缓存键
     */
    public static String userFollowers(UserId userId) {
        return FOLLOWERS + userId.value();
    }

    /**
     * 社交统计缓存键
     */
    public static String socialStats(UserId userId) {
        return SOCIAL_STATS + userId.value();
    }

    // ========================================
    // 动态缓存键
    // ========================================

    /**
     * 用户关注关系缓存键（检查A是否关注B）
     */
    public static String followRelation(UserId followerId, UserId followeeId) {
        return FOLLOWING + followerId.value() + ":" + followeeId.value();
    }

    /**
     * 动态详情缓存键
     */
    public static String feedDetail(FeedId feedId) {
        return FEED_DETAIL + feedId.value();
    }

    /**
     * 用户时间线缓存键
     */
    public static String userTimeline(UserId userId) {
        return FEED_TIMELINE + "user:" + userId.value();
    }

    /**
     * 关注时间线缓存键
     */
    public static String followingTimeline(UserId userId) {
        return FEED_TIMELINE + "following:" + userId.value();
    }

    /**
     * 公开热门动态缓存键
     */
    public static String publicHotFeeds() {
        return HOT_FEEDS;
    }

    /**
     * 话题动态缓存键
     */
    public static String hashtagFeeds(String hashtag) {
        return FEED_PREFIX + "hashtag:" + hashtag;
    }

    // ========================================
    // 互动缓存键
    // ========================================

    /**
     * 位置动态缓存键
     */
    public static String locationFeeds(String location) {
        return FEED_PREFIX + "location:" + location;
    }

    /**
     * 目标互动统计缓存键
     */
    public static String interactionStats(TargetId targetId) {
        return INTERACTION_STATS + targetId.value();
    }

    /**
     * 用户点赞目标关系缓存键
     */
    public static String userLikeTarget(UserId userId, TargetId targetId) {
        return USER_LIKES + userId.value() + ":" + targetId.value();
    }

    /**
     * 用户收藏目标关系缓存键
     */
    public static String userFavoriteTarget(UserId userId, TargetId targetId) {
        return USER_FAVORITES + userId.value() + ":" + targetId.value();
    }

    /**
     * 用户点赞列表缓存键
     */
    public static String userLikes(UserId userId) {
        return USER_LIKES + userId.value();
    }

    // ========================================
    // 热点数据缓存键
    // ========================================

    /**
     * 用户收藏列表缓存键
     */
    public static String userFavorites(UserId userId) {
        return USER_FAVORITES + userId.value();
    }

    /**
     * 热门用户缓存键
     */
    public static String hotUsers() {
        return HOT_USERS;
    }

    // ========================================
    // 缓存模式匹配
    // ========================================

    /**
     * 热门话题缓存键
     */
    public static String hotHashtags() {
        return HOT_HASHTAGS;
    }

    /**
     * 用户相关的所有缓存键模式
     */
    public static String userPattern(UserId userId) {
        return PREFIX + "*:" + userId.value() + "*";
    }

    /**
     * 动态相关的所有缓存键模式
     */
    public static String feedPattern(FeedId feedId) {
        return PREFIX + "*:" + feedId.value() + "*";
    }

    /**
     * 社交关系相关的缓存键模式
     */
    public static String socialPattern(UserId userId) {
        return SOCIAL_PREFIX + "*:" + userId.value() + "*";
    }
}

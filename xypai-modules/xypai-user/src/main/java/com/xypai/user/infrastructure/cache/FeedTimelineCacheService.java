package com.xypai.user.infrastructure.cache;

import com.xypai.user.domain.service.CacheService;
import com.xypai.user.domain.valueobject.FeedId;
import com.xypai.user.domain.valueobject.UserId;
import com.xypai.user.insurance.dto.FeedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * 动态时间线缓存服务 - 专门处理动态和时间线的缓存
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedTimelineCacheService {

    // 缓存过期时间配置
    private static final Duration FEED_DETAIL_TTL = Duration.ofHours(2);     // 动态详情缓存2小时
    private static final Duration USER_TIMELINE_TTL = Duration.ofMinutes(30); // 用户时间线缓存30分钟
    private static final Duration FOLLOWING_TIMELINE_TTL = Duration.ofMinutes(15); // 关注时间线缓存15分钟
    private static final Duration HOT_FEEDS_TTL = Duration.ofMinutes(10);    // 热门动态缓存10分钟
    private static final Duration HASHTAG_FEEDS_TTL = Duration.ofHours(1);   // 话题动态缓存1小时
    // 时间线缓存大小限制
    private static final int TIMELINE_MAX_SIZE = 100;
    private static final int HOT_FEEDS_MAX_SIZE = 50;
    private final CacheService cacheService;

    // ========================================
    // 动态详情缓存
    // ========================================

    /**
     * 缓存动态详情
     */
    public void cacheFeedDetail(FeedId feedId, FeedResponse feedResponse) {
        String key = CacheKeys.feedDetail(feedId);
        cacheService.set(key, feedResponse, FEED_DETAIL_TTL);
        log.debug("缓存动态详情: feedId={}", feedId);
    }

    /**
     * 获取动态详情缓存
     */
    public Optional<FeedResponse> getFeedDetail(FeedId feedId) {
        String key = CacheKeys.feedDetail(feedId);
        return cacheService.get(key, FeedResponse.class);
    }

    /**
     * 清除动态详情缓存
     */
    public void invalidateFeedDetail(FeedId feedId) {
        String key = CacheKeys.feedDetail(feedId);
        cacheService.delete(key);
        log.debug("清除动态详情缓存: feedId={}", feedId);
    }

    // ========================================
    // 用户时间线缓存
    // ========================================

    /**
     * 缓存用户时间线
     */
    public void cacheUserTimeline(UserId userId, List<FeedId> timeline) {
        String key = CacheKeys.userTimeline(userId);
        // 限制时间线长度
        List<FeedId> limitedTimeline = timeline.size() > TIMELINE_MAX_SIZE
                ? timeline.subList(0, TIMELINE_MAX_SIZE)
                : timeline;

        cacheService.setList(key, limitedTimeline, USER_TIMELINE_TTL);
        log.debug("缓存用户时间线: userId={}, count={}", userId, limitedTimeline.size());
    }

    /**
     * 获取用户时间线缓存
     */
    public Optional<List<FeedId>> getUserTimeline(UserId userId) {
        String key = CacheKeys.userTimeline(userId);
        List<FeedId> timeline = cacheService.getList(key, FeedId.class);
        return timeline.isEmpty() ? Optional.empty() : Optional.of(timeline);
    }

    /**
     * 在用户时间线头部添加新动态
     */
    public void addToUserTimelineHead(UserId userId, FeedId feedId) {
        String key = CacheKeys.userTimeline(userId);
        cacheService.addToListHead(key, feedId);

        // 维护时间线长度
        trimTimelineToMaxSize(key);
        log.debug("添加动态到用户时间线头部: userId={}, feedId={}", userId, feedId);
    }

    /**
     * 清除用户时间线缓存
     */
    public void invalidateUserTimeline(UserId userId) {
        String key = CacheKeys.userTimeline(userId);
        cacheService.delete(key);
        log.debug("清除用户时间线缓存: userId={}", userId);
    }

    // ========================================
    // 关注时间线缓存
    // ========================================

    /**
     * 缓存关注时间线
     */
    public void cacheFollowingTimeline(UserId userId, List<FeedId> timeline) {
        String key = CacheKeys.followingTimeline(userId);
        List<FeedId> limitedTimeline = timeline.size() > TIMELINE_MAX_SIZE
                ? timeline.subList(0, TIMELINE_MAX_SIZE)
                : timeline;

        cacheService.setList(key, limitedTimeline, FOLLOWING_TIMELINE_TTL);
        log.debug("缓存关注时间线: userId={}, count={}", userId, limitedTimeline.size());
    }

    /**
     * 获取关注时间线缓存
     */
    public Optional<List<FeedId>> getFollowingTimeline(UserId userId) {
        String key = CacheKeys.followingTimeline(userId);
        List<FeedId> timeline = cacheService.getList(key, FeedId.class);
        return timeline.isEmpty() ? Optional.empty() : Optional.of(timeline);
    }

    /**
     * 推送动态到关注者的时间线
     */
    public void pushToFollowersTimeline(List<UserId> followerIds, FeedId feedId) {
        for (UserId followerId : followerIds) {
            String key = CacheKeys.followingTimeline(followerId);
            cacheService.addToListHead(key, feedId);

            // 维护时间线长度
            trimTimelineToMaxSize(key);
        }
        log.debug("推送动态到关注者时间线: feedId={}, followers={}", feedId, followerIds.size());
    }

    /**
     * 清除关注时间线缓存
     */
    public void invalidateFollowingTimeline(UserId userId) {
        String key = CacheKeys.followingTimeline(userId);
        cacheService.delete(key);
        log.debug("清除关注时间线缓存: userId={}", userId);
    }

    // ========================================
    // 热门动态缓存
    // ========================================

    /**
     * 缓存热门动态列表
     */
    public void cacheHotFeeds(List<FeedId> hotFeeds) {
        String key = CacheKeys.publicHotFeeds();
        List<FeedId> limitedFeeds = hotFeeds.size() > HOT_FEEDS_MAX_SIZE
                ? hotFeeds.subList(0, HOT_FEEDS_MAX_SIZE)
                : hotFeeds;

        cacheService.setList(key, limitedFeeds, HOT_FEEDS_TTL);
        log.debug("缓存热门动态列表: count={}", limitedFeeds.size());
    }

    /**
     * 获取热门动态缓存
     */
    public Optional<List<FeedId>> getHotFeeds() {
        String key = CacheKeys.publicHotFeeds();
        List<FeedId> hotFeeds = cacheService.getList(key, FeedId.class);
        return hotFeeds.isEmpty() ? Optional.empty() : Optional.of(hotFeeds);
    }

    /**
     * 添加动态到热门列表头部
     */
    public void addToHotFeeds(FeedId feedId) {
        String key = CacheKeys.publicHotFeeds();
        cacheService.addToListHead(key, feedId);

        // 维护热门列表长度
        trimListToMaxSize(key, HOT_FEEDS_MAX_SIZE);
        log.debug("添加动态到热门列表: feedId={}", feedId);
    }

    // ========================================
    // 话题动态缓存
    // ========================================

    /**
     * 缓存话题动态列表
     */
    public void cacheHashtagFeeds(String hashtag, List<FeedId> feeds) {
        String key = CacheKeys.hashtagFeeds(hashtag);
        cacheService.setList(key, feeds, HASHTAG_FEEDS_TTL);
        log.debug("缓存话题动态: hashtag={}, count={}", hashtag, feeds.size());
    }

    /**
     * 获取话题动态缓存
     */
    public Optional<List<FeedId>> getHashtagFeeds(String hashtag) {
        String key = CacheKeys.hashtagFeeds(hashtag);
        List<FeedId> feeds = cacheService.getList(key, FeedId.class);
        return feeds.isEmpty() ? Optional.empty() : Optional.of(feeds);
    }

    /**
     * 添加动态到话题列表
     */
    public void addToHashtagFeeds(String hashtag, FeedId feedId) {
        String key = CacheKeys.hashtagFeeds(hashtag);
        cacheService.addToListHead(key, feedId);
        log.debug("添加动态到话题列表: hashtag={}, feedId={}", hashtag, feedId);
    }

    /**
     * 清除话题动态缓存
     */
    public void invalidateHashtagFeeds(String hashtag) {
        String key = CacheKeys.hashtagFeeds(hashtag);
        cacheService.delete(key);
        log.debug("清除话题动态缓存: hashtag={}", hashtag);
    }

    // ========================================
    // 位置动态缓存
    // ========================================

    /**
     * 缓存位置动态列表
     */
    public void cacheLocationFeeds(String location, List<FeedId> feeds) {
        String key = CacheKeys.locationFeeds(location);
        cacheService.setList(key, feeds, HASHTAG_FEEDS_TTL);
        log.debug("缓存位置动态: location={}, count={}", location, feeds.size());
    }

    /**
     * 获取位置动态缓存
     */
    public Optional<List<FeedId>> getLocationFeeds(String location) {
        String key = CacheKeys.locationFeeds(location);
        List<FeedId> feeds = cacheService.getList(key, FeedId.class);
        return feeds.isEmpty() ? Optional.empty() : Optional.of(feeds);
    }

    // ========================================
    // 批量操作
    // ========================================

    /**
     * 清除动态相关的所有缓存
     */
    public void invalidateFeedCache(FeedId feedId) {
        String pattern = CacheKeys.feedPattern(feedId);
        cacheService.deletePattern(pattern);
        log.debug("清除动态相关缓存: feedId={}", feedId);
    }

    /**
     * 批量缓存动态详情
     */
    public void batchCacheFeedDetails(List<FeedResponse> feeds) {
        for (FeedResponse feed : feeds) {
            cacheFeedDetail(feed.feedId(), feed);
        }
        log.debug("批量缓存动态详情: count={}", feeds.size());
    }

    // ========================================
    // 私有辅助方法
    // ========================================

    /**
     * 维护时间线最大长度
     */
    private void trimTimelineToMaxSize(String key) {
        trimListToMaxSize(key, TIMELINE_MAX_SIZE);
    }

    /**
     * 维护列表最大长度
     */
    private void trimListToMaxSize(String key, int maxSize) {
        try {
            // 保留头部maxSize个元素，删除其余的
            List<Object> range = cacheService.getListRange(key, 0, maxSize - 1, Object.class);
            if (range.size() >= maxSize) {
                // 重新设置列表
                cacheService.setList(key, range, Duration.ofMinutes(30));
            }
        } catch (Exception e) {
            log.error("维护列表长度失败: key={}, maxSize={}, error={}", key, maxSize, e.getMessage());
        }
    }

    // ========================================
    // 缓存统计
    // ========================================

    /**
     * 获取时间线缓存统计信息
     */
    public TimelineCacheStats getCacheStats() {
        // 这里可以实现缓存命中率等统计信息
        return new TimelineCacheStats(0, 0, 0, 0.0);
    }

    /**
     * 时间线缓存统计记录
     */
    public record TimelineCacheStats(
            long feedDetailHits,
            long timelineHits,
            long hotFeedsHits,
            double hitRate
    ) {
    }
}
